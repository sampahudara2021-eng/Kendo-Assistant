# Smart Home Relay - Android App

Aplikasi Android WebView untuk mengontrol 8 relay via Flask + Tasmota.

## Cara Build APK (Gratis, tanpa install Android Studio)

### Metode 1: GitHub Actions (Paling Mudah)

1. **Buat akun GitHub** di https://github.com (gratis)
2. **Buat repository baru** → nama bebas, misalnya `smarthome-apk`
3. **Upload semua file** dari folder ini ke repository tersebut
4. GitHub Actions otomatis mulai build
5. Setelah ~3-5 menit, buka tab **Actions** → klik workflow → scroll bawah → **Download artifact**
6. Extract zip → dapatkan file `app-debug.apk`

### Metode 2: Android Studio (PC sendiri)
1. Download Android Studio dari https://developer.android.com/studio
2. Buka folder ini sebagai project
3. Klik **Build → Build Bundle(s)/APK(s) → Build APK(s)**
4. APK ada di `app/build/outputs/apk/debug/`

## Cara Install APK di HP

1. Pindahkan APK ke HP (via kabel USB / Google Drive / WhatsApp ke diri sendiri)
2. Buka file manager, cari file APK
3. Tap untuk install → izinkan "Install dari sumber tidak dikenal" jika diminta
4. Selesai! Ikon "Smart Home" muncul di layar

## Cara Pakai Aplikasi

1. Pastikan HP dan komputer server terhubung ke **WiFi yang sama**
2. Buka app → tap ikon ⚙️ (settings) di pojok kanan atas
3. Isi **IP Address** komputer server (cek dengan `ipconfig` di Windows / `ifconfig` di Linux)
4. Isi **Port** → `5000` (default Flask)
5. Tap **Simpan & Hubungkan**
6. Tampilan `/smarthome` Flask langsung muncul!

## Cek IP Server

**Windows:**
```
ipconfig
```
Cari "IPv4 Address" di bagian WiFi adapter

**Linux/Mac:**
```
ip addr show
```
atau
```
hostname -I
```

## Fitur App

- WebView membungkus halaman `/smarthome` Flask kamu
- Swipe down untuk refresh
- Tombol settings untuk ganti IP kapan saja
- Tampilan error jika server tidak bisa dihubungi
- Izin mikrofon sudah disiapkan (untuk voice command di Flask)
- Mendukung Android 7.0 (API 24) ke atas

## Struktur File

```
SmartHomeApp/
├── app/
│   └── src/main/
│       ├── java/com/smarthome/relay/
│       │   └── MainActivity.kt       ← Logika utama
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml ← Layout halaman utama
│       │   │   └── dialog_settings.xml ← Dialog pengaturan IP
│       │   ├── drawable/             ← Ikon-ikon
│       │   └── values/               ← Warna & tema
│       └── AndroidManifest.xml
├── .github/workflows/build.yml       ← GitHub Actions (auto build)
├── build.gradle
├── settings.gradle
└── README.md
```
