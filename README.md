# Nova IoT AI — Android starter project

Target: Android 10+ (API 29+), Kotlin, Jetpack Compose.

## Current starter features
- Dark dashboard UI
- ESP32 / ESP8266 local HTTP telemetry fetch
- Works on a local Wi-Fi network or phone hotspot without internet, provided phone and board are on the same LAN
- Sensor JSON keys: `temperature`, `humidity`, `soil`, `water`
- Device and AI Lab screens explain planned integrations

## ESP32/ESP8266 local endpoint contract
Run a small HTTP server on the board and expose:
- `GET /telemetry`
- Return JSON such as:
  `{"temperature":28.4,"humidity":71,"soil":42,"water":15}`

For a phone hotspot, connect the board to the hotspot and use the board's assigned IP address. `192.168.4.1` is only a placeholder and may not be the actual IP.

## Build APK
1. Open this folder in Android Studio (recent version).
2. Allow Gradle sync and install Android SDK Platform 35.
3. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Not yet implemented
Bluetooth/BLE transport, cloud account/server, MQTT, dashboards/widgets editor, historical database, alerts, and downloadable offline LLM. These require separate implementation and testing; this starter must not be mistaken for a complete Blynk replacement.
