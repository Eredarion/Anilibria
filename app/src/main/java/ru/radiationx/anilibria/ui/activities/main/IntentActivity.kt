package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.BuildConfig

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : AppCompatActivity() {

    companion object {
        const val KEY_RESTORE = "restore_data"
        const val ACTION_RESTORE = "anilibria.app.RESTORE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lalala", "IntentActivity intent: $intent")
        intent?.also {
            if (it.action == ACTION_RESTORE) {
                it.extras?.let {
                    val json = it.getString(KEY_RESTORE)
                    Toast.makeText(this@IntentActivity, "Json size: ${json?.length}", Toast.LENGTH_SHORT).show()
                }
            } else {
                it.data?.let {
                    val screen = App.injections.linkHandler.findScreen(it.toString())
                    Log.e("lalala", "screen: $screen, url=${it.toString()}")
                    if (screen != null) {
                        startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                            data = it
                        })
                    }
                }

                finish()
            }
        }
    }
}