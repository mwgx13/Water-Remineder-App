package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BeverageType
import com.example.data.HydrationUnit
import com.example.ui.theme.AbyssSurfaceLow
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.GlassFillLevel1
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.theme.OutlineMuted
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIntakeBottomSheet(
  initialAmountMl: Int,
  initialBeverage: BeverageType,
  initialNote: String,
  unit: HydrationUnit,
  title: String,
  ctaLabel: String,
  onDismiss: () -> Unit,
  onConfirm: (amountMl: Int, beverageType: BeverageType, note: String?) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var amountMl by remember { mutableIntStateOf(initialAmountMl.coerceIn(50, 2000)) }
  var selectedBeverage by remember { mutableStateOf(initialBeverage) }
  var noteText by remember { mutableStateOf(initialNote.take(140)) }
  val effectiveMl = selectedBeverage.computeEffectiveMl(amountMl)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = AbyssSurfaceLow,
    contentColor = OnSurfaceIce,
    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 28.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = OnSurfaceIce,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text =
          "Effective hydration: ${unit.formatCompact(effectiveMl)} (${(selectedBeverage.hydrationFactor * 100).roundToInt()}% factor)",
        style = MaterialTheme.typography.bodySmall,
        color = CrystallineAqua,
      )

      Spacer(Modifier.height(20.dp))

      // Volume Stepper Display
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          modifier = Modifier.size(48.dp).minimumInteractiveComponentSize().testTag("stepper_minus"),
          shape = CircleShape,
          color = GlassFillLevel1,
          border = BorderStroke(1.dp, Color(0x33FFFFFF)),
          onClick = { amountMl = (amountMl - 50).coerceAtLeast(50) },
        ) {
          Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = "Decrease volume",
            tint = OnSurfaceIce,
            modifier = Modifier.padding(12.dp),
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = unit.formatCompact(amountMl),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = OnSurfaceIce,
          )
          Text(
            text = "${selectedBeverage.label}",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariantMist,
          )
        }

        Surface(
          modifier = Modifier.size(48.dp).minimumInteractiveComponentSize().testTag("stepper_plus"),
          shape = CircleShape,
          color = GlassFillLevel1,
          border = BorderStroke(1.dp, Color(0x33FFFFFF)),
          onClick = { amountMl = (amountMl + 50).coerceAtMost(2000) },
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Increase volume",
            tint = OnSurfaceIce,
            modifier = Modifier.padding(12.dp),
          )
        }
      }

      Spacer(Modifier.height(12.dp))

      Slider(
        value = amountMl.toFloat(),
        onValueChange = { raw ->
          val snapped = ((raw / 25f).roundToInt() * 25).coerceIn(50, 2000)
          amountMl = snapped
        },
        valueRange = 50f..1500f,
        colors =
          SliderDefaults.colors(
            thumbColor = CrystallineAqua,
            activeTrackColor = OceanicCyan,
            inactiveTrackColor = Color(0x28FFFFFF),
          ),
      )

      Spacer(Modifier.height(16.dp))

      Text(
        text = "Beverage",
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceVariantMist,
      )
      Spacer(Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        BeverageType.entries.forEach { beverage ->
          FluidSelectorChip(
            label = beverage.shortLabel,
            selected = selectedBeverage == beverage,
            dotColor = beverage.accentColor,
            onClick = { selectedBeverage = beverage },
          )
        }
      }

      Spacer(Modifier.height(18.dp))

      Text(
        text = "Ritual Note (Optional)",
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceVariantMist,
      )
      Spacer(Modifier.height(8.dp))

      RecessedDarkInputContainer(
        modifier = Modifier.fillMaxWidth(),
        isFocused = noteText.isNotEmpty(),
      ) {
        if (noteText.isEmpty()) {
          Text(
            text = "e.g., After morning run, chilled with lemon",
            style = MaterialTheme.typography.bodyMedium,
            color = OutlineMuted,
          )
        }
        BasicTextField(
          value = noteText,
          onValueChange = { if (it.length <= 140) noteText = it },
          textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceIce),
          cursorBrush = SolidColor(OceanicCyan),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("custom_note_input"),
        )
      }

      Spacer(Modifier.height(24.dp))

      FluidPrimaryButton(
        text = ctaLabel,
        leadingIcon = Icons.Filled.WaterDrop,
        onClick = { onConfirm(amountMl, selectedBeverage, noteText.trim().takeIf { it.isNotEmpty() }) },
        modifier = Modifier.fillMaxWidth().testTag("confirm_custom_log_button"),
      )
    }
  }
}
