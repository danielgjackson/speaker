package uk.co.danieljackson.speaker

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // --- Device List Multi Select ---
            val deviceListPreference: MultiSelectListPreference? = findPreference("device_list")

            // Summary provider
            deviceListPreference?.summaryProvider =
                Preference.SummaryProvider<MultiSelectListPreference> { preference ->
                    val values = preference.values.orEmpty()
                    if (values.isEmpty()) {
                        getText(R.string.device_list_summary)
                    } else {
                        values.toTypedArray().joinToString(separator = "\n")
                    }
                }

            // Get current list
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val selectedDevices = sharedPreferences.getStringSet("device_list", setOf<String>()).orEmpty()

            // Get bonded devices
            val bondedDevices = BluetoothAdapter.getDefaultAdapter().bondedDevices
            val bondedDeviceNames = mutableListOf<String>()
            bondedDevices.forEach { bluetoothDevice ->
                bondedDeviceNames.add(bluetoothDevice.name)
            }

            // Union of the current selection and the available devices
            val allDevices = mutableSetOf<String>()
            for (device in selectedDevices) {
                allDevices.add(device)
            }
            for (device in bondedDeviceNames) {
                allDevices.add(device)
            }

            // Set entry value/display
            val entryValues = mutableListOf<String>()
            val entryDisplay = mutableListOf<String>()
            for (device in allDevices) {
                entryValues.add(device)
                entryDisplay.add(device)
            }
            deviceListPreference?.entries = entryDisplay.toTypedArray()
            deviceListPreference?.entryValues = entryValues.toTypedArray()

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}