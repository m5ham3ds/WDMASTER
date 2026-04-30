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

        // تفعيل التقاط الأعطال أولاً
        CrashLogger.init(this)

        // تفعيل المسجِّل المركزي
        AppLogger.init(this)

        // زرع Timber ليرسل السجلات إلى AppLogger أيضاً
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
                        submitSelector = "",
                        successIndicator = "تفاصيل الأستخدام",
                        failureIndicator = "ادخل الرمز",
                        customJs = """
                            // انتظر حتى يكتمل تحميل الصفحة بالكامل (DOM جاهز)
                            function waitForReady() {
                                return new Promise(resolve => {
                                    if (document.readyState === 'complete') {
                                        resolve();
                                    } else {
                                        window.addEventListener('load', resolve);
                                    }
                                });
                            }

                            // ابدأ العمل بعد جهوزية الصفحة
                            waitForReady().then(() => {

                                // ======================================================
                                // 1. هل نحن في صفحة تسجيل الدخول؟
                                // ======================================================
                                var u = document.querySelector('input[name=username]');
                                var p = document.querySelector('input[name=password]');
                                if (u && p) {
                                    // ضع رمز البطاقة (CARD_PLACEHOLDER سيُستبدل تلقائياً بالبطاقة الحالية)
                                    u.value = 'CARD_PLACEHOLDER';
                                    p.value = '';  // اترك كلمة المرور فارغة، لأن doLogin ستولّدها

                                    // أطلق حدث "input" لضمان تفعيل أي مستمعين مرتبطين بالحقل
                                    u.dispatchEvent(new Event('input', { bubbles: true }));

                                    // استدعِ دالة doLogin الأصلية التي تبني كلمة المرور وترسل النموذج
                                    if (typeof doLogin === 'function') {
                                        doLogin();                        // تنفيذ تسجيل الدخول
                                        AndroidBridge.onResult('submitted'); // أبلغ التطبيق أن العملية تمت
                                    } else {
                                        // في حال عدم وجود doLogin، ابحث عن النموذج وارسله
                                        var f = document.forms['login'] || document.forms[0];
                                        if (f) f.submit();
                                        AndroidBridge.onResult('submitted');
                                    }
                                    return;
                                }

                                // ======================================================
                                // 2. هل نحن في صفحة النجاح؟
                                // ======================================================
                                var bodyHTML = document.body.innerHTML;
                                var bodyText = document.body.innerText || '';
                                if (bodyHTML.indexOf('تفاصيل الأستخدام') !== -1 ||
                                    bodyText.indexOf('نجاح') !== -1) {

                                    // حاول استخراج الوقت المتبقي (اختياري، للعلم فقط)
                                    var t = document.getElementById('timeLeft');
                                    var timeInfo = t ? t.innerText : '';

                                    // أبلغ التطبيق أن البطاقة ناجحة
                                    AndroidBridge.onResult('success|' + timeInfo);

                                    // ======== تسجيل الخروج ========
                                    // الأفضلية دائماً لاستدعاء openLogout() إن وُجدت
                                    if (typeof openLogout === 'function') {
                                        openLogout();  // هذه الدالة تفتح نافذة الخروج وتغلق الحالية
                                    } else {
                                        // خطة بديلة: النقر على زر "تسجيل الخروج"
                                        var logoutBtn = document.querySelector('a[href*="logout"], button');
                                        if (logoutBtn) logoutBtn.click();
                                    }
                                    return;
                                }

                                // ======================================================
                                // 3. لا صفحة دخول ولا صفحة نجاح ← البطاقة فاشلة
                                // ======================================================
                                AndroidBridge.onResult('failure');
                            });
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