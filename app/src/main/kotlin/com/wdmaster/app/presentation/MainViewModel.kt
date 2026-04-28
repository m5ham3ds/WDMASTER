package com.wdmaster.app.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wdmaster.app.presentation.common.BaseViewModel

class MainViewModel : BaseViewModel() {

    private val _toolbarTitle = MutableLiveData("")
    val toolbarTitle: LiveData<String> = _toolbarTitle

    fun setToolbarTitle(title: String) {
        _toolbarTitle.value = title
    }
}