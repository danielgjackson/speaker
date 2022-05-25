package dev.danjackson.speaker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html.*
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY

class MainActivity : AppCompatActivity() {

    //private var menuIsPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val infoTextView = findViewById<TextView>(R.id.info_text_view)
        infoTextView.movementMethod = ScrollingMovementMethod()

        val monitor = Monitor.getInstance(applicationContext)

        monitor.summary.observe(this) {

            val deviceSummary =
                // Summary changes when preferences (including device selection) changes
                when {
                    it == null -> "\uD83D\uDD07"
                    it.isNotEmpty() -> it.joinToString(
                        prefix = "\uD83D\uDD09 ",
                        separator = "; \uD83D\uDD09 ",
                        postfix = "."
                    ) // 🔉
                    else -> applicationContext.getString(R.string.device_list_summary)
                }  // "⚠️"

            val text: String = getString(R.string.info_text, deviceSummary)

            val styledText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                fromHtml(text, FROM_HTML_MODE_LEGACY)
            else @Suppress("DEPRECATION")
            fromHtml(text)

            infoTextView.text = styledText
        }

        monitor.playing.observe(this) {
            // Refresh menu enabled/disabled state
            //menuIsPlaying = it
            this.invalidateOptionsMenu()
        }
    }


    override fun onResume() {
        super.onResume()

        val monitor = Monitor.getInstance(applicationContext)
        monitor.mainActivityResumed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

        override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuIsPlaying = Monitor.getInstance(applicationContext).playing.value
        menu.findItem(R.id.action_play).isVisible = menuIsPlaying != true
        menu.findItem(R.id.action_pause).isVisible = menuIsPlaying == true
        return true
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openSettings()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)) {
                    val builder = AlertDialog.Builder(this)
                    with(builder)
                    {
                        setTitle("Bluetooth Connect Permission")
                        setMessage("To list your paired devices, this app requires Bluetooth Connect permission.")
                        setPositiveButton("OK") { _, _ ->

                        }
                        setPositiveButton("Cancel") { _, _ ->
                            Toast.makeText(applicationContext,"Cannot list devices without Bluetooth Connect permission", Toast.LENGTH_SHORT).show()
                        }
                        show()
                    }
                } else {
                    Toast.makeText(applicationContext,"Cannot list devices without Bluetooth Connect permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val bluetoothConnectRequestCode = 1

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                // Prompt for Bluetooth permission if required
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), bluetoothConnectRequestCode)
                    } else {
                        Toast.makeText(this,"Problem requesting permission: Bluetooth Connect", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    openSettings()
                }
                true
            }
            R.id.action_play, R.id.action_pause  -> {
                if (Monitor.getInstance(applicationContext).playing.value == true) {
                    Monitor.getInstance(applicationContext).stop()
                } else {
                    Monitor.getInstance(applicationContext).play()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
