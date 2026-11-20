package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthScreen
import com.example.auth.authStateFlow
import com.example.ui.screens.HydroAppShell
import com.example.ui.theme.HydroFluidityTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    setContent {
      HydroFluidityTheme {
        HydroAppRoot()
      }
    }
  }
}

@Composable
fun HydroAppRoot(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    key(user.uid) {
      HydroAppShell(
        currentUserId = user.uid,
        userDisplayName = user.displayName,
        userEmail = user.email,
        onSignedOut = {},
      )
    }
  }
}
