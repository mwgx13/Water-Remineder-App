package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BeverageType
import com.example.data.HydrationUnit
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.theme.PrimaryCyan

data class DailyHydrationPoint(
  val dayKey: String,
  val shortLabel: String,
  val effectiveMl: Int,
  val rawMl: Int,
  val goalMl: Int,
)

@Composable
fun HydrationTelemetryChart(
  points: List<DailyHydrationPoint>,
  selectedDayKey: String?,
  unit: HydrationUnit,
  onSelectDay: (DailyHydrationPoint) -> Unit,
  modifier: Modifier = Modifier,
) {
  val maxGoal = points.maxOfOrNull { it.goalMl } ?: 2500
  val maxValue = maxOf(maxGoal, points.maxOfOrNull { it.effectiveMl } ?: 0).coerceAtLeast(1000)
  val chartMax = (maxValue * 1.12f)

  Column(modifier = modifier.fillMaxWidth()) {
    // Dashed goal threshold legend
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Daily Effective Intake",
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceVariantMist,
      )
      Text(
        text = "Target: ${unit.formatCompact(maxGoal)}",
        style = MaterialTheme.typography.labelMedium,
        color = CrystallineAqua,
      )
    }

    Spacer(Modifier.height(16.dp))

    Box(modifier = Modifier.fillMaxWidth().height(168.dp)) {
      Canvas(modifier = Modifier.matchParentSize()) {
        val goalRatio = (maxGoal.toFloat() / chartMax).coerceIn(0.05f, 0.95f)
        val goalY = size.height * (1f - goalRatio)
        drawLine(
          color = CrystallineAqua.copy(alpha = 0.45f),
          start = Offset(0f, goalY),
          end = Offset(size.width, goalY),
          strokeWidth = 1.5.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
        )

        if (points.isNotEmpty()) {
          val slotWidth = size.width / points.size
          val barWidth = (slotWidth * 0.46f).coerceIn(10.dp.toPx(), 28.dp.toPx())
          points.forEachIndexed { idx, point ->
            val centerX = slotWidth * idx + slotWidth / 2f
            val fillRatio = (point.effectiveMl.toFloat() / chartMax).coerceIn(0f, 1f)
            val barHeight = (size.height * fillRatio).coerceAtLeast(
              if (point.effectiveMl > 0) 8.dp.toPx() else 4.dp.toPx()
            )
            val topY = size.height - barHeight
            val isGoalMet = point.effectiveMl >= point.goalMl && point.goalMl > 0
            val isSelected = point.dayKey == selectedDayKey

            // Track background capsule
            drawRoundRect(
              color = Color(0x14FFFFFF),
              topLeft = Offset(centerX - barWidth / 2f, 0f),
              size = Size(barWidth, size.height),
              cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )

            // Luminous active glow when selected or goal met
            if (isSelected || isGoalMet) {
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors =
                      listOf(
                        OceanicCyan.copy(alpha = if (isSelected) 0.35f else 0.18f),
                        Color.Transparent,
                      ),
                    center = Offset(centerX, topY + barHeight * 0.3f),
                    radius = barWidth * 1.4f,
                  ),
                radius = barWidth * 1.4f,
                center = Offset(centerX, topY + barHeight * 0.3f),
              )
            }

            // Filled capsule bar
            val barBrush =
              if (point.effectiveMl == 0) {
                Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x10FFFFFF)))
              } else if (isGoalMet || isSelected) {
                Brush.verticalGradient(listOf(CrystallineAqua, OceanicCyan))
              } else {
                Brush.verticalGradient(
                  listOf(PrimaryCyan.copy(alpha = 0.72f), OceanicCyan.copy(alpha = 0.38f))
                )
              }

            drawRoundRect(
              brush = barBrush,
              topLeft = Offset(centerX - barWidth / 2f, topY),
              size = Size(barWidth, barHeight),
              cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
          }
        }
      }

      // Interactive invisible tap columns matching bar positions
      Row(modifier = Modifier.matchParentSize()) {
        points.forEach { point ->
          Box(
            modifier =
              Modifier
                .weight(1f)
                .height(168.dp)
                .clickable { onSelectDay(point) }
          )
        }
      }
    }

    Spacer(Modifier.height(10.dp))

    // Day axis labels
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      points.forEachIndexed { idx, point ->
        val showLabel = points.size <= 7 || idx % 5 == 0 || idx == points.lastIndex
        val isSelected = point.dayKey == selectedDayKey
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.Center,
        ) {
          if (showLabel) {
            Text(
              text = point.shortLabel,
              style = MaterialTheme.typography.labelSmall,
              color = if (isSelected) CrystallineAqua else OnSurfaceVariantMist,
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

@Composable
fun BeverageDistributionBar(
  beverage: BeverageType,
  volumeMl: Int,
  totalMl: Int,
  unit: HydrationUnit,
  modifier: Modifier = Modifier,
) {
  val ratio = if (totalMl > 0) (volumeMl.toFloat() / totalMl.toFloat()).coerceIn(0f, 1f) else 0f
  val pct = (ratio * 100f).toInt()

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(beverage.accentColor)
        )
        Text(
          text = beverage.label,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = OnSurfaceIce,
        )
      }
      Text(
        text = "${unit.formatCompact(volumeMl)} · $pct%",
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceVariantMist,
      )
    }

    Spacer(Modifier.height(6.dp))

    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(CircleShape)
          .background(Color(0x16FFFFFF))
          .border(0.5.dp, Color(0x18FFFFFF), CircleShape)
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth(ratio.coerceAtLeast(0.03f))
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
              Brush.horizontalGradient(
                listOf(beverage.accentColor.copy(alpha = 0.7f), beverage.accentColor)
              )
            )
      )
    }
  }
}
