package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AbyssSurfaceLowest
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.CyanGlowAmbient
import com.example.ui.theme.CyanGlowSoft
import com.example.ui.theme.GlassDockBackdrop
import com.example.ui.theme.GlassFillChipResting
import com.example.ui.theme.GlassFillLevel1
import com.example.ui.theme.GlassFillLevel2
import com.example.ui.theme.GlassStrokeBottomRight
import com.example.ui.theme.GlassStrokeTopLeft
import com.example.ui.theme.OceanBottom
import com.example.ui.theme.OceanMid
import com.example.ui.theme.OceanTop
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist

@Composable
fun OceanicBackdrop(
  modifier: Modifier = Modifier,
  pulseIntensity: Float = 1f,
  content: @Composable BoxScope.() -> Unit,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ocean_ambient")
  val drift by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "ambient_drift",
    )

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          brush =
            Brush.verticalGradient(
              0.0f to OceanTop,
              0.55f to OceanMid,
              1.0f to OceanBottom,
            )
        )
        .drawBehind {
          val w = size.width
          val h = size.height
          val topGlowCenter = Offset(w * (0.22f + 0.10f * drift), h * (0.14f + 0.04f * drift))
          drawCircle(
            brush =
              Brush.radialGradient(
                colors =
                  listOf(
                    OceanicCyan.copy(alpha = 0.16f * pulseIntensity),
                    CyanGlowAmbient.copy(alpha = 0.05f),
                    Color.Transparent,
                  ),
                center = topGlowCenter,
                radius = w * 0.78f,
              ),
            radius = w * 0.78f,
            center = topGlowCenter,
          )

          val lowerGlowCenter = Offset(w * (0.82f - 0.12f * drift), h * (0.72f - 0.05f * drift))
          drawCircle(
            brush =
              Brush.radialGradient(
                colors =
                  listOf(
                    CrystallineAqua.copy(alpha = 0.10f * pulseIntensity),
                    Color.Transparent,
                  ),
                center = lowerGlowCenter,
                radius = w * 0.65f,
              ),
            radius = w * 0.65f,
            center = lowerGlowCenter,
          )
        },
    content = content,
  )
}

@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 24.dp,
  elevated: Boolean = false,
  contentPadding: PaddingValues = PaddingValues(24.dp),
  content: @Composable ColumnScope.() -> Unit,
) {
  val shape: Shape = RoundedCornerShape(cornerRadius)
  val fillColor = if (elevated) GlassFillLevel2 else GlassFillLevel1
  val strokeBrush =
    Brush.linearGradient(
      colors =
        if (elevated) {
          listOf(Color(0x59FFFFFF), Color(0x1EFFFFFF))
        } else {
          listOf(GlassStrokeTopLeft, GlassStrokeBottomRight)
        }
    )

  Surface(
    modifier = modifier,
    shape = shape,
    color = Color.Transparent,
    border = BorderStroke(1.dp, strokeBrush),
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier =
        Modifier
          .background(
            brush =
              Brush.verticalGradient(
                colors = listOf(fillColor, fillColor.copy(alpha = fillColor.alpha * 0.65f))
              ),
            shape = shape,
          )
          .padding(contentPadding),
      content = content,
    )
  }
}

@Composable
fun GlassHairlineDivider(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(
          brush =
            Brush.horizontalGradient(
              colors =
                listOf(
                  Color.Transparent,
                  Color(0x28FFFFFF),
                  Color(0x28FFFFFF),
                  Color.Transparent,
                )
            )
        )
  )
}

@Composable
fun FluidPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: ImageVector? = null,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed && enabled) 0.96f else 1f,
      animationSpec = spring(dampingRatio = 0.6f, stiffness = 450f),
      label = "fluid_btn_scale",
    )

  Box(
    modifier =
      modifier
        .scale(scale)
        .height(56.dp)
        .minimumInteractiveComponentSize()
        .drawBehind {
          if (enabled) {
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors = listOf(OceanicCyan.copy(alpha = 0.35f), Color.Transparent),
                  center = Offset(size.width / 2f, size.height * 0.65f),
                  radius = size.width * 0.65f,
                ),
              radius = size.width * 0.65f,
              center = Offset(size.width / 2f, size.height * 0.65f),
            )
          }
        }
        .clip(CircleShape)
        .background(
          brush =
            if (enabled) {
              Brush.horizontalGradient(listOf(OceanicCyan, CrystallineAqua))
            } else {
              Brush.horizontalGradient(listOf(Color(0x334CD6FB), Color(0x3390E0EF)))
            }
        )
        .border(
          width = 1.dp,
          brush =
            Brush.verticalGradient(
              listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))
            ),
          shape = CircleShape,
        )
        .clickable(
          enabled = enabled,
          interactionSource = interactionSource,
          indication = null,
          role = Role.Button,
          onClick = onClick,
        )
        .padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = OceanBottom,
          modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = OceanBottom,
      )
    }
  }
}

