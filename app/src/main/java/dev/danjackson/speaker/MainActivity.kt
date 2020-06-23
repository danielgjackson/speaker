package dev.danjackson.speaker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html.*
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : AppCompatActivity() {

    //private var menuIsPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)

        info_text_view.movementMethod = ScrollingMovementMethod()

        val monitor = Monitor.getInstance(applicationContext)

        monitor.summary.observe(this, Observer {

            val deviceSummary =
                // Summary changes when preferences (including device selection) changes
                when {
                    it == null -> "\uD83D\uDD07"
                    it.count() > 0 -> it.joinToString(prefix = "\uD83D\uDD09 ", separator = "; \uD83D\uDD09 ", postfix = ".") // 🔉
                    else -> applicationContext.getString(R.string.device_list_summary)
                }  // "⚠️"

            val text: String = getString(R.string.info_text, deviceSummary)

            val styledText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                fromHtml(text, FROM_HTML_MODE_LEGACY)
            else @Suppress("DEPRECATION")
                fromHtml(text)

            info_text_view.text = styledText
        })

        monitor.playing.observe(this, Observer {
            // Refresh menu enabled/disabled state
            //menuIsPlaying = it
            this.invalidateOptionsMenu()
        })
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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
