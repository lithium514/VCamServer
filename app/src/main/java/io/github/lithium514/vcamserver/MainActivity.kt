package io.github.lithium514.vcamserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.lithium514.vcamserver.ui.theme.VCamServerTheme
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VCamServerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VCamServerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun VCamServerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var server by remember { mutableStateOf<VCamHttpServer?>(null) }
    var statusMessage by remember { mutableStateOf("Server stopped") }
    var isRunning by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            server?.stop()
        }
    }

    val startServer: () -> Unit = {
        try {
            val newServer = VCamHttpServer(context, 8080)
            newServer.start()
            server = newServer
            isRunning = true
            val ip = VCamHttpServer.getLocalIpAddress()
            statusMessage = "Running at http://$ip:8080\nPOST images to /upload"
        } catch (e: IOException) {
            statusMessage = "Failed to start: ${e.message}"
            isRunning = false
        }
    }

    val stopServer: () -> Unit = {
        server?.stop()
        server = null
        isRunning = false
        statusMessage = "Server stopped"
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "VCam Server",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = statusMessage)
        Spacer(modifier = Modifier.height(16.dp))
        if (isRunning) {
            Button(onClick = stopServer) {
                Text("Stop Server")
            }
        } else {
            Button(onClick = startServer) {
                Text("Start Server")
            }
        }
    }
}
