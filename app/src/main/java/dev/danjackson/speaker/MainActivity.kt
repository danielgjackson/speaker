package dev.danjackson.speaker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.parseAsHtml
import dev.danjackson.speaker.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    //private var menuIsPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.infoTextView.movementMethod = ScrollingMovementMethod()

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
            binding.infoTextView.text = text.parseAsHtml()
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

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) {
            isGranted: Boolean ->
        if (isGranted) {
            openSettings()
        } else {
            showPermissionRationale()
        }
    }

    private fun showPermissionRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)) {
            AlertDialog.Builder(this)
                .setTitle("Bluetooth Connect Permission")
                .setMessage("To list your paired devices, this app requires Bluetooth Connect permission.")
                .setPositiveButton("OK", null)
                .setPositiveButton("Cancel") { _, _ ->
                    Toast.makeText(applicationContext, R.string.bluetooth_permission_error, Toast.LENGTH_SHORT).show()
                    openSettings()
                }
                .show()
        } else {
            Toast.makeText(applicationContext, R.string.bluetooth_permission_error, Toast.LENGTH_SHORT).show()
            openSettings()
        }
    }

    private val isBluetoothPermissionDenied: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                // Prompt for Bluetooth permission if required
                if (isBluetoothPermissionDenied) {
                    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
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
