package com.example.timer.base

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.timer.R
import com.example.timer.databinding.ActivityMainBinding
import com.example.timer.model.Duration
import com.example.timer.service.TimerService
import com.example.timer.utils.Utils

private const val TIME_UNIT_MAX_VALUE = 59
private const val PREF_TIMER_PREV_STATE_SECONDS = "timerPreviousStateInSeconds"
private const val PREF_SELECTED_LANGUAGE = "selectedLanguage"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tickReceiver: BroadcastReceiver

    private lateinit var etHours: EditText
    private lateinit var etMinutes: EditText
    private lateinit var etSeconds: EditText
    private lateinit var etTime: List<EditText> // All time unit edit texts in a list

    private var timerStarted = false
    private var timerResumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
            .also { b ->
                b.lifecycleOwner = this

                etHours = b.editTextHours
                etMinutes = b.editTextMinutes
                etSeconds = b.editTextSeconds
            }

        etTime = listOf(etHours, etMinutes, etSeconds)

        etTime.forEach { editText ->
            editText.addAfterTextChangedListener()
            editText.setCustomOnFocusChangedListener()
        }

        createNotificationChannels()

        tickReceiver = createBroadcastReceiver()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(tickReceiver, IntentFilter(ACTION_TIMER_STATE_CHANGED))

        val duration = Duration(getPreference(PREF_TIMER_PREV_STATE_SECONDS))
        etHours.update(duration.hours)
        etMinutes.update(duration.minutes)
        etSeconds.update(duration.seconds)

        requestInitDataFromService()

        val requestPermissionLauncher = registerPermissionsCallback()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.changelanguage, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(tickReceiver)
    }

    fun onTimerStart(view: View) {
        if (!timerStarted) {
            startTimerService(COMMAND_START)

            val duration = Duration(etHours.getInt(), etMinutes.getInt(), etSeconds.getInt())
            savePreference(PREF_TIMER_PREV_STATE_SECONDS, duration.secondsTotal)

            binding.floatingButtonStop.visibility = Button.VISIBLE

        } else if (view.tag.toString() == getString(R.string.pause)) {
            startTimerService(COMMAND_PAUSE)
        } else {
            startTimerService(COMMAND_RESUME)
        }
        Utils.hideKeyboard(this, view)
    }

    fun onTimerStop(@Suppress(SUPPRESS_UNUSED_PARAMETER)view: View) { stopService(getTimerServiceIntent(COMMAND_STOP)) }

    fun selectAllText(view: View) { if (view is EditText) view.selectAll() }

    private fun startTimerService(command: String) {
        val intent = getTimerServiceIntent(command)
        if (Utils.isOreoOrAbove()) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    /**
     * Make sure that the value of an [EditText] is no more than [TIME_UNIT_MAX_VALUE].
     * If the [EditText] is empty set the value to '00'
     */
    private fun EditText.addAfterTextChangedListener() {
        this.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    this.apply {
                        setText(getString(R.string.zero))
                        selectAll()
                    }
                } else {
                    val n = text.toString().toInt()
                    if (n > TIME_UNIT_MAX_VALUE) this.apply {
                        setText("$TIME_UNIT_MAX_VALUE")
                        setSelection(length())
                    }
                }
            }
        })
    }

    /**
     * Add a leading zero to the [EditText] if the value is one-figure
     */
    private fun EditText.setCustomOnFocusChangedListener() {
        this.setOnFocusChangeListener { view, hasFocus ->
            if (view is EditText && !hasFocus && view.text.length == 1) {
                this.setText(getString(R.string.leading_zero, view.text.toString().toInt()))
            }
        }
    }

    private fun getTimerServiceIntent(command: String): Intent {
        return Intent(this, TimerService::class.java)
            .putExtra(EXTRA_COMMAND, command)
            .putExtra(EXTRA_HOURS, etHours.getInt())
            .putExtra(EXTRA_MINUTES, etMinutes.getInt())
            .putExtra(EXTRA_SECONDS, etSeconds.getInt())
    }

    private fun createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ or higher because
        // the NotificationChannel class is new and not in the support library
        if (Utils.isOreoOrAbove()) {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)

            serviceChannel.description = getString(R.string.service_channel_description)

            val timeEndChannel = NotificationChannel(
                TIMER_END_CHANNEL_ID,
                getString(R.string.timer_end_channel_name),
                NotificationManager.IMPORTANCE_HIGH)

            timeEndChannel.description = getString(R.string.timer_end_channel_description)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.apply {
                createNotificationChannel(serviceChannel)
                createNotificationChannel(timeEndChannel)
            }
        }
    }

    private fun createBroadcastReceiver() = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.apply {
                when (getIntExtra(EXTRA_RESULT_CODE, 0)) {
                    SECONDS_RESULT_CODE -> etSeconds.update(getIntExtra(EXTRA_SECONDS, 0))
                    MINUTES_RESULT_CODE -> etMinutes.update(getIntExtra(EXTRA_MINUTES, 0))
                    HOURS_RESULT_CODE -> etHours.update(getIntExtra(EXTRA_HOURS, 0))

                    TIMER_STARTED_RESULT_CODE ->
                        onTimerStarted(getBooleanExtra(EXTRA_TIMER_STARTED, false))
                    TIMER_RESUMED_RESULT_CODE ->
                        onTimerResumed(getBooleanExtra(EXTRA_TIMER_RESUMED, false))

                    ALL_DATA_RESULT_CODE -> {
                        etSeconds.update(getIntExtra(EXTRA_SECONDS, 2))
                        etMinutes.update(getIntExtra(EXTRA_MINUTES, 0))
                        etHours.update(getIntExtra(EXTRA_HOURS, 0))
                        onTimerStarted(getBooleanExtra(EXTRA_TIMER_STARTED, false))
                        onTimerResumed(getBooleanExtra(EXTRA_TIMER_RESUMED, false))
                    }
                }
            }
        }
    }

    private fun EditText.update(value: Int) {
        if (value < 10) {
            this.setText(getString(R.string.leading_zero, value))
        } else {
            this.setText(value.toString())
        }
    }

    private fun onTimerStarted(started: Boolean) {
        if (started) {
            etTime.forEach {
                editText -> editText.setEditable(false)
                binding.floatingButtonStop.visibility = Button.VISIBLE
            }
        } else {
            etTime.forEach { editText -> editText.setEditable(true) }
            binding.apply {
                floatingButtonStop.visibility = Button.INVISIBLE
                floatingButtonStart.setImageResource(R.drawable.icon_resume_48p)
                floatingButtonStart.tag = (this@MainActivity.getString(R.string.resume))
            }
        }
        timerStarted = started
    }

    private fun onTimerResumed(resumed: Boolean) {
        if (resumed) {
            binding.apply {
                floatingButtonStart.setImageResource(R.drawable.icon_pause_48p)
                floatingButtonStart.tag = (this@MainActivity.getString(R.string.pause))
            }
        } else if (timerStarted) {
            binding.apply {
                floatingButtonStart.setImageResource(R.drawable.icon_resume_48p)
                floatingButtonStart.tag = (this@MainActivity.getString(R.string.resume))
            }
        }
        timerResumed = resumed
    }

    private fun EditText.getInt() = this.text.toString().toInt()

    private fun EditText.setEditable(editable: Boolean) {
        this.apply {
            isClickable = editable
            isFocusable = editable
            isFocusableInTouchMode = editable
        }
    }

    private fun requestInitDataFromService() {
        intent = Intent(this, TimerService::class.java)
            .putExtra(EXTRA_COMMAND, COMMAND_SEND_DATA)
        startService(intent)
    }

    private fun registerPermissionsCallback() =
        registerForActivityResult(RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                getString(R.string.permission_not_granted_message),
                Toast.LENGTH_LONG).show()
        }
    }

    private fun getPreference(key: String, defaultValue: Int = 0): Int {
        return getPreferences(MODE_PRIVATE).getInt(key, defaultValue)
    }

    private fun savePreference(key: String, value: Int) {
        getPreferences(MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }
}