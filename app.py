from flask import Flask, render_template, request, jsonify
import requests
import json

app = Flask(__name__)

TASMOTA_DEVICES = [
    {"id": 1, "name": "Relay 1", "ip": "", "state": False},
    {"id": 2, "name": "Relay 2", "ip": "", "state": False},
    {"id": 3, "name": "Relay 3", "ip": "", "state": False},
    {"id": 4, "name": "Relay 4", "ip": "", "state": False},
    {"id": 5, "name": "Relay 5", "ip": "", "state": False},
    {"id": 6, "name": "Relay 6", "ip": "", "state": False},
    {"id": 7, "name": "Relay 7", "ip": "", "state": False},
    {"id": 8, "name": "Relay 8", "ip": "", "state": False},
]

device_states = {i: False for i in range(1, 9)}
device_ips = {i: "" for i in range(1, 9)}
device_names = {i: f"Relay {i}" for i in range(1, 9)}


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/smarthome")
def smarthome():
    devices = []
    for i in range(1, 9):
        devices.append({
            "id": i,
            "name": device_names[i],
            "ip": device_ips[i],
            "state": device_states[i],
        })
    return render_template("smarthome.html", devices=devices)


@app.route("/api/relay/<int:relay_id>/toggle", methods=["POST"])
def toggle_relay(relay_id):
    if relay_id < 1 or relay_id > 8:
        return jsonify({"error": "Invalid relay ID"}), 400

    ip = device_ips.get(relay_id, "")
    new_state = not device_states[relay_id]

    if ip:
        try:
            cmd = "Power%20On" if new_state else "Power%20Off"
            url = f"http://{ip}/cm?cmnd={cmd}"
            resp = requests.get(url, timeout=3)
            resp.raise_for_status()
        except requests.RequestException as e:
            return jsonify({"error": f"Tasmota error: {str(e)}", "state": device_states[relay_id]}), 500

    device_states[relay_id] = new_state
    return jsonify({"relay_id": relay_id, "state": new_state, "name": device_names[relay_id]})


@app.route("/api/relay/<int:relay_id>/set", methods=["POST"])
def set_relay(relay_id):
    if relay_id < 1 or relay_id > 8:
        return jsonify({"error": "Invalid relay ID"}), 400

    data = request.get_json() or {}
    new_state = data.get("state", False)
    ip = device_ips.get(relay_id, "")

    if ip:
        try:
            cmd = "Power%20On" if new_state else "Power%20Off"
            url = f"http://{ip}/cm?cmnd={cmd}"
            resp = requests.get(url, timeout=3)
            resp.raise_for_status()
        except requests.RequestException as e:
            return jsonify({"error": f"Tasmota error: {str(e)}", "state": device_states[relay_id]}), 500

    device_states[relay_id] = new_state
    return jsonify({"relay_id": relay_id, "state": new_state, "name": device_names[relay_id]})


@app.route("/api/relay/all-on", methods=["POST"])
def all_on():
    errors = []
    for i in range(1, 9):
        ip = device_ips.get(i, "")
        if ip:
            try:
                requests.get(f"http://{ip}/cm?cmnd=Power%20On", timeout=3)
            except Exception as e:
                errors.append(f"Relay {i}: {str(e)}")
        device_states[i] = True
    return jsonify({"states": device_states, "errors": errors})


@app.route("/api/relay/all-off", methods=["POST"])
def all_off():
    errors = []
    for i in range(1, 9):
        ip = device_ips.get(i, "")
        if ip:
            try:
                requests.get(f"http://{ip}/cm?cmnd=Power%20Off", timeout=3)
            except Exception as e:
                errors.append(f"Relay {i}: {str(e)}")
        device_states[i] = False
    return jsonify({"states": device_states, "errors": errors})


@app.route("/api/settings", methods=["GET"])
def get_settings():
    settings = []
    for i in range(1, 9):
        settings.append({
            "id": i,
            "name": device_names[i],
            "ip": device_ips[i],
            "state": device_states[i],
        })
    return jsonify(settings)


@app.route("/api/settings", methods=["POST"])
def save_settings():
    data = request.get_json() or {}
    for item in data.get("devices", []):
        relay_id = item.get("id")
        if relay_id and 1 <= relay_id <= 8:
            if "name" in item:
                device_names[relay_id] = item["name"]
            if "ip" in item:
                device_ips[relay_id] = item["ip"]
    return jsonify({"success": True})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
