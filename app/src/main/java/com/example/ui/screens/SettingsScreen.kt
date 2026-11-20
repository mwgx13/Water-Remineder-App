package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.HydrationUnit
import com.example.ui.components.FluidPrimaryButton
import com.example.ui.components.FluidSelectorChip
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.viewmodel.SanctuaryDashboardState
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
  dashboard: SanctuaryDashboardState,
  userEmail: String?,
  isWideLayout: Boolean,
  onSavePreferences: (
    dailyGoalMl: Int,
    unit: HydrationUnit,
    smallMl: Int,
    mediumMl: Int,
    largeMl: Int,
    wakeHour: Int,
    sleepHour: Int,
  ) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val profile = dashboard.profile

  var goalMl by remember(profile.dailyGoalMl) { mutableIntStateOf(profile.dailyGoalMl.toInt()) }
  var selectedUnit by remember(profile.unit) { mutableStateOf(profile.preferredUnit) }
  var smallMl by remember(profile.quickPresetSmallMl) {
    mutableIntStateOf(profile.quickPresetSmallMl.toInt())
  }
  var mediumMl by remember(profile.quickPresetMediumMl) {
    mutableIntStateOf(profile.quickPresetMediumMl.toInt())
  }
  var largeMl by remember(profile.quickPresetLargeMl) {
    mutableIntStateOf(profile.quickPresetLargeMl.toInt())
  }
  var wakeHour by remember(profile.wakeHour) { mutableIntStateOf(profile.wakeHour.toInt()) }
  var sleepHour by remember(profile.sleepHour) { mutableIntStateOf(profile.sleepHour.toInt()) }

  val hasChanges =
    goalMl != profile.dailyGoalMl.toInt() ||
      selectedUnit != profile.preferredUnit ||
      smallMl != profile.quickPresetSmallMl.toInt() ||
      mediumMl != profile.quickPresetMediumMl.toInt() ||
      largeMl != profile.quickPresetLargeMl.toInt() ||
      wakeHour != profile.wakeHour.toInt() ||
      sleepHour != profile.sleepHour.toInt()

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
      contentPadding =
        PaddingValues(
          start = 20.dp,
          end = 20.dp,
          top = 16.dp,
          bottom = if (isWideLayout) 32.dp else 120.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      item {
        Column {
          Text(
            text = "PERSONAL CALIBRATION",
            style = MaterialTheme.typography.labelSmall,
            color = CrystallineAqua,
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Ritual Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurfaceIce,
          )
        }
      }

      // Daily Hydration Goal Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Daily Hydration Goal",
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceIce,
              )
              Text(
                text = "Recommended 2,200 – 3,200 mL",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantMist,
              )
            }
            Text(
              text = selectedUnit.formatVolume(goalMl),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = CrystallineAqua,
            )
          }

          Spacer(Modifier.height(14.dp))

          Slider(
            value = goalMl.toFloat(),
            onValueChange = { raw ->
              goalMl = ((raw / 100f).roundToInt() * 100).coerceIn(1000, 6000)
            },
            valueRange = 1000f..5000f,
            colors =
              SliderDefaults.colors(
                thumbColor = CrystallineAqua,
                activeTrackColor = OceanicCyan,
                inactiveTrackColor = Color(0x28FFFFFF),
              ),
            modifier = Modifier.testTag("goal_slider"),
          )

          Spacer(Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            listOf(2000, 2500, 3000, 3500).forEach { preset ->
              FluidSelectorChip(
                label = selectedUnit.formatCompact(preset),
                selected = goalMl == preset,
                onClick = { goalMl = preset },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      // Units & Quick-Add Droplets Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          Text(
            text = "Units & Quick Droplets",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceIce,
          )
          Spacer(Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            FluidSelectorChip(
              label = "Milliliters (mL)",
              selected = selectedUnit == HydrationUnit.ML,
              onClick = { selectedUnit = HydrationUnit.ML },
              modifier = Modifier.weight(1f).testTag("unit_ml_chip"),
            )
            FluidSelectorChip(
              label = "Fluid Ounces (oz)",
              selected = selectedUnit == HydrationUnit.OZ,
              onClick = { selectedUnit = HydrationUnit.OZ },
              modifier = Modifier.weight(1f).testTag("unit_oz_chip"),
            )
          }

          Spacer(Modifier.height(18.dp))
          GlassHairlineDivider()
          Spacer(Modifier.height(18.dp))

          Text(
            text = "Vessel Preset Pack",
            style = MaterialTheme.typography.titleSmall,
            color = OnSurfaceIce,
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Choose three quick-tap droplet volumes for your Sanctuary screen",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantMist,
          )
          Spacer(Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            val standardActive = smallMl == 150 && mediumMl == 250 && largeMl == 500
            val bottleActive = smallMl == 250 && mediumMl == 500 && largeMl == 750
            val sipActive = smallMl == 100 && mediumMl == 200 && largeMl == 350

            FluidSelectorChip(
              label = "150 / 250 / 500",
              selected = standardActive,
              onClick = {
                smallMl = 150
                mediumMl = 250
                largeMl = 500
              },
              modifier = Modifier.weight(1f),
            )
            FluidSelectorChip(
              label = "250 / 500 / 750",
              selected = bottleActive,
              onClick = {
                smallMl = 250
                mediumMl = 500
                largeMl = 750
              },
              modifier = Modifier.weight(1f),
            )
            FluidSelectorChip(
              label = "100 / 200 / 350",
              selected = sipActive,
              onClick = {
                smallMl = 100
                mediumMl = 200
                largeMl = 350
              },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // Active Hydration Window Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Active Rhythm Window",
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceIce,
              )
              Text(
                text = "Used to calculate your hourly pacing curve",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantMist,
              )
            }
            Text(
              text = "${wakeHour}:00 – ${sleepHour}:00",
              style = MaterialTheme.typography.labelLarge,
              color = CrystallineAqua,
            )
          }

          Spacer(Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FluidSelectorChip(
              label = "Early (6–21)",
              selected = wakeHour == 6 && sleepHour == 21,
              onClick = {
                wakeHour = 6
                sleepHour = 21
              },
              modifier = Modifier.weight(1f),
            )
            FluidSelectorChip(
              label = "Balanced (7–22)",
              selected = wakeHour == 7 && sleepHour == 22,
              onClick = {
                wakeHour = 7
                sleepHour = 22
              },
              modifier = Modifier.weight(1f),
            )
            FluidSelectorChip(
              label = "Late (9–23)",
              selected = wakeHour == 9 && sleepHour == 23,
              onClick = {
                wakeHour = 9
                sleepHour = 23
              },
              modifier = Modifier.weight(1f),
            )
          }

          if (hasChanges) {
            Spacer(Modifier.height(20.dp))
            FluidPrimaryButton(
              text = "Save Calibration",
              leadingIcon = Icons.Filled.Check,
              onClick = {
                onSavePreferences(
                  goalMl,
                  selectedUnit,
                  smallMl,
                  mediumMl,
                  largeMl,
                  wakeHour,
                  sleepHour,
                )
              },
              modifier = Modifier.fillMaxWidth().testTag("save_preferences_button"),
            )
          }
        }
      }

      // Google Account Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Surface(
              modifier = Modifier.size(46.dp),
              shape = CircleShape,
              color = Color(0x2200B4D8),
              border = BorderStroke(1.dp, CrystallineAqua.copy(alpha = 0.4f)),
            ) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Account profile",
                tint = CrystallineAqua,
                modifier = Modifier.padding(11.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = profile.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceIce,
              )
              if (!userEmail.isNullOrBlank()) {
                Text(
                  text = userEmail,
                  style = MaterialTheme.typography.bodySmall,
                  color = OnSurfaceVariantMist,
                )
              }
            }
          }

          Spacer(Modifier.height(18.dp))
          GlassHairlineDivider()
          Spacer(Modifier.height(16.dp))

          Surface(
            modifier = Modifier.fillMaxWidth().testTag("sign_out_button"),
            shape = CircleShape,
            color = ErrorCoral.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, ErrorCoral.copy(alpha = 0.42f)),
            onClick = onSignOut,
          ) {
            Row(
              modifier = Modifier.padding(vertical = 14.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = ErrorCoral,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.size(8.dp))
              Text(
                text = "Sign Out",
                style = MaterialTheme.typography.labelLarge,
                color = ErrorCoral,
              )
            }
          }
        }
      }
    }
  }
}
