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

    // ✅ سكوب منظم بدل GlobalScope
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

        // ✅ إدخال الراوتر الافتراضي
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

                            // ✅ محددات دقيقة
                            usernameSelector = "#username",
                            passwordSelector = "input[name=password]",

                            // ❗ نتركه فارغ لأننا سنستخدم doLogin
                            submitSelector = "",

                            successIndicator = "تفاصيل الأستخدام",
                            failureIndicator = "ادخل الرمز",

                            customJs = """
                                (function() {

                                    function waitForReady(callback) {
                                        if (document.readyState === 'complete') {
                                            callback();
                                        } else {
                                            window.addEventListener('load', callback);
                                        }
                                    }

                                    waitForReady(function() {

                                        var usernameField = document.querySelector('#username');
                                        var passwordField = document.querySelector('input[name=password]');

                                        // ✅ حقن البطاقة
                                        if (usernameField) {
                                            usernameField.value = 'CARD_PLACEHOLDER';
                                            usernameField.dispatchEvent(new Event('input', { bubbles: true }));
                                        }

                                        if (passwordField) {
                                            passwordField.value = '';
                                        }

                                        // ✅ تسجيل الدخول الحقيقي
                                        if (typeof doLogin === 'function') {
                                            doLogin();
                                            AndroidBridge.onResult('submitted');
                                            return;
                                        }

                                        var form = document.forms['login'] || document.forms[0];
                                        if (form) {
                                            form.submit();
                                            AndroidBridge.onResult('submitted');
                                            return;
                                        }

                                        // ❗ fallback
                                        AndroidBridge.onResult('no_form');

                                        // ✅ تحقق من النجاح
                                        setTimeout(function() {
                                            var text = document.body.innerText || '';

                                            if (text.includes('تفاصيل الأستخدام') || text.includes('نجاح')) {
                                                AndroidBridge.onResult('success');

                                                // تسجيل خروج
                                                if (typeof openLogout === 'function') {
                                                    openLogout();
                                                } else {
                                                    var logoutBtn = document.querySelector('a[href*="logout"], button');
                                                    if (logoutBtn) logoutBtn.click();
                                                }

                                            } else if (text.includes('ادخل الرمز')) {
                                                AndroidBridge.onResult('failure');
                                            }

                                        }, 2000);

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
        appScope.cancel() // ✅ تنظيف
    }
}
