package com.irashad1707.bharatkeyime

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.InputDevice
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class BharatKeySettingsActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences(CustomWinIME.PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "BharatKey IME"

        val root = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 36)
        }
        root.addView(layout)

        layout.addView(ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            adjustViewBounds = true
            maxWidth = 180
            maxHeight = 180
            layoutParams = LinearLayout.LayoutParams(180, 180).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = 18
            }
        })

        layout.addView(titleText("BharatKey IME"))
        layout.addView(bodyText("Hindi & English Physical Keyboard for Android mobile and tablet devices."))

        val keyboardStatus = if (isPhysicalKeyboardAttached()) {
            "Physical keyboard status: Connected"
        } else {
            "Physical keyboard status: Not connected — connect USB OTG, Bluetooth, or tablet keyboard cover."
        }
        layout.addView(bodyText(keyboardStatus))

        layout.addView(sectionText("Language Names"))
        layout.addView(bodyText("English: English US / EN"))
        layout.addView(bodyText("Hindi: Devanagari InScript / INS"))

        layout.addView(sectionText("Privacy Notice"))
        layout.addView(bodyText("BharatKey IME processes physical keyboard input locally on your device. It does not collect, upload, or sell typed data."))

        val defaultHindiSwitch = Switch(this).apply {
            text = "Start with Devanagari InScript by default"
            isChecked = prefs.getString(
                CustomWinIME.KEY_LAYOUT_MODE,
                InscriptEngine.LayoutMode.ENGLISH_US.name
            ) == InscriptEngine.LayoutMode.HINDI_CUSTOM.name

            setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                val mode = if (checked) InscriptEngine.LayoutMode.HINDI_CUSTOM else InscriptEngine.LayoutMode.ENGLISH_US
                prefs.edit().putString(CustomWinIME.KEY_LAYOUT_MODE, mode.name).apply()
            }
        }

        val topRowAltSwitch = Switch(this).apply {
            text = "Allow top-row numbers for Alt codes"
            isChecked = prefs.getBoolean(CustomWinIME.KEY_ALLOW_TOP_ROW_ALT_CODES, true)
            setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                prefs.edit().putBoolean(CustomWinIME.KEY_ALLOW_TOP_ROW_ALT_CODES, checked).apply()
            }
        }

        layout.addView(sectionText("Typing Settings"))
        layout.addView(defaultHindiSwitch)
        layout.addView(topRowAltSwitch)

        layout.addView(sectionText("Keyboard Setup"))
        layout.addView(Button(this).apply {
            text = "Open Keyboard Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })

        layout.addView(Button(this).apply {
            text = "Change Keyboard / Select Gboard"
            setOnClickListener { getSystemService(InputMethodManager::class.java).showInputMethodPicker() }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            layout.addView(Button(this).apply {
                text = "Allow Notification Permission"
                setOnClickListener {
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
                    }
                }
            })
        }

        layout.addView(sectionText("How to Use"))
        layout.addView(bodyText("Important: BharatKey IME only works when a real physical keyboard is attached."))
        layout.addView(bodyText("Alt + Shift: Toggle EN / INS"))
        layout.addView(bodyText("Alt + Numpad Code: Windows-style Alt code input"))
        layout.addView(bodyText("Alt + 8377 or Alt + 164: ₹"))
        layout.addView(bodyText("Alt + 2335: क"))
        layout.addView(bodyText("Ctrl shortcuts like Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+S, Ctrl+F are passed to apps."))
        layout.addView(bodyText("Arrow keys, Home, End, Page Up, and Page Down are passed to apps."))

        setContentView(root)
    }

    private fun isPhysicalKeyboardAttached(): Boolean {
        return InputDevice.getDeviceIds().any { id ->
            val device = InputDevice.getDevice(id) ?: return@any false
            val hasKeyboardSource =
                device.sources and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD

            !device.isVirtual &&
                hasKeyboardSource &&
                device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
        }
    }

    private fun titleText(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 24f
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, 0, 0, 14)
    }

    private fun sectionText(text: String): TextView = TextView(this).apply {
        this.text = "\n$text"
        textSize = 18f
        setPadding(0, 20, 0, 8)
    }

    private fun bodyText(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setPadding(0, 4, 0, 4)
    }
}
