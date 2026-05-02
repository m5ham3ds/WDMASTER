package com.wdmaster.app

import android.app.Application
import android.content.res.Configuration
import com.wdmaster.app.data.local.entity.RouterProfileEntity
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.di.appModule
import com.wdmaster.app.di.viewModelModule
import com.wdmaster.app.util.AppLogger
import com.wdmaster.app.util.CrashLogger
import com.wdmaster.app.util.LocaleHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class WDMasterApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        CrashLogger.init(this)
        AppLogger.init(this)

        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, tag, message, t)
                    val level = when (priority) {
                        android.util.Log.ERROR -> "ERROR"
                        android.util.Log.WARN -> "WARNING"
                        android.util.Log.INFO -> "INFO"
                        else -> "DEBUG"
                    }
                    AppLogger.log(level, tag ?: "Timber", message, t)
                }
            })
        }

        AppLogger.i("WDMasterApp", "Application started")
        AppLogger.i("WDMasterApp", "Device: ${android.os.Build.MODEL}, Android: ${android.os.Build.VERSION.RELEASE}")

        startKoin {
            androidContext(this@WDMasterApp)
            modules(appModule, viewModelModule)
        }

        LocaleHelper.setLocale(this, LocaleHelper.getPersistedLocale(this))
        insertDefaultRouterIfNeeded()
    }

    private fun insertDefaultRouterIfNeeded() {
    appScope.launch {
        try {
            val routerRepository: RouterRepository by inject(RouterRepository::class.java)
            val routers = routerRepository.allRouters.first()
            if (routers.isEmpty()) {
                routerRepository.insertRouter(
                    RouterProfileEntity(
                        name = "شبكة معتصم نت",
                        ip = "10.0.0.1",
                        port = 80,
                        protocol = "http",
                        username = "",
                        password = "",
                        loginPath = "/login",
                        usernameSelector = "#username",
                        passwordSelector = "",
                        submitSelector = "",
                        successIndicator = "تفاصيل الأستخدام",
                        failureIndicator = "ادخل الرمز",
                        customJs = null,
                        authType = "FORM",
                        isActive = true,
                        isDefault = true
                    )
                )
                AppLogger.i("WDMasterApp", "Default router inserted successfully")
            }
        } catch (e: Exception) {
            AppLogger.log("ERROR", "WDMasterApp", "Failed to insert default router", e)
        }
    }
}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onConfigurationChanged(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }
}
