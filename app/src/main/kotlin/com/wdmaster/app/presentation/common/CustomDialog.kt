package com.wdmaster.app.presentation.common

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class CustomDialog<VB : ViewBinding> : DialogFragment() {

    protected var _binding: VB? = null
    val binding: VB get() = _binding!!

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = inflateBinding(LayoutInflater.from(requireContext()))
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}