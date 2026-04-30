package com.wdmaster.app.presentation.common

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

fun Fragment.handleBackPress(onBackPressed: () -> Boolean) {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!onBackPressed()) {
                    // إذا لم يتم التعامل معه، اسمح بالسلوك الافتراضي (الخروج)
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
    )
}