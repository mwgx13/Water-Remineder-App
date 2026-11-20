package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BeverageType
import com.example.data.HydrationLog
import com.example.data.HydrationUnit
import com.example.ui.components.CustomIntakeBottomSheet
import com.example.ui.components.DropletQuickAddButton
import com.example.ui.components.FluidSelectorChip
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.HydrationWaveOrb
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassFillLevel1
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.theme.PrimaryCyan
import com.example.ui.viewmodel.SanctuaryDashboardState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
  dashboard: SanctuaryDashboardState,
  selectedBeverage: BeverageType,
  isWideLayout: Boolean,
  onSelectBeverage: (BeverageType) -> Unit,
  onQuickAdd: (Int) -> Unit,
  onCustomAdd: (amountMl: Int, beverage: BeverageType, note: String?) -> Unit,
  onUpdateLog: (logId: String, amountMl: Int, beverage: BeverageType, note: String?) -> Unit,
  onDeleteLog: (logId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val profile = dashboard.profile
  val today = dashboard.today
  val unit = profile.preferredUnit

  var showAddSheet by remember { mutableStateOf(false) }
  var editingLog by remember { mutableStateOf<HydrationLog?>(null) }

  val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

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
      // Sanctuary Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = today.todayDateLabel.uppercase(Locale.getDefault()),
              style = MaterialTheme.typography.labelSmall,
              color = CrystallineAqua,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = "Hello, ${profile.displayName.substringBefore(" ")}",
              style = MaterialTheme.typography.headlineLarge,
              color = OnSurfaceIce,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Streak pill
            Surface(
              shape = CircleShape,
              color = Color(0x2200B4D8),
              border = BorderStroke(1.dp, CrystallineAqua.copy(alpha = 0.4f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Bolt,
                  contentDescription = "Current streak",
                  tint = PrimaryCyan,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = "${today.currentStreakDays}d",
                  style = MaterialTheme.typography.labelLarge,
                  color = CrystallineAqua,
                )
              }
            }
          }
        }
      }

      // Hero Wave Orb + Pacing Rhythm Badge
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 32.dp,
          contentPadding = PaddingValues(24.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            HydrationWaveOrb(
              currentEffectiveMl = today.effectiveMl,
              goalMl = today.goalMl,
              unit = unit,
              modifier = Modifier.fillMaxWidth(0.78f),
            )

            Spacer(Modifier.height(16.dp))
            GlassHairlineDivider()
            Spacer(Modifier.height(14.dp))

            val delta = today.effectiveMl - today.pacingTargetMl
            val pacingText =
              when {
                today.effectiveMl >= today.goalMl -> "Sanctuary complete — steady maintenance"
                delta >= 150 -> "Ahead of rhythm by ${unit.formatCompact(delta)}"
                delta >= -150 -> "In rhythm with your ${profile.wakeHour}:00–${profile.sleepHour}:00 window"
                else -> "Pace check: ${unit.formatCompact(-delta)} behind midday flow"
              }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "RHYTHM PACING",
                  style = MaterialTheme.typography.labelSmall,
                  color = OnSurfaceVariantMist,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                  text = pacingText,
                  style = MaterialTheme.typography.bodySmall,
                  color = CrystallineAqua,
                )
              }
              Text(
                text = "Raw ${unit.formatCompact(today.rawMl)}",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariantMist,
              )
            }
          }
        }
      }

      // Beverage Selector Chips + Droplet Quick-Add Dock
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "ACTIVE BEVERAGE",
            style = MaterialTheme.typography.labelSmall,
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
                onClick = { onSelectBeverage(beverage) },
                modifier = Modifier.testTag("beverage_chip_${beverage.name.lowercase()}"),
              )
            }
          }

          Spacer(Modifier.height(16.dp))

          val smallMl = profile.quickPresetSmallMl.toInt()
          val mediumMl = profile.quickPresetMediumMl.toInt()
          val largeMl = profile.quickPresetLargeMl.toInt()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            DropletQuickAddButton(
              volumeLabel = "+${formatQuickLabel(smallMl, unit)}",
              subLabel = selectedBeverage.shortLabel,
              onClick = { onQuickAdd(smallMl) },
              modifier = Modifier.weight(1f).testTag("quick_add_small"),
            )
            DropletQuickAddButton(
              volumeLabel = "+${formatQuickLabel(mediumMl, unit)}",
              subLabel = selectedBeverage.shortLabel,
              onClick = { onQuickAdd(mediumMl) },
              highlighted = true,
              modifier = Modifier.weight(1f).testTag("quick_add_medium"),
            )
            DropletQuickAddButton(
              volumeLabel = "+${formatQuickLabel(largeMl, unit)}",
              subLabel = selectedBeverage.shortLabel,
              onClick = { onQuickAdd(largeMl) },
              modifier = Modifier.weight(1f).testTag("quick_add_large"),
            )
            DropletQuickAddButton(
              volumeLabel = "Custom",
              subLabel = "Amount",
              onClick = { showAddSheet = true },
              modifier = Modifier.weight(1f).testTag("quick_add_custom"),
            )
          }
        }
      }

      // Today's Ritual Log Card
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
                text = "Today's Ritual Log",
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceIce,
              )
              Text(
                text = "${today.todayLogs.size} ${if (today.todayLogs.size == 1) "pour" else "pours"} recorded",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantMist,
              )
            }

            Surface(
              shape = CircleShape,
              color = Color(0x224CD6FB),
              border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.45f)),
              onClick = { showAddSheet = true },
              modifier = Modifier.testTag("add_log_pill_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Add,
                  contentDescription = "Add custom intake",
                  tint = CrystallineAqua,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Log",
                  style = MaterialTheme.typography.labelMedium,
                  color = CrystallineAqua,
                )
              }
            }
          }

          Spacer(Modifier.height(16.dp))

          if (today.todayLogs.isEmpty()) {
            GlassHairlineDivider()
            Spacer(Modifier.height(20.dp))
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0x1E00B4D8)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.WaterDrop,
                  contentDescription = null,
                  tint = CrystallineAqua,
                  modifier = Modifier.size(26.dp),
                )
              }
              Spacer(Modifier.height(12.dp))
              Text(
                text = "Begin your first pour",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceIce,
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = "Tap any droplet quick-log above to start today's fluid wave.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantMist,
              )
            }
          }
        }
      }

      if (today.todayLogs.isNotEmpty()) {
        itemsIndexed(
          items = today.todayLogs,
          key = { _, item -> item.id },
        ) { _, log ->
          IntakeLogRowCard(
            log = log,
            unit = unit,
            timeText = timeFormatter.format(Date(log.loggedAtMillis)),
            onEdit = { editingLog = log },
            onDelete = { onDeleteLog(log.id) },
          )
        }
      }
    }
  }

  if (showAddSheet) {
    CustomIntakeBottomSheet(
      initialAmountMl = profile.quickPresetMediumMl.toInt(),
      initialBeverage = selectedBeverage,
      initialNote = "",
      unit = unit,
      title = "Log Custom Intake",
      ctaLabel = "Add to Sanctuary",
      onDismiss = { showAddSheet = false },
      onConfirm = { amount, bev, note ->
        showAddSheet = false
        onCustomAdd(amount, bev, note)
      },
    )
  }

  val targetEditLog = editingLog
  if (targetEditLog != null) {
    CustomIntakeBottomSheet(
      initialAmountMl = targetEditLog.amountMl.toInt(),
      initialBeverage = targetEditLog.beverage,
      initialNote = targetEditLog.note ?: "",
      unit = unit,
      title = "Edit Hydration Entry",
      ctaLabel = "Save Changes",
      onDismiss = { editingLog = null },
      onConfirm = { amount, bev, note ->
        editingLog = null
        onUpdateLog(targetEditLog.id, amount, bev, note)
      },
    )
  }
}

