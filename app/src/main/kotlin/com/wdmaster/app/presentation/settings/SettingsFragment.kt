package com.wdmaster.app.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wdmaster.app.R
import com.wdmaster.app.databinding.FragmentSettingsBinding
import com.wdmaster.app.presentation.common.BaseFragment
import com.wdmaster.app.presentation.common.ThemeManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsPreferenceFragment())
            .commit()

        // مراقبة أحداث ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is SettingsViewModel.UiEvent.NavigateToRouterManager -> {
                        findNavController().navigate(R.id.nav_router_manager)
                    }
                    is SettingsViewModel.UiEvent.ShowClearHistoryDialog -> {
                        showClearHistoryDialog()
                    }
                    is SettingsViewModel.UiEvent.ShowMessage -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                    is SettingsViewModel.UiEvent.OpenGitHub -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/m5ham/WDMASTER"))
                        startActivity(intent)
                    }
                    is SettingsViewModel.UiEvent.ExportDatabase -> {
                        // يمكن ربطه مع HistoryViewModel أو استخدام ExportResultsUseCase
                    }
                }
            }
        }
    }

    private fun showClearHistoryDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Dialog)
            .setTitle("مسح جميع السجلات")
            .setMessage("هل أنت متأكد من حذف جميع الجلسات والنتائج بشكل دائم؟ لا يمكن التراجع عن هذا الإجراء.")
            .setPositiveButton("نعم، امسح الكل") { _, _ ->
                viewModel.confirmClearHistory()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    class SettingsPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

            val parentFragment = this.parentFragment as? SettingsFragment
            val viewModel = parentFragment?.viewModel ?: return

            val themePref = findPreference<ListPreference>("theme")
            themePref?.setOnPreferenceChangeListener { _, newValue ->
                val mode = newValue as String
                viewModel.setThemeMode(mode)
                ThemeManager.applyTheme(mode)
                requireActivity().recreate()
                true
            }

            val languagePref = findPreference<ListPreference>("app_language")
            languagePref?.setOnPreferenceChangeListener { _, newValue ->
                viewModel.setAppLanguage(newValue as String)
                requireActivity().recreate()
                true
            }

            val manageRoutersPref = findPreference<Preference>("manage_routers")
            manageRoutersPref?.setOnPreferenceClickListener {
                viewModel.navigateToRouterManager()
                true
            }

            val clearHistoryPref = findPreference<Preference>("clear_history")
            clearHistoryPref?.setOnPreferenceClickListener {
                viewModel.clearHistory()
                true
            }

            val exportDbPref = findPreference<Preference>("export_db")
            exportDbPref?.setOnPreferenceClickListener {
                viewModel.exportDatabase()
                true
            }

            findPreference<Preference>("version")?.summary = "1.0.0"

            val githubPref = findPreference<Preference>("github")
            githubPref?.setOnPreferenceClickListener {
                viewModel.openGitHub()
                true
            }
        }
    }
}
