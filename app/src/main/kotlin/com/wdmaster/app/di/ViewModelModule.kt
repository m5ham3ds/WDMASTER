package com.wdmaster.app.di

import com.wdmaster.app.presentation.MainViewModel
import com.wdmaster.app.presentation.home.HomeViewModel
import com.wdmaster.app.presentation.test.TestViewModel
import com.wdmaster.app.presentation.settings.SettingsViewModel
import com.wdmaster.app.presentation.settings.RouterManagerViewModel
import com.wdmaster.app.presentation.history.HistoryViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { MainViewModel() }

    viewModel {
        HomeViewModel(
            generateCardsUseCase = get(),
            sessionRepository = get(),
            routerRepository = get(),
            testResultRepository = get()
        )
    }

    viewModel {
        TestViewModel(
            app = androidApplication()
        )
    }

    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            themePreferences = get(),
            testResultRepository = get(),
            sessionRepository = get()
        )
    }

    viewModel {
        RouterManagerViewModel(
            manageRoutersUseCase = get(),
            settingsRepository = get()
        )
    }

    viewModel {
        HistoryViewModel(
            testResultRepository = get(),
            sessionRepository = get(),
            exportResultsUseCase = get()
        )
    }
}
