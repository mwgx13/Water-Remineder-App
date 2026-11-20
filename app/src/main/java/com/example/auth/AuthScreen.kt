package com.example.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.ui.components.FluidPrimaryButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.HydrationWaveOrb
import com.example.ui.components.OceanicBackdrop
import com.example.data.HydrationUnit
import com.example.ui.theme.CrystallineAqua
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.OceanBottom
import com.example.ui.theme.OceanicCyan
import com.example.ui.theme.OnSurfaceIce
import com.example.ui.theme.OnSurfaceVariantMist

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isCheckingAutoSignIn by remember { mutableStateOf(true) }
  var isSigningIn by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {
        isCheckingAutoSignIn = false
        onAuthSuccess()
      },
      onUnauthenticated = { isCheckingAutoSignIn = false },
      scope = scope,
    )
  }

  OceanicBackdrop(modifier = modifier.fillMaxSize()) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 20.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Surface(
          shape = CircleShape,
          color = Color(0x1E00B4D8),
          border = BorderStroke(1.dp, CrystallineAqua.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.WaterDrop,
              contentDescription = null,
              tint = CrystallineAqua,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "PERSONAL HYDRATION SANCTUARY",
              style = MaterialTheme.typography.labelSmall,
              color = CrystallineAqua,
            )
          }
        }

        Spacer(Modifier.height(20.dp))

        Text(
          text = "Hydro Fluidity",
          style = MaterialTheme.typography.displayLarge,
          color = OnSurfaceIce,
          textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(10.dp))

        Text(
          text = "Elevate daily water intake into a serene, restorative ritual with fluid wave telemetry.",
          style = MaterialTheme.typography.bodyLarge,
          color = OnSurfaceVariantMist,
          textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        HydrationWaveOrb(
          currentEffectiveMl = 1850,
          goalMl = 2500,
          unit = HydrationUnit.ML,
          modifier = Modifier.sizeInOrb(),
        )

        Spacer(Modifier.height(28.dp))

        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 28.dp,
        ) {
          Text(
            text = "Sync Your Daily Ritual",
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurfaceIce,
          )
          Spacer(Modifier.height(6.dp))
          Text(
            text = "Sign in with Google to save hydration logs, custom targets, and multi-day streaks securely in the cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantMist,
          )

          if (errorMessage != null) {
            Spacer(Modifier.height(14.dp))
            GlassHairlineDivider()
            Spacer(Modifier.height(12.dp))
            Text(
              text = errorMessage!!,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = ErrorCoral,
            )
          }

          Spacer(Modifier.height(20.dp))

          if (isCheckingAutoSignIn || isSigningIn) {
            Box(
              modifier = Modifier.fillMaxWidth().height(56.dp),
              contentAlignment = Alignment.Center,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                CircularProgressIndicator(
                  color = OceanicCyan,
                  trackColor = OceanBottom,
                  strokeWidth = 2.5.dp,
                  modifier = Modifier.size(22.dp),
                )
                Text(
                  text = if (isCheckingAutoSignIn) "Checking session..." else "Signing in with Google...",
                  style = MaterialTheme.typography.labelLarge,
                  color = CrystallineAqua,
                )
              }
            }
          } else {
            FluidPrimaryButton(
              text = "Sign in with Google",
              leadingIcon = Icons.Filled.AccountCircle,
              onClick = {
                errorMessage = null
                isSigningIn = true
                onGoogleSignInClicked(
                  context = context,
                  credentialManager = credentialManager,
                  onAuthSuccess = {
                    isSigningIn = false
                    onAuthSuccess()
                  },
                  onAuthError = { msg ->
                    isSigningIn = false
                    errorMessage = msg
                  },
                  scope = scope,
                  onAuthCancelled = { isSigningIn = false },
                )
              },
              modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button"),
            )
          }
        }
      }
    }
  }
}

private fun Modifier.sizeInOrb(): Modifier = this.size(220.dp)
