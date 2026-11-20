package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BeverageType
import com.example.data.HydrationUnit
import com.example.ui.components.BeverageDistributionBar
import com.example.ui.components.DailyHydrationPoint
import com.example.ui.components.FluidSelectorChip
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.HydrationTelemetryChart
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.theme.PrimaryCyan
import com.example.ui.viewmodel.SanctuaryDashboardState

@Composable
fun InsightsScreen(
  dashboard: SanctuaryDashboardState,
  isWideLayout: Boolean,
  modifier: Modifier = Modifier,
) {
  val unit = dashboard.profile.preferredUnit
  val insights = dashboard.insights
  var show30Days by remember { mutableStateOf(false) }

  val activeSeries = if (show30Days) insights.series30Days else insights.series7Days
  var selectedPoint by remember(activeSeries) {
    mutableStateOf<DailyHydrationPoint?>(activeSeries.lastOrNull())
  }

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
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "TELEMETRY & TRENDS",
              style = MaterialTheme.typography.labelSmall,
              color = CrystallineAqua,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = "Hydration Rhythm",
              style = MaterialTheme.typography.headlineLarge,
              color = OnSurfaceIce,
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluidSelectorChip(
              label = "7D",
              selected = !show30Days,
              onClick = { show30Days = false },
              modifier = Modifier.testTag("range_7d_chip"),
            )
            FluidSelectorChip(
              label = "30D",
              selected = show30Days,
              onClick = { show30Days = true },
              modifier = Modifier.testTag("range_30d_chip"),
            )
          }
        }
      }

      // Main Fluid Telemetry Chart Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 28.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          val inspected = selectedPoint ?: activeSeries.lastOrNull()
          if (inspected != null) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom,
            ) {
              Column {
                Text(
                  text = inspected.dayKey,
                  style = MaterialTheme.typography.labelMedium,
                  color = CrystallineAqua,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                  text = unit.formatVolume(inspected.effectiveMl),
                  style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                  color = OnSurfaceIce,
                )
              }
              Text(
                text =
                  if (inspected.effectiveMl >= inspected.goalMl) {
                    "Goal Met"
                  } else {
                    "${((inspected.effectiveMl.toFloat() / inspected.goalMl.coerceAtLeast(1)) * 100).toInt()}% of goal"
                  },
                style = MaterialTheme.typography.labelLarge,
                color =
                  if (inspected.effectiveMl >= inspected.goalMl) PrimaryCyan else OnSurfaceVariantMist,
              )
            }
            Spacer(Modifier.height(16.dp))
            GlassHairlineDivider()
            Spacer(Modifier.height(16.dp))
          }

          HydrationTelemetryChart(
            points = activeSeries,
            selectedDayKey = selectedPoint?.dayKey,
            unit = unit,
            onSelectDay = { selectedPoint = it },
          )
        }
      }

      // 2x2 Summary Telemetry Cards
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            MetricGlassTile(
              label = "7-DAY AVERAGE",
              value = unit.formatVolume(insights.dailyAverageMl),
              subtext = "Active days",
              modifier = Modifier.weight(1f),
            )
            MetricGlassTile(
              label = "GOAL CONSISTENCY",
              value = "${insights.goalCompletionPercent}%",
              subtext = "Last 7 days",
              modifier = Modifier.weight(1f),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            MetricGlassTile(
              label = "CURRENT STREAK",
              value = "${insights.currentStreakDays} days",
              subtext = "Consecutive targets",
              modifier = Modifier.weight(1f),
            )
            MetricGlassTile(
              label = "PEAK DAY",
              value = unit.formatVolume(insights.bestDayMl),
              subtext = "All-time best",
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // Beverage Breakdown Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 24.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          Text(
            text = "Beverage Composition (30 Days)",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceIce,
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = "Weighted by hydration absorption factor",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantMist,
          )
          Spacer(Modifier.height(16.dp))
          GlassHairlineDivider()
          Spacer(Modifier.height(16.dp))

          if (insights.beverageBreakdown.isEmpty()) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = CrystallineAqua,
                modifier = Modifier.size(22.dp),
              )
              Text(
                text = "Log drinks in your Sanctuary to unlock your 30-day beverage composition.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariantMist,
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              insights.beverageBreakdown.forEach { item ->
                BeverageDistributionBar(
                  beverage = item.beverage,
                  volumeMl = item.rawMl,
                  totalMl = insights.totalPeriodMl,
                  unit = unit,
                )
              }
            }
          }
        }
      }

      // Contextual Hydration Guidance Card
      item {
        val guidance = buildContextualGuidance(insights.goalCompletionPercent, insights.beverageBreakdown.firstOrNull()?.beverage, unit, insights.dailyAverageMl)
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 24.dp,
          elevated = true,
          contentPadding = PaddingValues(24.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = CrystallineAqua,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = "Fluidity Insight",
              style = MaterialTheme.typography.titleMedium,
              color = CrystallineAqua,
            )
          }
          Spacer(Modifier.height(10.dp))
          Text(
            text = guidance,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceIce,
          )
        }
      }
    }
  }
}

@Composable
private fun MetricGlassTile(
  label: String,
  value: String,
  subtext: String,
  modifier: Modifier = Modifier,
) {
  GlassCard(
    modifier = modifier,
    cornerRadius = 24.dp,
    contentPadding = PaddingValues(20.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = CrystallineAqua,
      maxLines = 1,
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
      color = OnSurfaceIce,
      maxLines = 1,
    )
    Spacer(Modifier.height(2.dp))
    Text(
      text = subtext,
      style = MaterialTheme.typography.bodySmall,
      color = OnSurfaceVariantMist,
      maxLines = 1,
    )
  }
}

private fun buildContextualGuidance(
  completionPercent: Int,
  topBeverage: BeverageType?,
  unit: HydrationUnit,
  averageMl: Int,
): String {
  return when {
    averageMl == 0 ->
      "Start with a 250 mL pour right after waking to establish your baseline hydration curve."
    completionPercent >= 80 ->
      "Strong consistency at $completionPercent% goal completion this week. Your steady intake keeps electrolyte balance optimal."
    topBeverage == BeverageType.COFFEE ->
      "Coffee is currently your top logged drink. Pair each espresso or brew with 250 mL of pure water to balance its 80% hydration factor."
    else ->
      "You're averaging ${unit.formatCompact(averageMl)} on active days. Adding one mid-afternoon ${unit.formatCompact(250)} glass can lift your weekly rhythm."
  }
}
