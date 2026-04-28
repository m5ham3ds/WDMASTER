package com.wdmaster.app.di

import com.wdmaster.app.presentation.MainViewModel
import com.wdmaster.app.presentation.home.HomeViewModel
import com.wdmaster.app.presentation.test.TestViewModel
import com.wdmaster.app.presentation.settings.SettingsViewModel
import com.wdmaster.app.presentation.settings.RouterManagerViewModel
import com.wdmaster.app.presentation.history.HistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { MainViewModel() }

    viewModel {
        HomeViewModel(
            generateCardsUseCase = get(),
            testBatchUseCase = get(),
            cardRepository = get(),
            sessionRepository = get(),
            patternLearningUseCase = get(),
            routerRepository = get(),
            exportResultsUseCase = get()
        )
    }

    viewModel {
        TestViewModel(
            testCardUseCase = get(),
            testBatchUseCase = get(),
            manageRoutersUseCase = get(),
            patternLearningUseCase = get(),
            app = get()   // ← يحصل على Application من Koin مباشرة
        )
    }

    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            themePreferences = get()
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