@Composable
fun FluidSelectorChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  dotColor: Color? = null,
) {
  val bgColor by
    animateColorAsState(
      targetValue = if (selected) CrystallineAqua else GlassFillChipResting,
      animationSpec = tween(220),
      label = "chip_bg",
    )
  val textColor by
    animateColorAsState(
      targetValue = if (selected) OceanBottom else OnSurfaceIce,
      animationSpec = tween(220),
      label = "chip_text",
    )

  Surface(
    modifier =
      modifier
        .height(40.dp)
        .minimumInteractiveComponentSize()
        .then(
          if (selected) {
            Modifier.drawBehind {
              drawCircle(
                brush =
                  Brush.radialGradient(
                    colors = listOf(OceanicCyan.copy(alpha = 0.35f), Color.Transparent),
                    radius = size.maxDimension * 0.7f,
                  ),
                radius = size.maxDimension * 0.7f,
              )
            }
          } else {
            Modifier
          }
        ),
    shape = CircleShape,
    color = bgColor,
    border =
      BorderStroke(
        width = 1.dp,
        color = if (selected) CrystallineAqua else Color(0x28FFFFFF),
      ),
    onClick = onClick,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (dotColor != null) {
        Box(
          modifier =
            Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (selected) OceanBottom else dotColor)
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = textColor,
      )
    }
  }
}

@Composable
fun DropletQuickAddButton(
  volumeLabel: String,
  subLabel: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  highlighted: Boolean = false,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.92f else 1f,
      animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
      label = "droplet_scale",
    )

  Surface(
    modifier =
      modifier
        .scale(scale)
        .minimumInteractiveComponentSize()
        .drawBehind {
          drawCircle(
            brush =
              Brush.radialGradient(
                colors =
                  listOf(
                    if (highlighted) OceanicCyan.copy(alpha = 0.32f) else CyanGlowSoft,
                    Color.Transparent,
                  ),
                radius = size.maxDimension * 0.72f,
              ),
            radius = size.maxDimension * 0.72f,
          )
        },
    shape = RoundedCornerShape(24.dp),
    color = Color.Transparent,
    border =
      BorderStroke(
        width = 1.dp,
        brush =
          Brush.linearGradient(
            if (highlighted) {
              listOf(CrystallineAqua.copy(alpha = 0.75f), OceanicCyan.copy(alpha = 0.25f))
            } else {
              listOf(GlassStrokeTopLeft, GlassStrokeBottomRight)
            }
          ),
      ),
    onClick = onClick,
    interactionSource = interactionSource,
  ) {
    Column(
      modifier =
        Modifier
          .background(
            brush =
              Brush.verticalGradient(
                if (highlighted) {
                  listOf(Color(0x3300B4D8), Color(0x1800B4D8))
                } else {
                  listOf(GlassFillLevel2, GlassFillLevel1)
                }
              )
          )
          .padding(horizontal = 12.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = if (highlighted) CrystallineAqua else OceanicCyan,
        modifier = Modifier.size(18.dp),
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = volumeLabel,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = OnSurfaceIce,
        maxLines = 1,
      )
      Text(
        text = subLabel,
        style = MaterialTheme.typography.labelSmall,
        color = OnSurfaceVariantMist,
        maxLines = 1,
      )
    }
  }
}

data class HydroNavItem(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val testTag: String,
)

@Composable
fun SuspendedBottomDock(
  items: List<HydroNavItem>,
  currentRoute: String?,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(horizontal = 24.dp)
        .padding(bottom = 20.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier =
        Modifier
          .widthIn(max = 420.dp)
          .fillMaxWidth()
          .drawBehind {
            drawRoundRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(OceanicCyan.copy(alpha = 0.22f), Color.Transparent),
                  radius = size.width * 0.65f,
                )
            )
          },
      shape = CircleShape,
      color = GlassDockBackdrop,
      border =
        BorderStroke(
          width = 1.dp,
          brush = Brush.linearGradient(listOf(Color(0x59FFFFFF), Color(0x1AFFFFFF))),
        ),
      shadowElevation = 16.dp,
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          val selected = currentRoute == item.route
          val pillColor by
            animateColorAsState(
              targetValue = if (selected) Color(0x2E4CD6FB) else Color.Transparent,
              animationSpec = tween(220),
              label = "dock_pill",
            )
          val contentColor by
            animateColorAsState(
              targetValue = if (selected) CrystallineAqua else OnSurfaceVariantMist,
              animationSpec = tween(220),
              label = "dock_tint",
            )

          Row(
            modifier =
              Modifier
                .testTag(item.testTag)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(pillColor)
                .then(
                  if (selected) {
                    Modifier.border(
                      1.dp,
                      CrystallineAqua.copy(alpha = 0.35f),
                      CircleShape,
                    )
                  } else {
                    Modifier
                  }
                )
                .clickable(
                  role = Role.Tab,
                  onClick = { onNavigate(item.route) },
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = item.icon,
              contentDescription = item.label,
              tint = contentColor,
              modifier = Modifier.size(20.dp),
            )
            if (selected) {
              Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun RecessedDarkInputContainer(
  modifier: Modifier = Modifier,
  isFocused: Boolean = false,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(16.dp))
        .background(AbyssSurfaceLowest.copy(alpha = 0.72f))
        .border(
          width = 1.dp,
          color = if (isFocused) OceanicCyan else Color(0x28FFFFFF),
          shape = RoundedCornerShape(16.dp),
        )
        .padding(horizontal = 16.dp, vertical = 12.dp),
    content = content,
  )
}
