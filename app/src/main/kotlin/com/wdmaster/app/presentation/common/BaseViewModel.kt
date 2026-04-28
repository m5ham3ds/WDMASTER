package com.wdmaster.app.presentation.common

import androidx.lifecycle.ViewModel
import com.wdmaster.app.util.Logger

open class BaseViewModel : ViewModel() {
    open val logger: Logger = Logger(javaClass.simpleName)
}