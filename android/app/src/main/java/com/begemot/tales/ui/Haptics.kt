package com.begemot.tales.ui

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberHapticFeedback(): (Int) -> Unit {
    val view = LocalView.current
    return { feedbackConstant ->
        view.performHapticFeedback(feedbackConstant)
    }
}

object Haptics {
    const val CLICK = HapticFeedbackConstants.KEYBOARD_TAP
    const val TICK = HapticFeedbackConstants.CLOCK_TICK
    const val CONFIRM = HapticFeedbackConstants.CONFIRM
}
