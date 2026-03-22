package com.smarthome.relay

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var loadingBar: ProgressBar
    private lateinit var btnRetry: Button
    private lateinit var btnSettings: ImageButton
    private lateinit var txtError: TextView

    private var serverUrl: String = ""

    companion object {
        private const val PREFS_NAME = "SmartHomePrefs"
        private const val KEY_IP = "server_ip"
        private const val KEY_PORT = "server_port"
        private const val DEFAULT_IP = "192.168.1.100"
        private const val DEFAULT_PORT = "5000"
        private const val SMARTHOME_PATH = "/smarthome"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadServerUrl()
        setupWebView()
        setupSwipeRefresh()

        loadSmartHome()
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        errorLayout = findViewById(R.id.errorLayout)
        loadingBar = findViewById(R.id.loadingBar)
        btnRetry = findViewById(R.id.btnRetry)
        btnSettings = findViewById(R.id.btnSettings)
        txtError = findViewById(R.id.txtError)

        btnRetry.setOnClickListener { loadSmartHome() }
        btnSettings.setOnClickListener { showSettingsDialog() }
    }

    private fun loadServerUrl() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ip = prefs.getString(KEY_IP, DEFAULT_IP) ?: DEFAULT_IP
        val port = prefs.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
        serverUrl = "http://$ip:$port$SMARTHOME_PATH"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = false
            // Izinkan request ke HTTP (lokal)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                loadingBar.visibility = View.VISIBLE
                errorLayout.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                loadingBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    loadingBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    webView.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    val errMsg = error?.description?.toString() ?: "Tidak diketahui"
                    txtError.text = "Tidak bisa terhubung ke server.\n\nPastikan:\n• Server Flask menyala\n• HP & komputer 1 jaringan WiFi\n• IP & port sudah benar\n\nError: $errMsg"
                }
            }

            // Izinkan semua SSL (untuk dev environment lokal)
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: android.net.http.SslError?
            ) {
                handler?.proceed()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                loadingBar.progress = newProgress
            }

            // Izinkan akses mikrofon untuk fitur voice command
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(
            getColor(R.color.colorPrimary)
        )
        swipeRefresh.setOnRefreshListener {
            loadSmartHome()
        }
    }

    private fun loadSmartHome() {
        errorLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
        loadingBar.visibility = View.VISIBLE

        if (!isNetworkAvailable()) {
            loadingBar.visibility = View.GONE
            webView.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            txtError.text = "Tidak ada koneksi WiFi.\n\nPastikan HP terhubung ke jaringan yang sama dengan server."
            swipeRefresh.isRefreshing = false
            return
        }

        webView.loadUrl(serverUrl)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showSettingsDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentIp = prefs.getString(KEY_IP, DEFAULT_IP) ?: DEFAULT_IP
        val currentPort = prefs.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT

        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val etIp = dialogView.findViewById<EditText>(R.id.etIpAddress)
        val etPort = dialogView.findViewById<EditText>(R.id.etPort)

        etIp.setText(currentIp)
        etPort.setText(currentPort)

        AlertDialog.Builder(this)
            .setTitle("Pengaturan Server")
            .setView(dialogView)
            .setPositiveButton("Simpan & Hubungkan") { _, _ ->
                val ip = etIp.text.toString().trim()
                val port = etPort.text.toString().trim()

                if (ip.isEmpty()) {
                    Toast.makeText(this, "IP tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                prefs.edit()
                    .putString(KEY_IP, ip)
                    .putString(KEY_PORT, port.ifEmpty { DEFAULT_PORT })
                    .apply()

                loadServerUrl()
                loadSmartHome()
                Toast.makeText(this, "Kendo Assistant — menghubungkan ke $serverUrl", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }
}
