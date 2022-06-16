package dev.danjackson.speaker

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import dev.danjackson.speaker.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager
            .beginTransaction()
            .replace(binding.settings.id, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        // Update multi-select, only require unselected elements when showing full list (requires permission)
        private fun refresh(showUnselected: Boolean) {
            // --- Device List Multi Select ---
            val deviceListPreference: MultiSelectListPreference? = findPreference("device_list")

            // Get current list
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val selectedDevices = sharedPreferences.getStringSet("device_list", setOf<String>()).orEmpty()

            // Start with no bonded devices
            val bondedDeviceNames = mutableListOf<String>()

            // Get bonded devices
            if (showUnselected) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val bluetoothManager =
                        requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
                    val bondedDevices = bluetoothAdapter?.bondedDevices.orEmpty()
                    bondedDevices.forEach { bluetoothDevice ->
                        bondedDeviceNames.add(bluetoothDevice.name)
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.bluetooth_permission_error, Toast.LENGTH_SHORT).show()
                }
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
            addPreferencesFromResource(R.xml.root_preferences)

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

            // TODO: false when constructed, call again with true when open details
            refresh(true)
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