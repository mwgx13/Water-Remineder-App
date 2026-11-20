package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HydrationRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(expectedUserId: String): String {
    val uid =
      auth.currentUser?.uid
        ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user access." }
    return uid
  }

  fun observeUserProfile(userId: String): Flow<UserHydrationProfile?> = flow {
    val uid = requireUserId(userId)
    val path = "users/$uid"
    emitAll(
      db
        .collection("users")
        .document(uid)
        .snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(
              UserHydrationProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.GET, path)
          }
          throw error
        }
    )
  }

  fun observeIntakeLogs(userId: String): Flow<List<HydrationLog>> = flow {
    val uid = requireUserId(userId)
    val path = "users/$uid/intakeLogs"
    emitAll(
      db
        .collection("users")
        .document(uid)
        .collection("intakeLogs")
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              HydrationLog::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.loggedAtMillis }
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun ensureUserProfile(userId: String, rawDisplayName: String?): Result<Unit> {
    val docPath = "users/$userId"
    return try {
      val uid = requireUserId(userId)
      val docRef = db.collection("users").document(uid)
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val safeName =
          rawDisplayName?.trim()?.takeIf { it.isNotEmpty() }?.take(80) ?: "Hydration Guest"
        val payload =
          mapOf(
            "userId" to uid,
            "displayName" to safeName,
            "dailyGoalMl" to 2500L,
            "unit" to HydrationUnit.ML.name,
            "quickPresetSmallMl" to 150L,
            "quickPresetMediumMl" to 250L,
            "quickPresetLargeMl" to 500L,
            "wakeHour" to 7L,
            "sleepHour" to 22L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateProfilePreferences(
    userId: String,
    dailyGoalMl: Int,
    unit: HydrationUnit,
    quickPresetSmallMl: Int,
    quickPresetMediumMl: Int,
    quickPresetLargeMl: Int,
    wakeHour: Int,
    sleepHour: Int,
  ): Result<Unit> {
    val docPath = "users/$userId"
    return try {
      val uid = requireUserId(userId)
      require(dailyGoalMl in 500..10000) { "Daily goal must be between 500 and 10,000 mL" }
      require(quickPresetSmallMl in 50..2000) { "Small preset must be between 50 and 2,000 mL" }
      require(quickPresetMediumMl in 50..2000) { "Medium preset must be between 50 and 2,000 mL" }
      require(quickPresetLargeMl in 50..2000) { "Large preset must be between 50 and 2,000 mL" }
      require(wakeHour in 0..23 && sleepHour in 0..23) { "Active hours must be valid 0-23" }

      db
        .collection("users")
        .document(uid)
        .update(
          mapOf(
            "dailyGoalMl" to dailyGoalMl.toLong(),
            "unit" to unit.name,
            "quickPresetSmallMl" to quickPresetSmallMl.toLong(),
            "quickPresetMediumMl" to quickPresetMediumMl.toLong(),
            "quickPresetLargeMl" to quickPresetLargeMl.toLong(),
            "wakeHour" to wakeHour.toLong(),
            "sleepHour" to sleepHour.toLong(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addIntakeLog(
    userId: String,
    amountMl: Int,
    beverageType: BeverageType,
    dayKey: String,
    note: String?,
    loggedAtMillis: Long,
  ): Result<Unit> {
    var docPath = "users/$userId/intakeLogs"
    return try {
      val uid = requireUserId(userId)
      require(amountMl in 1..5000) { "Volume must be between 1 and 5,000 mL" }
      require(dayKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
        "Invalid dayKey format"
      }
      val effectiveMl = beverageType.computeEffectiveMl(amountMl)
      val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }?.take(140)
      val docRef = db.collection("users").document(uid).collection("intakeLogs").document()
      docPath = docRef.path

      val payload =
        buildMap<String, Any?> {
            put("userId", uid)
            put("amountMl", amountMl.toLong())
            put("effectiveMl", effectiveMl.toLong())
            put("beverageType", beverageType.name)
            put("dayKey", dayKey)
            put("note", cleanNote)
            put("loggedAtMillis", loggedAtMillis)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
          .filterValues { it != null }
          .mapValues { it.value!! }

      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateIntakeLog(
    userId: String,
    logId: String,
    amountMl: Int,
    beverageType: BeverageType,
    note: String?,
  ): Result<Unit> {
    val docPath = "users/$userId/intakeLogs/$logId"
    return try {
      val uid = requireUserId(userId)
      require(logId.isNotBlank()) { "Log ID must not be blank" }
      require(amountMl in 1..5000) { "Volume must be between 1 and 5,000 mL" }
      val effectiveMl = beverageType.computeEffectiveMl(amountMl)
      val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }?.take(140)

      val updates =
        mutableMapOf<String, Any>(
          "amountMl" to amountMl.toLong(),
          "effectiveMl" to effectiveMl.toLong(),
          "beverageType" to beverageType.name,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      if (cleanNote != null) {
        updates["note"] = cleanNote
      } else {
        updates["note"] = FieldValue.delete()
      }

      db
        .collection("users")
        .document(uid)
        .collection("intakeLogs")
        .document(logId)
        .update(updates)
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteIntakeLog(userId: String, logId: String): Result<Unit> {
    val docPath = "users/$userId/intakeLogs/$logId"
    return try {
      val uid = requireUserId(userId)
      require(logId.isNotBlank()) { "Log ID must not be blank" }
      db
        .collection("users")
        .document(uid)
        .collection("intakeLogs")
        .document(logId)
        .delete()
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }
}
