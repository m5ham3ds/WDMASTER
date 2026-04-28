package com.wdmaster.app

import android.app.Application
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.di.appModule
import com.wdmaster.app.di.viewModelModule
import com.wdmaster.app.util.CrashLogger
import com.wdmaster.app.util.LocaleHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import timber.log.Timber

class WDMasterApp : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()

        CrashLogger.init(this)

        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@WDMasterApp)
            modules(appModule, viewModelModule)
        }

        LocaleHelper.setLocale(this, LocaleHelper.getPersistedLocale(this))

        // إضافة راوتر افتراضي عند أول تشغيل
        GlobalScope.launch(Dispatchers.IO) {
            val routerRepository: RouterRepository by inject()
            val routers = routerRepository.allRouters.first()
            if (routers.isEmpty()) {
                routerRepository.insertRouter(
                    RouterProfileEntity(
                        name = "MikroTik (192.168.1.1)",
                        ip = "192.168.1.1",
                        port = 80,
                        protocol = "http",
                        username = "admin",
                        password = "",
                        loginPath = "/login",
                        usernameSelector = "input[name=username]",
                        passwordSelector = "input[name=password]",
                        submitSelector = "button[type=submit]",
                        successIndicator = "status=ok",
                        failureIndicator = "error=",
                        customJs = null,
                        authType = "FORM",
                        isActive = true,
                        isDefault = true
                    )
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onConfigurationChanged(this)
    }
}
