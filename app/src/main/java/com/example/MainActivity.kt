package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainScreen
import com.example.ui.NexFyViewModel
import com.example.ui.Screen
import com.example.ui.theme.NexFyRemesasTheme

class MainActivity : ComponentActivity() {
  
  private var pendingMapUri: Uri? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Check if activity was launched with an intent
    if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
        pendingMapUri = intent.data
    }
    
    enableEdgeToEdge()
    setContent {
      val viewModel: NexFyViewModel = viewModel()
      val settings by viewModel.settings.collectAsState()
      
      // Handle pending geo intent
      pendingMapUri?.let { uri ->
          // Extract lat lng if possible
          val targetLocation = parseGeoUri(uri)
          if (targetLocation != null) {
              viewModel.navigateTo(Screen.OfflineMap(targetLocation = targetLocation))
          }
          pendingMapUri = null
      }
      
      NexFyRemesasTheme(darkTheme = settings.darkMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
      super.onNewIntent(intent)
      if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
          // If we receive a new intent while running, we can just broadcast or update ViewModel.
          // For simplicity, we can let the UI react if we had a shared flow, but since we are re-entering,
          // let's just recreate or access the viewmodel. Since we can't easily access the compose viewmodel here,
          // we could store it or restart. Let's restart the activity to make it simple.
          setIntent(intent)
          recreate()
      }
  }

  private fun parseGeoUri(uri: Uri): Pair<Double, Double>? {
      try {
          if (uri.scheme == "geo") {
              val schemeSpecificPart = uri.schemeSpecificPart
              // geo:lat,lng?q=lat,lng
              val parts = schemeSpecificPart.split("?")
              if (parts.isNotEmpty()) {
                  val latLngStr = parts[0].split(",")
                  if (latLngStr.size >= 2) {
                      val lat = latLngStr[0].toDoubleOrNull()
                      val lng = latLngStr[1].toDoubleOrNull()
                      if (lat != null && lng != null) {
                          return Pair(lat, lng)
                      }
                  }
              }
          } else if (uri.host?.contains("maps.google") == true || uri.host == "goo.gl") {
              // https://maps.google.com/?q=lat,lng
              val q = uri.getQueryParameter("q")
              if (q != null) {
                  val latLngStr = q.split(",")
                  if (latLngStr.size >= 2) {
                      val lat = latLngStr[0].toDoubleOrNull()
                      val lng = latLngStr[1].toDoubleOrNull()
                      if (lat != null && lng != null) {
                          return Pair(lat, lng)
                      }
                  }
              }
          }
      } catch (e: Exception) {
          e.printStackTrace()
      }
      return null
  }
}
