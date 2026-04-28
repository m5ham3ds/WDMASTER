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
                        name = "شبكة معتصم نت",
                        ip = "192.168.1.1",
                        port = 80,
                        protocol = "http",
                        username = "",
                        password = "",
                        loginPath = "/login",
                        usernameSelector = "input[name=username]",
                        passwordSelector = "input[name=password]",
                        submitSelector = "",  // لا نستخدمه، customJs يتولى الأمر
                        successIndicator = "تفاصيل الأستخدام",
                        failureIndicator = "ادخل الرمز",
                        customJs = """
                            function waitForReady() {
                                return new Promise(resolve => {
                                    if (document.readyState === 'complete') {
                                        resolve();
                                    } else {
                                        window.addEventListener('load', resolve);
                                    }
                                });
                            }

                            waitForReady().then(() => {
                                if (document.querySelector('input[name=username]')) {
                                    var u = document.querySelector('input[name=username]');
                                    var p = document.querySelector('input[name=password]');
                                    if (!u || !p) { AndroidBridge.onResult('fields_not_found'); return; }
                                    u.value = 'CARD_PLACEHOLDER';
                                    p.value = '';
                                    u.dispatchEvent(new Event('input', { bubbles: true }));
                                    if (typeof doLogin === 'function') {
                                        doLogin();
                                        AndroidBridge.onResult('submitted');
                                    }
                                    return;
                                }
                                if (document.body.innerHTML.indexOf('تفاصيل الأستخدام') !== -1) {
                                    var t = document.getElementById('timeLeft');
                                    AndroidBridge.onResult('success|' + (t ? t.innerText : ''));
                                    if (typeof openLogout === 'function') openLogout();
                                    else { var b = document.querySelector('button[type=submit]'); if (b) b.click(); }
                                    return;
                                }
                                AndroidBridge.onResult('failure');
                            });
                        """.trimIndent(),
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
