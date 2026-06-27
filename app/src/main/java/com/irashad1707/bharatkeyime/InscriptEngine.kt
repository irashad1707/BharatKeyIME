package com.irashad1707.bharatkeyime

import android.view.KeyEvent

object InscriptEngine {

    enum class LayoutMode(
        val fullName: String,
        val shortName: String
    ) {
        ENGLISH_US("English US", "EN"),
        HINDI_CUSTOM("Devanagari InScript", "INS")
    }

    fun translate(
        keyCode: Int,
        shift: Boolean,
        capsLock: Boolean,
        layoutMode: LayoutMode
    ): String? {
        return when (layoutMode) {
            LayoutMode.ENGLISH_US -> englishUs(keyCode, shift, capsLock)
            LayoutMode.HINDI_CUSTOM -> hindiCustom(keyCode, shift)
        }
    }

    private fun englishUs(keyCode: Int, shift: Boolean, capsLock: Boolean): String? {
        val upper = shift xor capsLock

        return when (keyCode) {
            KeyEvent.KEYCODE_1 -> if (shift) "!" else "1"
            KeyEvent.KEYCODE_2 -> if (shift) "@" else "2"
            KeyEvent.KEYCODE_3 -> if (shift) "#" else "3"
            KeyEvent.KEYCODE_4 -> if (shift) "$" else "4"
            KeyEvent.KEYCODE_5 -> if (shift) "%" else "5"
            KeyEvent.KEYCODE_6 -> if (shift) "^" else "6"
            KeyEvent.KEYCODE_7 -> if (shift) "&" else "7"
            KeyEvent.KEYCODE_8 -> if (shift) "*" else "8"
            KeyEvent.KEYCODE_9 -> if (shift) "(" else "9"
            KeyEvent.KEYCODE_0 -> if (shift) ")" else "0"

            KeyEvent.KEYCODE_A -> if (upper) "A" else "a"
            KeyEvent.KEYCODE_B -> if (upper) "B" else "b"
            KeyEvent.KEYCODE_C -> if (upper) "C" else "c"
            KeyEvent.KEYCODE_D -> if (upper) "D" else "d"
            KeyEvent.KEYCODE_E -> if (upper) "E" else "e"
            KeyEvent.KEYCODE_F -> if (upper) "F" else "f"
            KeyEvent.KEYCODE_G -> if (upper) "G" else "g"
            KeyEvent.KEYCODE_H -> if (upper) "H" else "h"
            KeyEvent.KEYCODE_I -> if (upper) "I" else "i"
            KeyEvent.KEYCODE_J -> if (upper) "J" else "j"
            KeyEvent.KEYCODE_K -> if (upper) "K" else "k"
            KeyEvent.KEYCODE_L -> if (upper) "L" else "l"
            KeyEvent.KEYCODE_M -> if (upper) "M" else "m"
            KeyEvent.KEYCODE_N -> if (upper) "N" else "n"
            KeyEvent.KEYCODE_O -> if (upper) "O" else "o"
            KeyEvent.KEYCODE_P -> if (upper) "P" else "p"
            KeyEvent.KEYCODE_Q -> if (upper) "Q" else "q"
            KeyEvent.KEYCODE_R -> if (upper) "R" else "r"
            KeyEvent.KEYCODE_S -> if (upper) "S" else "s"
            KeyEvent.KEYCODE_T -> if (upper) "T" else "t"
            KeyEvent.KEYCODE_U -> if (upper) "U" else "u"
            KeyEvent.KEYCODE_V -> if (upper) "V" else "v"
            KeyEvent.KEYCODE_W -> if (upper) "W" else "w"
            KeyEvent.KEYCODE_X -> if (upper) "X" else "x"
            KeyEvent.KEYCODE_Y -> if (upper) "Y" else "y"
            KeyEvent.KEYCODE_Z -> if (upper) "Z" else "z"

            KeyEvent.KEYCODE_EQUALS -> if (shift) "+" else "="
            KeyEvent.KEYCODE_MINUS -> if (shift) "_" else "-"
            KeyEvent.KEYCODE_LEFT_BRACKET -> if (shift) "{" else "["
            KeyEvent.KEYCODE_RIGHT_BRACKET -> if (shift) "}" else "]"
            KeyEvent.KEYCODE_BACKSLASH -> if (shift) "|" else "\\"
            KeyEvent.KEYCODE_SEMICOLON -> if (shift) ":" else ";"
            KeyEvent.KEYCODE_APOSTROPHE -> if (shift) "\"" else "'"
            KeyEvent.KEYCODE_COMMA -> if (shift) "<" else ","
            KeyEvent.KEYCODE_PERIOD -> if (shift) ">" else "."
            KeyEvent.KEYCODE_SLASH -> if (shift) "?" else "/"
            KeyEvent.KEYCODE_GRAVE -> if (shift) "~" else "`"
            KeyEvent.KEYCODE_SPACE -> " "
            else -> null
        }
    }

