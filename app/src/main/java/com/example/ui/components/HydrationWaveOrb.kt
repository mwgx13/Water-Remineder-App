package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.HydrationUnit
import com.example.ui.theme.AbyssSurfaceLowest
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.theme.PrimaryCyan
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HydrationWaveOrb(
  currentEffectiveMl: Int,
  goalMl: Int,
  unit: HydrationUnit,
  modifier: Modifier = Modifier,
) {
  val rawProgress = if (goalMl > 0) (currentEffectiveMl.toFloat() / goalMl.toFloat()) else 0f
  val animatedFill by
    animateFloatAsState(
      targetValue = rawProgress.coerceIn(0.04f, 1.0f),
      animationSpec = spring(dampingRatio = 0.72f, stiffness = 140f),
      label = "wave_fill",
    )
  val percentage = (rawProgress * 100f).roundToInt().coerceAtLeast(0)
  val remainingMl = (goalMl - currentEffectiveMl).coerceAtLeast(0)

  val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")
  val phase1 by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 3600, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "wave_phase_1",
    )
  val phase2 by
    infiniteTransition.animateFloat(
      initialValue = (2f * PI).toFloat(),
      targetValue = 0f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 5200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "wave_phase_2",
    )
  val bubbleProgress by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 4400, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "bubble_rise",
    )

  Box(
    modifier =
      modifier
        .sizeIn(maxWidth = 284.dp, maxHeight = 284.dp)
        .aspectRatio(1f),
    contentAlignment = Alignment.Center,
  ) {
    // Outer concentric aura & layered sine wave canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      val radius = size.minDimension / 2f
      val center = Offset(size.width / 2f, size.height / 2f)

      // Outer atmospheric cyan halo
      drawCircle(
        brush =
          Brush.radialGradient(
            colors =
              listOf(
                OceanicCyan.copy(alpha = if (percentage >= 100) 0.34f else 0.22f),
                OceanicCyan.copy(alpha = 0.06f),
                Color.Transparent,
              ),
            center = center,
            radius = radius * 1.15f,
          ),
        radius = radius * 1.15f,
        center = center,
      )

      // Outer concentric glass ring
      drawCircle(
        brush =
          Brush.linearGradient(
            listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))
          ),
        radius = radius * 0.96f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
      )

      val innerRadius = radius * 0.87f
      // Recessed dark sphere backdrop
      drawCircle(
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0xFF0A2239), AbyssSurfaceLowest)
          ),
        radius = innerRadius,
        center = center,
      )

      val circleClip =
        Path().apply {
          addOval(
            androidx.compose.ui.geometry.Rect(
              left = center.x - innerRadius,
              top = center.y - innerRadius,
              right = center.x + innerRadius,
              bottom = center.y + innerRadius,
            )
          )
        }

      clipPath(circleClip) {
        val diameter = innerRadius * 2f
        val topY = center.y + innerRadius - (diameter * animatedFill)
        val amplitude1 = 8.dp.toPx() * (1f - animatedFill * 0.35f).coerceAtLeast(0.4f)
        val amplitude2 = 6.dp.toPx() * (1f - animatedFill * 0.35f).coerceAtLeast(0.4f)
        val startX = center.x - innerRadius
        val endX = center.x + innerRadius
        val step = 6f

        // Back wave (deeper translucent cyan)
        val backPath = Path().apply {
          moveTo(startX, topY)
          var x = startX
          while (x <= endX + step) {
            val normalized = (x - startX) / diameter
            val y = topY + sin(normalized * 2.2f * PI.toFloat() + phase2) * amplitude2
            lineTo(x, y)
            x += step
          }
          lineTo(endX, center.y + innerRadius)
          lineTo(startX, center.y + innerRadius)
          close()
        }
        drawPath(
          path = backPath,
          brush =
            Brush.verticalGradient(
              colors =
                listOf(
                  OceanicCyan.copy(alpha = 0.45f),
                  Color(0xFF033859).copy(alpha = 0.85f),
                ),
              startY = topY - amplitude2,
              endY = center.y + innerRadius,
            ),
        )

        // Front wave (luminous crystalline aqua crest)
        val frontPath = Path().apply {
          moveTo(startX, topY)
          var x = startX
          while (x <= endX + step) {
            val normalized = (x - startX) / diameter
            val y = topY + sin(normalized * 2.5f * PI.toFloat() + phase1) * amplitude1
            lineTo(x, y)
            x += step
          }
          lineTo(endX, center.y + innerRadius)
          lineTo(startX, center.y + innerRadius)
          close()
        }
        drawPath(
          path = frontPath,
          brush =
            Brush.verticalGradient(
              colors =
                listOf(
                  CrystallineAqua.copy(alpha = 0.85f),
                  OceanicCyan.copy(alpha = 0.78f),
                  Color(0xFF062C49).copy(alpha = 0.94f),
                ),
              startY = topY - amplitude1,
              endY = center.y + innerRadius,
            ),
        )

        // Luminous meniscus crest highlight
        val crestPath = Path().apply {
          var x = startX
          var started = false
          while (x <= endX + step) {
            val normalized = (x - startX) / diameter
            val y = topY + sin(normalized * 2.5f * PI.toFloat() + phase1) * amplitude1
            if (!started) {
              moveTo(x, y)
              started = true
            } else {
              lineTo(x, y)
            }
            x += step
          }
        }
        drawPath(
          path = crestPath,
          color = Color(0xAAFFFFFF),
          style = Stroke(width = 1.8.dp.toPx()),
        )

        // Subtle rising air-bubble particles inside liquid
        val bubbleOffsets = listOf(0.24f, 0.48f, 0.68f, 0.82f)
        bubbleOffsets.forEachIndexed { idx, xFraction ->
          val localProgress = (bubbleProgress + idx * 0.27f) % 1f
          val bx = startX + diameter * xFraction + sin(localProgress * 6f) * 4.dp.toPx()
          val by =
            (center.y + innerRadius) -
              localProgress * (diameter * animatedFill * 0.88f).coerceAtLeast(12f)
          if (by > topY + 6.dp.toPx()) {
            drawCircle(
              color = Color.White.copy(alpha = (1f - localProgress) * 0.32f),
              radius = (2.5f + (idx % 2) * 1.4f).dp.toPx(),
              center = Offset(bx, by),
            )
          }
        }
      }

      // Inner crisp glass rim with directional refraction
      drawCircle(
        brush =
          Brush.linearGradient(
            listOf(Color(0x66FFFFFF), Color(0x18FFFFFF))
          ),
        radius = innerRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
      )
    }

    // Center typography readout
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp),
    ) {
      Box(
        modifier =
          Modifier
            .clip(CircleShape)
            .background(Color(0x55020F1E))
            .border(1.dp, CrystallineAqua.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Text(
          text = "$percentage% HYDRATED",
          style = MaterialTheme.typography.labelSmall,
          color = CrystallineAqua,
        )
      }

      Spacer(Modifier.height(10.dp))

      Text(
        text = unit.formatNumberOnly(currentEffectiveMl),
        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
        color = OnSurfaceIce,
      )

      Text(
        text = "/ ${unit.formatVolume(goalMl)}",
        style = MaterialTheme.typography.titleSmall,
        color = OnSurfaceIce.copy(alpha = 0.88f),
      )

      Spacer(Modifier.height(8.dp))

      Text(
        text =
          if (remainingMl == 0) {
            "Daily sanctuary goal reached"
          } else {
            "${unit.formatCompact(remainingMl)} remaining"
          },
        style = MaterialTheme.typography.bodySmall,
        color = if (remainingMl == 0) PrimaryCyan else OnSurfaceVariantMist,
      )
    }
  }
}
