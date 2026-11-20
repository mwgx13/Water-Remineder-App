package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BeverageType
import com.example.data.HydrationLog
import com.example.data.HydrationRepository
import com.example.data.HydrationUnit
import com.example.data.UiState
import com.example.data.UserHydrationProfile
import com.example.ui.components.DailyHydrationPoint
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodaySummary(
  val todayKey: String,
  val todayDateLabel: String,
  val effectiveMl: Int,
  val rawMl: Int,
  val goalMl: Int,
  val currentStreakDays: Int,
  val pacingTargetMl: Int,
  val todayLogs: List<HydrationLog>,
)

data class BeverageBreakdownItem(
  val beverage: BeverageType,
  val rawMl: Int,
  val effectiveMl: Int,
)

data class InsightsSummary(
  val series7Days: List<DailyHydrationPoint>,
  val series30Days: List<DailyHydrationPoint>,
  val dailyAverageMl: Int,
  val goalCompletionPercent: Int,
  val currentStreakDays: Int,
  val bestDayMl: Int,
  val beverageBreakdown: List<BeverageBreakdownItem>,
  val totalPeriodMl: Int,
)

data class SanctuaryDashboardState(
  val profile: UserHydrationProfile,
  val today: TodaySummary,
  val insights: InsightsSummary,
)

