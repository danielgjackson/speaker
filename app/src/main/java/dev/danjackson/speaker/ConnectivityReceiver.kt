package dev.danjackson.speaker

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED") {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val state = intent.getIntExtra(
                BluetoothProfile.EXTRA_STATE,
                -1
            )
            val previousState = intent.getIntExtra(
                BluetoothProfile.EXTRA_PREVIOUS_STATE,
                -1
            )

            //val text = "CONNECTION_STATE_CHANGED: $previousState -> $state ${device?.name}"
            //println(">>> $text")
            //Toast.makeText(context, text, Toast.LENGTH_LONG).show()

            val monitor = Monitor.getInstance(context.applicationContext)
            monitor.connectionStateChanged(device?.name, state)
        } //else {
            //val text = "BroadcastReceiver.onReceive(${intent.action})"
            //println(">>> $text")
            //Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        //}

    }
}
