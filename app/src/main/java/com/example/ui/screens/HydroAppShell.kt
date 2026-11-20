package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.auth.signOut
import com.example.data.HydrationRepository
import com.example.data.UiState
import com.example.ui.components.GlassCard
import com.example.ui.components.HydroNavItem
import com.example.ui.components.OceanicBackdrop
import com.example.ui.components.SuspendedBottomDock
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassDockBackdrop
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist
import com.example.ui.viewmodel.HydrationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

const val ROUTE_TODAY = "today"
const val ROUTE_INSIGHTS = "insights"
const val ROUTE_SETTINGS = "settings"

@Composable
fun HydroAppShell(
  currentUserId: String,
  userDisplayName: String?,
  userEmail: String?,
  onSignedOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  val viewModel: HydrationViewModel =
    viewModel(
      key = currentUserId,
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            HydrationViewModel(
              repository = HydrationRepository(db),
              currentUserId = currentUserId,
              initialDisplayName = userDisplayName,
            )
          }
        },
    )

  val dashboardUi by viewModel.dashboardState.collectAsStateWithLifecycle()
  val selectedBeverage by viewModel.selectedBeverage.collectAsStateWithLifecycle()
  val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
  val pulseCount by viewModel.pulseCount.collectAsStateWithLifecycle()

  LaunchedEffect(actionMessage) {
    if (actionMessage != null) {
      delay(2600)
      viewModel.clearActionMessage()
    }
  }

  val navItems = remember {
    listOf(
      HydroNavItem(
        route = ROUTE_TODAY,
        label = "Sanctuary",
        icon = Icons.Filled.WaterDrop,
        testTag = "nav_today",
      ),
      HydroNavItem(
        route = ROUTE_INSIGHTS,
        label = "Rhythm",
        icon = Icons.Filled.Insights,
        testTag = "nav_insights",
      ),
      HydroNavItem(
        route = ROUTE_SETTINGS,
        label = "Ritual",
        icon = Icons.Filled.Tune,
        testTag = "nav_settings",
      ),
    )
  }

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val navigateToTab: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  OceanicBackdrop(
    modifier = modifier.fillMaxSize(),
    pulseIntensity = if (pulseCount > 0) 1.25f else 1f,
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isWideLayout = maxWidth >= 600.dp

      Row(
        modifier =
          Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
      ) {
        if (isWideLayout) {
          NavigationRail(
            containerColor = GlassDockBackdrop.copy(alpha = 0.72f),
            contentColor = OnSurfaceIce,
            modifier = Modifier.fillMaxHeight().width(88.dp),
          ) {
            Spacer(Modifier.height(20.dp))
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp),
                  )
                },
                label = {
                  Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                colors =
                  NavigationRailItemDefaults.colors(
                    selectedIconColor = CrystallineAqua,
                    selectedTextColor = CrystallineAqua,
                    indicatorColor = Color(0x2E4CD6FB),
                    unselectedIconColor = OnSurfaceVariantMist,
                    unselectedTextColor = OnSurfaceVariantMist,
                  ),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          when (val state = dashboardUi) {
            UiState.Loading -> {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  CircularProgressIndicator(color = OceanicCyan, strokeWidth = 3.dp)
                  Spacer(Modifier.height(14.dp))
                  Text(
                    text = "Preparing your sanctuary...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariantMist,
                  )
                }
              }
            }

            is UiState.Error -> {
              Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
              ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "Unable to sync hydration data",
                    style = MaterialTheme.typography.titleLarge,
                    color = ErrorCoral,
                  )
                  Spacer(Modifier.height(8.dp))
                  Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariantMist,
                  )
                }
              }
            }

            is UiState.Success -> {
              val dashboard = state.data
              NavHost(
                navController = navController,
                startDestination = ROUTE_TODAY,
                modifier = Modifier.fillMaxSize(),
              ) {
                composable(ROUTE_TODAY) {
                  TodayScreen(
                    dashboard = dashboard,
                    selectedBeverage = selectedBeverage,
                    isWideLayout = isWideLayout,
                    onSelectBeverage = viewModel::selectBeverage,
                    onQuickAdd = viewModel::quickAdd,
                    onCustomAdd = viewModel::logIntake,
                    onUpdateLog = viewModel::updateIntakeLog,
                    onDeleteLog = viewModel::deleteIntakeLog,
                  )
                }
                composable(ROUTE_INSIGHTS) {
                  InsightsScreen(
                    dashboard = dashboard,
                    isWideLayout = isWideLayout,
                  )
                }
                composable(ROUTE_SETTINGS) {
                  SettingsScreen(
                    dashboard = dashboard,
                    userEmail = userEmail,
                    isWideLayout = isWideLayout,
                    onSavePreferences = viewModel::updatePreferences,
                    onSignOut = {
                      signOut(
                        credentialManager = credentialManager,
                        scope = scope,
                        onSignOutComplete = onSignedOut,
                      )
                    },
                  )
                }
              }
            }
          }
        }
      }

      // Floating Feedback Pill Toast
      AnimatedVisibility(
        visible = actionMessage != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 12.dp, start = 20.dp, end = 20.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = GlassDockBackdrop,
          border = BorderStroke(1.dp, CrystallineAqua.copy(alpha = 0.55f)),
          shadowElevation = 10.dp,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.WaterDrop,
              contentDescription = null,
              tint = CrystallineAqua,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = actionMessage.orEmpty(),
              style = MaterialTheme.typography.labelLarge,
              color = OnSurfaceIce,
            )
          }
        }
      }

      // Suspended Floating Bottom Dock (Compact Portrait/Mobile)
      if (!isWideLayout) {
        SuspendedBottomDock(
          items = navItems,
          currentRoute = currentRoute,
          onNavigate = navigateToTab,
          modifier = Modifier.align(Alignment.BottomCenter),
        )
      }
    }
  }
}
