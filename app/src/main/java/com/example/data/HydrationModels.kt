package com.example.data

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BeverageCoffeeColor
import com.example.ui.theme.BeverageElectrolytesColor
import com.example.ui.theme.BeverageGreenTeaColor
import com.example.ui.theme.BeverageHerbalColor
import com.example.ui.theme.BeverageSparklingColor
import com.example.ui.theme.BeverageWaterColor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.roundToInt

enum class BeverageType(
  val label: String,
  val shortLabel: String,
  val hydrationFactor: Float,
  val accentColor: Color,
) {
  WATER("Pure Water", "Water", 1.00f, BeverageWaterColor),
  SPARKLING("Sparkling", "Sparkling", 1.00f, BeverageSparklingColor),
  HERBAL_TEA("Herbal Tea", "Herbal", 0.95f, BeverageHerbalColor),
  GREEN_TEA("Green Tea", "Green Tea", 0.90f, BeverageGreenTeaColor),
  ELECTROLYTES("Electrolytes", "Electrolytes", 1.15f, BeverageElectrolytesColor),
  COFFEE("Coffee", "Coffee", 0.80f, BeverageCoffeeColor);

  fun computeEffectiveMl(rawMl: Int): Int {
    return (rawMl * hydrationFactor).roundToInt().coerceIn(1, 6000)
  }

  companion object {
    fun fromRaw(raw: String?): BeverageType {
      return entries.firstOrNull { it.name == raw } ?: WATER
    }
  }
}

enum class HydrationUnit(val suffix: String) {
  ML("mL"),
  OZ("oz");

  fun formatVolume(ml: Int): String {
    return when (this) {
      ML -> "%,d mL".format(ml)
      OZ -> {
        val oz = ml / ML_PER_OZ
        "%.1f oz".format(oz)
      }
    }
  }

  fun formatCompact(ml: Int): String {
    return when (this) {
      ML -> "$ml mL"
      OZ -> {
        val oz = ml / ML_PER_OZ
        if (oz % 1.0 == 0.0) "${oz.toInt()} oz" else "%.1f oz".format(oz)
      }
    }
  }

  fun formatNumberOnly(ml: Int): String {
    return when (this) {
      ML -> "%,d".format(ml)
      OZ -> "%.1f".format(ml / ML_PER_OZ)
    }
  }

  companion object {
    const val ML_PER_OZ = 29.5735

    fun fromRaw(raw: String?): HydrationUnit {
      return entries.firstOrNull { it.name == raw } ?: ML
    }
  }
}

@Keep
@IgnoreExtraProperties
data class UserHydrationProfile(
  val userId: String = "",
  val displayName: String = "Hydration Guest",
  val dailyGoalMl: Long = 2500L,
  val unit: String = HydrationUnit.ML.name,
  val quickPresetSmallMl: Long = 150L,
  val quickPresetMediumMl: Long = 250L,
  val quickPresetLargeMl: Long = 500L,
  val wakeHour: Long = 7L,
  val sleepHour: Long = 22L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val preferredUnit: HydrationUnit
    get() = HydrationUnit.fromRaw(unit)
}

@Keep
@IgnoreExtraProperties
data class HydrationLog(
  @DocumentId val id: String = "",
  val userId: String = "",
  val amountMl: Long = 0L,
  val effectiveMl: Long = 0L,
  val beverageType: String = BeverageType.WATER.name,
  val dayKey: String = "",
  val note: String? = null,
  val loggedAtMillis: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val beverage: BeverageType
    get() = BeverageType.fromRaw(beverageType)
}

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>

  data class Success<T>(val data: T) : UiState<T>

  data class Error(val message: String) : UiState<Nothing>
}