@Composable
private fun IntakeLogRowCard(
  log: HydrationLog,
  unit: HydrationUnit,
  timeText: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val beverage = log.beverage
  GlassCard(
    modifier = Modifier.fillMaxWidth().testTag("intake_log_card_${log.id}"),
    cornerRadius = 20.dp,
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = beverage.accentColor.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, beverage.accentColor.copy(alpha = 0.4f)),
      ) {
        Icon(
          imageVector = beverageIcon(beverage),
          contentDescription = beverage.label,
          tint = beverage.accentColor,
          modifier = Modifier.padding(10.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = unit.formatCompact(log.amountMl.toInt()),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = OnSurfaceIce,
          )
          if (log.effectiveMl != log.amountMl) {
            Text(
              text = "→ ${unit.formatCompact(log.effectiveMl.toInt())} eff.",
              style = MaterialTheme.typography.labelSmall,
              color = CrystallineAqua,
            )
          }
        }
        Text(
          text = "${beverage.label} · $timeText",
          style = MaterialTheme.typography.bodySmall,
          color = OnSurfaceVariantMist,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (!log.note.isNullOrBlank()) {
          Spacer(Modifier.height(2.dp))
          Text(
            text = log.note,
            style = MaterialTheme.typography.bodySmall,
            color = PrimaryCyan.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(48.dp).testTag("edit_log_${log.id}"),
        ) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit log",
            tint = OnSurfaceVariantMist,
            modifier = Modifier.size(18.dp),
          )
        }
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(48.dp).testTag("delete_log_${log.id}"),
        ) {
          Icon(
            imageVector = Icons.Filled.DeleteOutline,
            contentDescription = "Delete log",
            tint = ErrorCoral.copy(alpha = 0.85f),
            modifier = Modifier.size(19.dp),
          )
        }
      }
    }
  }
}

private fun beverageIcon(beverage: BeverageType): ImageVector =
  when (beverage) {
    BeverageType.WATER -> Icons.Filled.WaterDrop
    BeverageType.SPARKLING -> Icons.Filled.LocalDrink
    BeverageType.HERBAL_TEA -> Icons.Filled.Spa
    BeverageType.GREEN_TEA -> Icons.Filled.Spa
    BeverageType.ELECTROLYTES -> Icons.Filled.Bolt
    BeverageType.COFFEE -> Icons.Filled.Coffee
  }

private fun formatQuickLabel(ml: Int, unit: HydrationUnit): String =
  when (unit) {
    HydrationUnit.ML -> "${ml}mL"
    HydrationUnit.OZ -> "${(ml / HydrationUnit.ML_PER_OZ).toInt()}oz"
  }
