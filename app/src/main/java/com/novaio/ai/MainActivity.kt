package com.novaio.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private val Bg = Color(0xFF080D18)
private val Panel = Color(0xFF121C2C)
private val Mint = Color(0xFF35E0B5)
private val Muted = Color(0xFF9AAAC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NovaApp() }
    }
}

@Composable
fun NovaApp() {
    var address by remember { mutableStateOf("http://192.168.4.1") }
    var status by remember { mutableStateOf("Local device not connected") }
    var response by remember { mutableStateOf("Connect to an ESP32 / ESP8266 local endpoint to read telemetry.") }
    var temperature by remember { mutableStateOf("--") }
    var humidity by remember { mutableStateOf("--") }
    var soil by remember { mutableStateOf("--") }
    var water by remember { mutableStateOf("--") }
    var selected by remember { mutableStateOf("Dashboard") }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Mint, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(Modifier.fillMaxSize().background(Bg).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("NOVA", color = Mint, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("IoT AI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                }
                Surface(color = Color(0xFF12372F), shape = RoundedCornerShape(50)) {
                    Text("LOCAL • READY", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Mint, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Dashboard", "Devices", "AI Lab").forEach {
                    FilterChip(selected = selected == it, onClick = { selected = it }, label = { Text(it) })
                }
            }
            Spacer(Modifier.height(14.dp))
            when (selected) {
                "Dashboard" -> {
                    Text("LIVE OVERVIEW", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Temperature", temperature, "°C", Modifier.weight(1f))
                        MetricCard("Humidity", humidity, "%", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Soil moisture", soil, "%", Modifier.weight(1f))
                        MetricCard("Water level", water, "%", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("LOCAL CONNECTION", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(address, { address = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("ESP32 / ESP8266 base URL") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            status = "Connecting…"
                            val result = fetchTelemetry(address)
                            if (result.startsWith("ERROR:")) {
                                status = "Not connected"
                                response = result
                            } else {
                                status = "Connected to local device"
                                response = result
                                temperature = jsonValue(result, "temperature")
                                humidity = jsonValue(result, "humidity")
                                soil = jsonValue(result, "soil")
                                water = jsonValue(result, "water")
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("CONNECT & REFRESH") }
                    Text(status, color = Mint, modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp)
                    Text(response, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
                "Devices" -> {
                    Text("DEVICE MANAGER", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))
                    InfoCard("ESP32 / ESP8266", "Connect your phone and board to the same Wi-Fi or mobile hotspot. Enter the board's local HTTP address in Dashboard.")
                    InfoCard("Bluetooth", "Bluetooth device discovery and data transport are planned for the next build. This starter build does not yet implement BLE pairing.")
                    InfoCard("Cloud sync", "Online remote access needs a cloud endpoint or broker. Local Wi-Fi telemetry works without internet.")
                }
                else -> {
                    Text("OFFLINE AI LAB", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))
                    InfoCard("Model manager — coming in next build", "Choose and download a compatible English language model. After model integration, chat can run locally without internet.")
                    InfoCard("Important", "Offline AI needs device storage and RAM. The model size determines speed and answer quality. This starter APK does not include an AI model yet.")
                }
            }
            Spacer(Modifier.weight(1f))
            Text("NOVA IoT AI  •  v1.0 starter", color = Color(0xFF65758D), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = Panel, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(unit, color = Mint, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
            }
        }
    }
}

@Composable
fun InfoCard(title: String, body: String) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Mint, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Muted, fontSize = 12.sp)
        }
    }
}

suspend fun fetchTelemetry(base: String): String = withContext(Dispatchers.IO) {
    try {
        val clean = base.trim().trimEnd('/')
        val conn = (URL("$clean/telemetry").openConnection() as HttpURLConnection)
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.requestMethod = "GET"
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        text
    } catch (e: Exception) {
        "ERROR: ${e.message ?: "Connection failed"}. Check the board IP and same Wi-Fi/hotspot."
    }
}

fun jsonValue(raw: String, key: String): String {
    val regex = Regex(""""$key"\s*:\s*"?(-?[0-9]+(?:\.[0-9]+)?)""")
    return regex.find(raw)?.groupValues?.getOrNull(1) ?: "--"
}
