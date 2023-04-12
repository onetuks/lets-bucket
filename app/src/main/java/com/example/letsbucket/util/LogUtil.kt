package com.example.letsbucket.util

import android.util.Log

object LogUtil {
    private var TAG: String = "MYLogUtil > "

    private var DEVELOPE_MODE: Boolean = true

    fun d(comment: String) {
        if (DEVELOPE_MODE)
            Log.d(TAG, comment)
    }
}