class HydrationViewModel(
  private val repository: HydrationRepository,
  private val currentUserId: String,
  private val initialDisplayName: String?,
) : ViewModel() {

  private val _selectedBeverage = MutableStateFlow(BeverageType.WATER)
  val selectedBeverage: StateFlow<BeverageType> = _selectedBeverage.asStateFlow()

  private val _actionMessage = MutableStateFlow<String?>(null)
  val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

  private val _isMutating = MutableStateFlow(false)
  val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

  private val _pulseCount = MutableStateFlow(0)
  val pulseCount: StateFlow<Int> = _pulseCount.asStateFlow()

  private val profileFlow: StateFlow<UiState<UserHydrationProfile>> =
    repository
      .observeUserProfile(currentUserId)
      .map<UserHydrationProfile?, UiState<UserHydrationProfile>> { profile ->
        UiState.Success(
          profile
            ?: UserHydrationProfile(
              userId = currentUserId,
              displayName = initialDisplayName?.trim()?.takeIf { it.isNotEmpty() } ?: "Sanctuary Guest",
            )
        )
      }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing user profile", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load hydration profile"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  private val logsFlow: StateFlow<UiState<List<HydrationLog>>> =
    repository
      .observeIntakeLogs(currentUserId)
      .map<List<HydrationLog>, UiState<List<HydrationLog>>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing intake logs", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load hydration logs"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val dashboardState: StateFlow<UiState<SanctuaryDashboardState>> =
    combine(profileFlow, logsFlow) { profileUi, logsUi ->
        when {
          profileUi is UiState.Error -> UiState.Error(profileUi.message)
          logsUi is UiState.Error -> UiState.Error(logsUi.message)
          profileUi is UiState.Success && logsUi is UiState.Success -> {
            UiState.Success(buildDashboard(profileUi.data, logsUi.data))
          }
          else -> UiState.Loading
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  init {
    viewModelScope.launch {
      repository.ensureUserProfile(currentUserId, initialDisplayName).onFailure { err ->
        _actionMessage.value = err.localizedMessage ?: "Could not initialize profile"
      }
    }
  }

  fun selectBeverage(beverage: BeverageType) {
    _selectedBeverage.value = beverage
  }

  fun clearActionMessage() {
    _actionMessage.value = null
  }

  fun quickAdd(amountMl: Int) {
    logIntake(
      amountMl = amountMl,
      beverageType = _selectedBeverage.value,
      note = null,
    )
  }

  fun logIntake(amountMl: Int, beverageType: BeverageType, note: String?) {
    viewModelScope.launch {
      _isMutating.value = true
      val nowMillis = System.currentTimeMillis()
      val dayKey = formatDayKey(nowMillis)
      val result =
        repository.addIntakeLog(
          userId = currentUserId,
          amountMl = amountMl,
          beverageType = beverageType,
          dayKey = dayKey,
          note = note,
          loggedAtMillis = nowMillis,
        )
      _isMutating.value = false
      result
        .onSuccess {
          _pulseCount.value += 1
          _actionMessage.value = "Added ${beverageType.shortLabel} (${amountMl} mL)"
        }
        .onFailure { err ->
          _actionMessage.value = err.localizedMessage ?: "Failed to save intake log"
        }
    }
  }

  fun updateIntakeLog(logId: String, amountMl: Int, beverageType: BeverageType, note: String?) {
    viewModelScope.launch {
      _isMutating.value = true
      val result =
        repository.updateIntakeLog(
          userId = currentUserId,
          logId = logId,
          amountMl = amountMl,
          beverageType = beverageType,
          note = note,
        )
      _isMutating.value = false
      result
        .onSuccess { _actionMessage.value = "Intake entry updated" }
        .onFailure { err ->
          _actionMessage.value = err.localizedMessage ?: "Failed to update intake log"
        }
    }
  }

  fun deleteIntakeLog(logId: String) {
    viewModelScope.launch {
      _isMutating.value = true
      val result = repository.deleteIntakeLog(userId = currentUserId, logId = logId)
      _isMutating.value = false
      result
        .onSuccess { _actionMessage.value = "Entry removed" }
        .onFailure { err ->
          _actionMessage.value = err.localizedMessage ?: "Failed to delete entry"
        }
    }
  }

  fun updatePreferences(
    dailyGoalMl: Int,
    unit: HydrationUnit,
    quickPresetSmallMl: Int,
    quickPresetMediumMl: Int,
    quickPresetLargeMl: Int,
    wakeHour: Int,
    sleepHour: Int,
  ) {
    viewModelScope.launch {
      _isMutating.value = true
      val result =
        repository.updateProfilePreferences(
          userId = currentUserId,
          dailyGoalMl = dailyGoalMl,
          unit = unit,
          quickPresetSmallMl = quickPresetSmallMl,
          quickPresetMediumMl = quickPresetMediumMl,
          quickPresetLargeMl = quickPresetLargeMl,
          wakeHour = wakeHour,
          sleepHour = sleepHour,
        )
      _isMutating.value = false
      result
        .onSuccess { _actionMessage.value = "Ritual preferences saved" }
        .onFailure { err ->
          _actionMessage.value = err.localizedMessage ?: "Failed to update preferences"
        }
    }
  }

  private fun buildDashboard(
    profile: UserHydrationProfile,
    allLogs: List<HydrationLog>,
  ): SanctuaryDashboardState {
    val now = Calendar.getInstance()
    val todayKey = formatDayKey(now.timeInMillis)
    val todayDateLabel =
      SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now.time)
    val goalMl = profile.dailyGoalMl.toInt().coerceIn(500, 10000)

    val logsByDay = allLogs.groupBy { it.dayKey }
    val todayLogs =
      (logsByDay[todayKey] ?: emptyList()).sortedByDescending { it.loggedAtMillis }

    val todayEffective = todayLogs.sumOf { it.effectiveMl.toInt() }
    val todayRaw = todayLogs.sumOf { it.amountMl.toInt() }

    val streakDays = computeStreakDays(logsByDay, goalMl, now)
    val pacingTargetMl =
      computePacingTargetMl(
        goalMl = goalMl,
        wakeHour = profile.wakeHour.toInt(),
        sleepHour = profile.sleepHour.toInt(),
        currentHour = now.get(Calendar.HOUR_OF_DAY),
      )

    val series7 = buildSeries(days = 7, logsByDay = logsByDay, goalMl = goalMl, shortFmt = "EEE")
    val series30 = buildSeries(days = 30, logsByDay = logsByDay, goalMl = goalMl, shortFmt = "d")

    val last30Keys = series30.map { it.dayKey }.toSet()
    val periodLogs = allLogs.filter { it.dayKey in last30Keys }
    val activeDaysCount = series7.count { it.effectiveMl > 0 }.coerceAtLeast(1)
    val avg7 = series7.sumOf { it.effectiveMl } / activeDaysCount
    val goalHits7 = series7.count { it.effectiveMl >= goalMl }
    val completionPct = ((goalHits7.toFloat() / 7f) * 100f).toInt()
    val bestDayMl =
      logsByDay.values.maxOfOrNull { dayList -> dayList.sumOf { it.effectiveMl.toInt() } } ?: 0

    val breakdown =
      periodLogs
        .groupBy { it.beverage }
        .map { (bev, items) ->
          BeverageBreakdownItem(
            beverage = bev,
            rawMl = items.sumOf { it.amountMl.toInt() },
            effectiveMl = items.sumOf { it.effectiveMl.toInt() },
          )
        }
        .sortedByDescending { it.rawMl }

    val totalPeriodMl = breakdown.sumOf { it.rawMl }

    return SanctuaryDashboardState(
      profile = profile,
      today =
        TodaySummary(
          todayKey = todayKey,
          todayDateLabel = todayDateLabel,
          effectiveMl = todayEffective,
          rawMl = todayRaw,
          goalMl = goalMl,
          currentStreakDays = streakDays,
          pacingTargetMl = pacingTargetMl,
          todayLogs = todayLogs,
        ),
      insights =
        InsightsSummary(
          series7Days = series7,
          series30Days = series30,
          dailyAverageMl = avg7,
          goalCompletionPercent = completionPct,
          currentStreakDays = streakDays,
          bestDayMl = bestDayMl,
          beverageBreakdown = breakdown,
          totalPeriodMl = totalPeriodMl,
        ),
    )
  }

  private fun buildSeries(
    days: Int,
    logsByDay: Map<String, List<HydrationLog>>,
    goalMl: Int,
    shortFmt: String,
  ): List<DailyHydrationPoint> {
    val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labelFormatter = SimpleDateFormat(shortFmt, Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -(days - 1))

    return (0 until days).map {
      val key = keyFormatter.format(cal.time)
      val label = labelFormatter.format(cal.time)
      val dayLogs = logsByDay[key] ?: emptyList()
      val eff = dayLogs.sumOf { item -> item.effectiveMl.toInt() }
      val raw = dayLogs.sumOf { item -> item.amountMl.toInt() }
      cal.add(Calendar.DAY_OF_YEAR, 1)
      DailyHydrationPoint(
        dayKey = key,
        shortLabel = label,
        effectiveMl = eff,
        rawMl = raw,
        goalMl = goalMl,
      )
    }
  }

  private fun computeStreakDays(
    logsByDay: Map<String, List<HydrationLog>>,
    goalMl: Int,
    now: Calendar,
  ): Int {
    val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = now.clone() as Calendar
    val todayKey = keyFormatter.format(cal.time)
    val todayEffective = logsByDay[todayKey]?.sumOf { it.effectiveMl.toInt() } ?: 0

    var streak = 0
    if (todayEffective >= goalMl) {
      streak++
    }
    cal.add(Calendar.DAY_OF_YEAR, -1)
    for (i in 0 until 90) {
      val key = keyFormatter.format(cal.time)
      val dayEffective = logsByDay[key]?.sumOf { it.effectiveMl.toInt() } ?: 0
      if (dayEffective >= goalMl) {
        streak++
        cal.add(Calendar.DAY_OF_YEAR, -1)
      } else {
        break
      }
    }
    return streak
  }

  private fun computePacingTargetMl(
    goalMl: Int,
    wakeHour: Int,
    sleepHour: Int,
    currentHour: Int,
  ): Int {
    val span = (sleepHour - wakeHour).coerceAtLeast(6)
    val elapsed = (currentHour - wakeHour).coerceIn(0, span)
    return ((elapsed.toFloat() / span.toFloat()) * goalMl).toInt()
  }

  private fun formatDayKey(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
  }

  companion object {
    private const val TAG = "HydrationVM"
    private const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
