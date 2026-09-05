package com.example.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.osmand.plus.activities.MapActivity

/**
 * Pantalla principal con FAB que abre OsmAnd.
 * El usuario puede descargar mapas, configurar y navegar offline.
 */
@Composable
fun OsmAndMapScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, MapActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Abrir mapa offline OsmAnd"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Aquí puedes poner tu UI de logística/entregas
            // El FAB flota sobre esta pantalla y abre OsmAnd completo
        }
    }
}

/**
 * Botón reutilizable para lanzar OsmAnd desde cualquier pantalla.
 */
@Composable
fun LaunchOsmAndButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    FloatingActionButton(
        onClick = {
            val intent = Intent(context, MapActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        },
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "Mapa offline"
        )
    }
}