    private fun hindiCustom(keyCode: Int, shift: Boolean): String? {
        return when (keyCode) {
            KeyEvent.KEYCODE_1 -> if (shift) "ऍ" else "1"
            KeyEvent.KEYCODE_2 -> if (shift) "ॅ" else "2"
            KeyEvent.KEYCODE_3 -> if (shift) "्र" else "3"
            KeyEvent.KEYCODE_4 -> if (shift) "र्" else "4"
            KeyEvent.KEYCODE_5 -> if (shift) "ज्ञ" else "5"
            KeyEvent.KEYCODE_6 -> if (shift) "त्र" else "6"
            KeyEvent.KEYCODE_7 -> if (shift) "क्ष" else "7"
            KeyEvent.KEYCODE_8 -> if (shift) "श्र" else "8"
            KeyEvent.KEYCODE_9 -> if (shift) "(" else "9"
            KeyEvent.KEYCODE_0 -> if (shift) ")" else "0"

            KeyEvent.KEYCODE_EQUALS -> if (shift) "ऋ" else "ृ"

            KeyEvent.KEYCODE_Q -> if (shift) "औ" else "ौ"
            KeyEvent.KEYCODE_W -> if (shift) "ऐ" else "ै"
            KeyEvent.KEYCODE_E -> if (shift) "आ" else "ा"
            KeyEvent.KEYCODE_R -> if (shift) "ई" else "ी"
            KeyEvent.KEYCODE_T -> if (shift) "ऊ" else "ू"
            KeyEvent.KEYCODE_Y -> if (shift) "भ" else "ब"
            KeyEvent.KEYCODE_U -> if (shift) "ङ" else "ह"
            KeyEvent.KEYCODE_I -> if (shift) "घ" else "ग"
            KeyEvent.KEYCODE_O -> if (shift) "ध" else "द"
            KeyEvent.KEYCODE_P -> if (shift) "झ" else "ज"
            KeyEvent.KEYCODE_LEFT_BRACKET -> if (shift) "ढ" else "ड"
            KeyEvent.KEYCODE_RIGHT_BRACKET -> if (shift) "ञ" else "़"

            KeyEvent.KEYCODE_A -> if (shift) "ओ" else "ो"
            KeyEvent.KEYCODE_S -> if (shift) "ए" else "े"
            KeyEvent.KEYCODE_D -> if (shift) "अ" else "्"
            KeyEvent.KEYCODE_F -> if (shift) "इ" else "ि"
            KeyEvent.KEYCODE_G -> if (shift) "उ" else "ु"
            KeyEvent.KEYCODE_H -> if (shift) "फ" else "प"
            KeyEvent.KEYCODE_J -> if (shift) "ऱ" else "र"
            KeyEvent.KEYCODE_K -> if (shift) "ख" else "क"
            KeyEvent.KEYCODE_L -> if (shift) "थ" else "त"
            KeyEvent.KEYCODE_SEMICOLON -> if (shift) "छ" else "च"
            KeyEvent.KEYCODE_APOSTROPHE -> if (shift) "ठ" else "ट"
            KeyEvent.KEYCODE_BACKSLASH -> if (shift) "ऑ" else "ॉ"

            KeyEvent.KEYCODE_Z -> if (shift) "ऎ" else "ॆ"
            KeyEvent.KEYCODE_X -> if (shift) "ँ" else "ं"
            KeyEvent.KEYCODE_C -> if (shift) "ण" else "म"
            KeyEvent.KEYCODE_V -> if (shift) "ऩ" else "न"
            KeyEvent.KEYCODE_B -> if (shift) "ऴ" else "व"
            KeyEvent.KEYCODE_N -> if (shift) "ळ" else "ल"
            KeyEvent.KEYCODE_M -> if (shift) "श" else "स"
            KeyEvent.KEYCODE_COMMA -> if (shift) "ष" else ","
            KeyEvent.KEYCODE_PERIOD -> if (shift) "।" else "."
            KeyEvent.KEYCODE_SLASH -> if (shift) "य़" else "य"

            KeyEvent.KEYCODE_GRAVE -> if (shift) "" else "ॊ"
            KeyEvent.KEYCODE_SPACE -> " "
            else -> null
        }
    }
}
