package com.irashad1707.bharatkeyime

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.provider.Settings
import android.view.InputDevice
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

class CustomWinIME : InputMethodService(), InputManager.InputDeviceListener {

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val inputManager by lazy { getSystemService(InputManager::class.java) }

    private var layoutMode = InscriptEngine.LayoutMode.ENGLISH_US
    private val altCodeEngine = AltCodeEngine()

    private var altHeld = false
    private var altShiftToggleConsumed = false
    private var capsLockEnabled = false

    override fun onCreate() {
        super.onCreate()
        activeService = WeakReference(this)
        createNotificationChannel()
        loadSavedLayout()
        inputManager.registerInputDeviceListener(this, null)
    }

    override fun onDestroy() {
        cancelLanguageNotification()
        inputManager.unregisterInputDeviceListener(this)
        activeService?.clear()
        activeService = null
        super.onDestroy()
    }

    override fun onCreateInputView(): View? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        loadSavedLayout()
        showLanguageNotification()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val ic = currentInputConnection ?: return super.onKeyDown(keyCode, event)

        // BharatKey IME is intentionally restricted to real attached physical keyboards.
        // Non-physical/virtual key events are not translated or committed by this IME.
        if (!isPhysicalKeyboardEvent(event)) {
            showLanguageNotification()
            return super.onKeyDown(keyCode, event)
        }

        if (isNavigationKey(keyCode)) return super.onKeyDown(keyCode, event)
        if (isShortcutPassthrough(event, keyCode)) return super.onKeyDown(keyCode, event)

