# Kendo Assistant — Smart Home Relay Controller

## Project Overview
A Flask web application that serves as the backend for the Kendo Assistant Android app. It provides a `/smarthome` control panel to manage 8 smart home relays via Tasmota devices.

## Architecture
- **Backend**: Python 3.11 + Flask
- **Frontend**: Jinja2 templates (served by Flask)
- **Android App**: WebView app (source in `app/`) that connects to this Flask server at `/smarthome`

## Key Routes
- `/` — Landing page
- `/smarthome` — 8-relay control panel (what the Android app wraps)
- `/api/relay/<id>/toggle` — Toggle a relay on/off
- `/api/relay/<id>/set` — Set relay state explicitly
- `/api/relay/all-on` — Turn all 8 relays ON
- `/api/relay/all-off` — Turn all 8 relays OFF
- `/api/settings` — GET/POST device names and Tasmota IPs

## Tasmota Integration
Each relay card can be configured with the IP address of a Tasmota device. When toggled, the server sends HTTP commands to `http://<ip>/cm?cmnd=Power%20On` or `Power%20Off`.

## Running
```bash
python app.py
```
Runs on `0.0.0.0:5000`.

## Android Source
The Android WebView app source is in `app/` (Kotlin + Gradle). Build via GitHub Actions (`.github/workflows/build.yml`) or Android Studio.

## Files
- `app.py` — Main Flask application
- `templates/index.html` — Landing page
- `templates/smarthome.html` — Relay control panel
- `requirements.txt` — Python dependencies
- `app/` — Android app source (Kotlin)
