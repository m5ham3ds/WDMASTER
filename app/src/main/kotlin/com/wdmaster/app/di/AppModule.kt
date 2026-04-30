package com.wdmaster.app.di

import androidx.room.Room
import com.wdmaster.app.data.local.database.AppDatabase
import com.wdmaster.app.data.local.preferences.AppPreferences
import com.wdmaster.app.data.local.preferences.ThemePreferences
import com.wdmaster.app.data.repository.*
import com.wdmaster.app.domain.usecase.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {

    // ── Room Database ─────────────────────────────
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // ── DAOs ──────────────────────────────────────
    single { get<AppDatabase>().cardDao() }
    single { get<AppDatabase>().routerProfileDao() }
    single { get<AppDatabase>().testResultDao() }
    single { get<AppDatabase>().patternDao() }
    single { get<AppDatabase>().sessionDao() }

    // ── Preferences ────────────────────────────────
    single { AppPreferences(androidApplication()) }
    single { ThemePreferences(androidApplication()) }

    // ── Repositories ───────────────────────────────
    single { CardRepository(get()) }
    single { RouterRepository(get()) }
    single { TestResultRepository(get()) }
    single { PatternRepository(get()) }
    single { SessionRepository(get()) }
    single { SettingsRepository(get(), get()) }

    // ── Use Cases ──────────────────────────────────
    single { GenerateCardsUseCase(get()) }
    // تمت إزالة: TestCardUseCase, TestBatchUseCase, PatternLearningUseCase
    single { ManageRoutersUseCase(get()) }
    single { ExportResultsUseCase(get()) }
    single { ImportResultsUseCase(get()) }
}