        if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK && event.repeatCount == 0) {
            capsLockEnabled = !capsLockEnabled
            showShortToast("Caps Lock ${if (capsLockEnabled) "ON" else "OFF"}")
            showLanguageNotification()
            return true
        }

        if (isAltKey(keyCode)) {
            altHeld = true
            altCodeEngine.start()
            showLanguageNotification()
            return true
        }

        if (altHeld && isShiftKey(keyCode)) {
            toggleLayout(showToast = true)
            altShiftToggleConsumed = true
            altCodeEngine.cancel()
            return true
        }

        if (altHeld || event.isAltPressed) {
            val digit = altCodeDigit(keyCode)
            if (digit != null) {
                altCodeEngine.appendDigit(digit)
                showLanguageNotification()
                return true
            }
            return true
        }

        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                return deleteSelectionOrCharacter(ic, forwardDelete = false)
            }
            KeyEvent.KEYCODE_FORWARD_DEL -> {
                return deleteSelectionOrCharacter(ic, forwardDelete = true)
            }
            KeyEvent.KEYCODE_ENTER -> {
                ic.commitText("\n", 1)
                return true
            }
            KeyEvent.KEYCODE_TAB -> {
                ic.commitText("\t", 1)
                return true
            }
        }

        val output = InscriptEngine.translate(
            keyCode = keyCode,
            shift = event.isShiftPressed,
            capsLock = capsLockEnabled,
            layoutMode = layoutMode
        )

        return if (output != null) {
            if (output.isNotEmpty()) ic.commitText(output, 1)
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val ic = currentInputConnection ?: return super.onKeyUp(keyCode, event)

        if (!isPhysicalKeyboardEvent(event)) {
            showLanguageNotification()
            return super.onKeyUp(keyCode, event)
        }

        if (isNavigationKey(keyCode)) return super.onKeyUp(keyCode, event)
        if (isShortcutPassthrough(event, keyCode)) return super.onKeyUp(keyCode, event)

        if (isAltKey(keyCode)) {
            altHeld = false
            if (!altShiftToggleConsumed) {
                altCodeEngine.finish()?.let { ic.commitText(it, 1) }
            } else {
                altCodeEngine.cancel()
            }
            altShiftToggleConsumed = false
            showLanguageNotification()
            return true
        }

        if (altHeld || event.isAltPressed) return true
        return super.onKeyUp(keyCode, event)
    }

    private fun loadSavedLayout() {
        val saved = prefs.getString(KEY_LAYOUT_MODE, InscriptEngine.LayoutMode.ENGLISH_US.name)
        layoutMode = runCatching {
            InscriptEngine.LayoutMode.valueOf(saved ?: InscriptEngine.LayoutMode.ENGLISH_US.name)
        }.getOrDefault(InscriptEngine.LayoutMode.ENGLISH_US)
    }

    private fun saveLayout() {
        prefs.edit().putString(KEY_LAYOUT_MODE, layoutMode.name).apply()
    }

    private fun toggleLayout(showToast: Boolean) {
        layoutMode = when (layoutMode) {
            InscriptEngine.LayoutMode.ENGLISH_US -> InscriptEngine.LayoutMode.HINDI_CUSTOM
            InscriptEngine.LayoutMode.HINDI_CUSTOM -> InscriptEngine.LayoutMode.ENGLISH_US
        }
        saveLayout()
        showLanguageNotification()
        if (showToast) showShortToast("${layoutMode.fullName} Enabled")
    }

    private fun showKeyboardPicker() {
        getSystemService(InputMethodManager::class.java).showInputMethodPicker()
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        showLanguageNotification()
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        showLanguageNotification()
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        showLanguageNotification()
    }

    private fun isPhysicalKeyboardAttached(): Boolean {
        return InputDevice.getDeviceIds().any { id ->
            val device = InputDevice.getDevice(id) ?: return@any false
            isUsablePhysicalKeyboard(device)
        }
    }

    private fun isPhysicalKeyboardEvent(event: KeyEvent): Boolean {
        val device = event.device ?: return false
        return isUsablePhysicalKeyboard(device)
    }

    private fun isUsablePhysicalKeyboard(device: InputDevice): Boolean {
        val hasKeyboardSource =
            device.sources and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD

        return !device.isVirtual &&
            hasKeyboardSource &&
            device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
    }

    private fun showLanguageNotification() {
        if (!canPostNotifications()) return

        val switchKeyboardPendingIntent = PendingIntent.getBroadcast(
            this,
            1001,
            Intent(this, ImeActionReceiver::class.java).apply { action = ACTION_SHOW_KEYBOARD_PICKER },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleLanguagePendingIntent = PendingIntent.getBroadcast(
            this,
            1002,
            Intent(this, ImeActionReceiver::class.java).apply { action = ACTION_TOGGLE_LANGUAGE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val settingsPendingIntent = PendingIntent.getActivity(
            this,
            1003,
            Intent(this, BharatKeySettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hasPhysicalKeyboard = isPhysicalKeyboardAttached()
        val altPreview = altCodeEngine.preview()
        val altLine = if (altPreview.isNotEmpty()) "\nAlt Code: $altPreview" else ""
        val capsLine = if (layoutMode == InscriptEngine.LayoutMode.ENGLISH_US) {
            "\nCaps Lock: ${if (capsLockEnabled) "ON" else "OFF"}"
        } else ""

        val title = if (hasPhysicalKeyboard) {
            "BharatKey IME — ${layoutMode.shortName}"
        } else {
            "BharatKey IME — No Keyboard"
        }

        val text = if (hasPhysicalKeyboard) {
            "${layoutMode.fullName} active"
        } else {
            "Connect a physical keyboard to use BharatKey IME"
        }

        val bigText = if (hasPhysicalKeyboard) {
            "${layoutMode.fullName} active$altLine$capsLine\n" +
                "Alt + Shift: Toggle EN / INS\n" +
                "Change Keyboard: Switch to Gboard or another installed keyboard."
        } else {
            "BharatKey IME only translates input from an attached physical keyboard.\n" +
                "Connect a USB OTG, Bluetooth, or tablet keyboard cover to use EN / INS typing."
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_keyboard_language)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(settingsPendingIntent)
            .addAction(R.drawable.ic_keyboard_language, "Change Keyboard", switchKeyboardPendingIntent)
            .addAction(R.drawable.ic_keyboard_language, "Toggle EN/INS", toggleLanguagePendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun cancelLanguageNotification() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BharatKey Language Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current BharatKey IME language and keyboard switch option"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun deleteSelectionOrCharacter(
        ic: InputConnection,
        forwardDelete: Boolean
    ): Boolean {
        val selectedText = ic.getSelectedText(0)

        return if (!selectedText.isNullOrEmpty()) {
            ic.beginBatchEdit()
            ic.finishComposingText()
            ic.commitText("", 1)
            ic.endBatchEdit()
            true
        } else {
            if (forwardDelete) {
                ic.deleteSurroundingText(0, 1)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
            true
        }
    }

    private fun isNavigationKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
            keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            keyCode == KeyEvent.KEYCODE_MOVE_HOME ||
            keyCode == KeyEvent.KEYCODE_MOVE_END ||
            keyCode == KeyEvent.KEYCODE_PAGE_UP ||
            keyCode == KeyEvent.KEYCODE_PAGE_DOWN
    }

    private fun isShortcutPassthrough(event: KeyEvent, keyCode: Int): Boolean {
        if (!event.isCtrlPressed) return false
        return keyCode == KeyEvent.KEYCODE_A ||
            keyCode == KeyEvent.KEYCODE_C ||
            keyCode == KeyEvent.KEYCODE_V ||
            keyCode == KeyEvent.KEYCODE_X ||
            keyCode == KeyEvent.KEYCODE_Z ||
            keyCode == KeyEvent.KEYCODE_Y ||
            keyCode == KeyEvent.KEYCODE_S ||
            keyCode == KeyEvent.KEYCODE_F ||
            keyCode == KeyEvent.KEYCODE_P ||
            keyCode == KeyEvent.KEYCODE_N ||
            keyCode == KeyEvent.KEYCODE_ENTER
    }

    private fun isAltKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT
    }

    private fun isShiftKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
    }

    private fun altCodeDigit(keyCode: Int): Char? {
        val numpadDigit = when (keyCode) {
            KeyEvent.KEYCODE_NUMPAD_0 -> '0'
            KeyEvent.KEYCODE_NUMPAD_1 -> '1'
            KeyEvent.KEYCODE_NUMPAD_2 -> '2'
            KeyEvent.KEYCODE_NUMPAD_3 -> '3'
            KeyEvent.KEYCODE_NUMPAD_4 -> '4'
            KeyEvent.KEYCODE_NUMPAD_5 -> '5'
            KeyEvent.KEYCODE_NUMPAD_6 -> '6'
            KeyEvent.KEYCODE_NUMPAD_7 -> '7'
            KeyEvent.KEYCODE_NUMPAD_8 -> '8'
            KeyEvent.KEYCODE_NUMPAD_9 -> '9'
            else -> null
        }
        if (numpadDigit != null) return numpadDigit

        val allowTopRow = prefs.getBoolean(KEY_ALLOW_TOP_ROW_ALT_CODES, true)
        if (!allowTopRow) return null

        return when (keyCode) {
            KeyEvent.KEYCODE_0 -> '0'
            KeyEvent.KEYCODE_1 -> '1'
            KeyEvent.KEYCODE_2 -> '2'
            KeyEvent.KEYCODE_3 -> '3'
            KeyEvent.KEYCODE_4 -> '4'
            KeyEvent.KEYCODE_5 -> '5'
            KeyEvent.KEYCODE_6 -> '6'
            KeyEvent.KEYCODE_7 -> '7'
            KeyEvent.KEYCODE_8 -> '8'
            KeyEvent.KEYCODE_9 -> '9'
            else -> null
        }
    }

    class ImeActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val service = activeService?.get()
            when (intent.action) {
                ACTION_SHOW_KEYBOARD_PICKER -> {
                    if (service != null) {
                        service.showKeyboardPicker()
                    } else {
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                }
                ACTION_TOGGLE_LANGUAGE -> service?.toggleLayout(showToast = true)
            }
        }
    }

    private class AltCodeEngine {
        private val buffer = StringBuilder()
        private var active = false

        fun start() {
            active = true
            buffer.clear()
        }

        fun appendDigit(digit: Char) {
            if (!active) active = true
            if (buffer.length < MAX_ALT_CODE_LENGTH) buffer.append(digit)
        }

        fun preview(): String = if (active) buffer.toString() else ""

        fun cancel() {
            active = false
            buffer.clear()
        }

        fun finish(): String? {
            if (!active || buffer.isEmpty()) {
                cancel()
                return null
            }
            val code = buffer.toString().toIntOrNull()
            cancel()
            return code?.let { resolveAltCode(it) }
        }

        private fun resolveAltCode(code: Int): String? {
            if (code == 164 || code == 8377) return "₹"
            if (code in 32..126) return code.toChar().toString()
            if (code in 2304..2431) return String(Character.toChars(code))
            return cp1252(code)
        }

        private fun cp1252(code: Int): String? {
            return when (code) {
                128 -> "€"
                130 -> "‚"
                131 -> "ƒ"
                132 -> "„"
                133 -> "…"
                134 -> "†"
                135 -> "‡"
                136 -> "ˆ"
                137 -> "‰"
                138 -> "Š"
                139 -> "‹"
                140 -> "Œ"
                142 -> "Ž"
                145 -> "‘"
                146 -> "’"
                147 -> "“"
                148 -> "”"
                149 -> "•"
                150 -> "–"
                151 -> "—"
                152 -> "˜"
                153 -> "™"
                154 -> "š"
                155 -> "›"
                156 -> "œ"
                158 -> "ž"
                159 -> "Ÿ"
                160 -> "\u00A0"
                161 -> "¡"
                162 -> "¢"
                163 -> "£"
                165 -> "¥"
                166 -> "¦"
                167 -> "§"
                168 -> "¨"
                169 -> "©"
                170 -> "ª"
                171 -> "«"
                172 -> "¬"
                173 -> "\u00AD"
                174 -> "®"
                175 -> "¯"
                176 -> "°"
                177 -> "±"
                178 -> "²"
                179 -> "³"
                180 -> "´"
                181 -> "µ"
                182 -> "¶"
                183 -> "·"
                184 -> "¸"
                else -> null
            }
        }
    }

    companion object {
        const val PREFS_NAME = "bharatkey_ime_prefs"
        const val KEY_LAYOUT_MODE = "layout_mode"
        const val KEY_ALLOW_TOP_ROW_ALT_CODES = "allow_top_row_alt_codes"

        private const val CHANNEL_ID = "bharatkey_ime_language_status"
        private const val NOTIFICATION_ID = 7001
        private const val MAX_ALT_CODE_LENGTH = 6

        private const val ACTION_SHOW_KEYBOARD_PICKER = "com.irashad1707.bharatkeyime.ACTION_SHOW_KEYBOARD_PICKER"
        private const val ACTION_TOGGLE_LANGUAGE = "com.irashad1707.bharatkeyime.ACTION_TOGGLE_LANGUAGE"

        private var activeService: WeakReference<CustomWinIME>? = null
    }
}
