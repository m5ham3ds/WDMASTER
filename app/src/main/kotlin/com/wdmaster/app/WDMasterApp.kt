package com.wdmaster.app

import android.app.Application
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import timber.log.Timber

class WDMasterApp : Application(), KoinComponent {

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

        GlobalScope.launch(Dispatchers.IO) {
            val routerRepository: RouterRepository by inject()
            val routers = routerRepository.allRouters.first()
            if (routers.isEmpty()) {
                routerRepository.insertRouter(
                    RouterProfileEntity(
                        name = "شبكة معتصم نت",
                        ip = "192.168.1.1",
                        port = 80,
                        protocol = "http",
                        username = "",
                        password = "",
                        loginPath = "/login",
                        usernameSelector = "input[name=username]",
                        passwordSelector = "input[name=password]",
                        submitSelector = "",
                        successIndicator = "تفاصيل الأستخدام",
                        failureIndicator = "ادخل الرمز",
                        customJs = """
                            (function() {
                                // انتظر تحميل الصفحة
                                function waitForReady() {
                                    return new Promise(resolve => {
                                        if (document.readyState === 'complete') resolve();
                                        else window.addEventListener('load', resolve);
                                    });
                                }

                                waitForReady().then(() => {
                                    // 1. البحث عن حقول الدخول
                                    var u = document.querySelector('input[name=username]');
                                    var p = document.querySelector('input[name=password]');
                                    if (u && p) {
                                        u.value = 'CARD_PLACEHOLDER';
                                        p.value = '';
                                        u.dispatchEvent(new Event('input', { bubbles: true }));

                                        if (typeof doLogin === 'function') {
                                            doLogin();
                                            AndroidBridge.onResult('submitted');
                                        } else {
                                            var f = document.forms['login'] || document.forms[0];
                                            if (f) { f.submit(); AndroidBridge.onResult('submitted'); }
                                            else AndroidBridge.onResult('no_form');
                                        }
                                        return;
                                    }

                                    // 2. البحث عن مؤشرات النجاح
                                    var bodyHTML = document.body.innerHTML;
                                    if (bodyHTML.indexOf('تفاصيل الأستخدام') !== -1 || bodyHTML.indexOf('نجاح') !== -1) {
                                        AndroidBridge.onResult('success');
                                        if (typeof openLogout === 'function') openLogout();
                                        else {
                                            var logoutBtn = document.querySelector('a[href*="logout"], button');
                                            if (logoutBtn) logoutBtn.click();
                                        }
                                        return;
                                    }

                                    // 3. فشل
                                    AndroidBridge.onResult('failure');
                                });
                            })();
                        """.trimIndent(),
                        authType = "FORM",
                        isActive = true,
                        isDefault = true
                    )
                )
                AppLogger.i("WDMasterApp", "Default router inserted successfully")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onConfigurationChanged(this)
    }
}
