package com.yxf.rxandroidextensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.yxf.rxandroidextensions.activity.ActivityResult
import com.yxf.rxandroidextensions.activity.ObservableStartContractForResult
import com.yxf.rxandroidextensions.activity.PermissionResult
import com.yxf.rxandroidextensions.activity.RxAndroidExtensionsFragment
import com.yxf.rxandroidextensions.lifecycle.*
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins


private class RxAndroidExtensions {

    companion object {
        val TAG = RxAndroidExtensions::class.qualifiedName

        const val AUTOMATIC_REQUEST_CODE = -1
        const val FIRST_DEFAULT_ID = 31 * 53
        const val MAX_REQUEST_ID = Short.MAX_VALUE
        var defaultId = FIRST_DEFAULT_ID
        private var automaticRequestId = FIRST_DEFAULT_ID
            get() {
                if (defaultId > MAX_REQUEST_ID) {
                    defaultId = FIRST_DEFAULT_ID
                }
                return defaultId++
            }


        fun requestId(): Int {
            return automaticRequestId
        }

    }

}

private typealias Ex = RxAndroidExtensions

private val handler by lazy { Handler(Looper.getMainLooper()) }

@Deprecated("should use result api instead")
private fun getFragment(activity: FragmentActivity): RxAndroidExtensionsFragment {
    return activity.supportFragmentManager.findFragmentByTag(Ex.TAG)
        ?.let { return@let (it as RxAndroidExtensionsFragment) }
        ?: RxAndroidExtensionsFragment().also {
            activity.supportFragmentManager.beginTransaction().add(it, Ex.TAG)
                .commitNow()
        }
}

internal fun isPermissionGranted(activity: Activity, permission: String) =
    ActivityCompat.checkSelfPermission(
        activity,
        permission
    ) == PackageManager.PERMISSION_GRANTED

internal fun runOnMainThread(runnable: Runnable) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        handler.post(runnable)
    } else {
        runnable.run()
    }
}

internal fun runOnMainThreadSync(runnable: Runnable) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        val lock = Object()
        handler.post {
            try {
                runnable.run()
            } finally {
                lock.notify()
            }
        }
        lock.wait()
    } else {
        runnable.run()
    }
}

fun FragmentActivity.rxRequestSinglePermission(
    permission: String,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<Boolean> {
    if (isPermissionGranted(this, permission)) {
        return Observable.just(true)
    }
    return rxStartContractForResult(ActivityResultContracts.RequestPermission(), permission)
}


fun FragmentActivity.rxRequestPermissions(
    permissions: Array<String>,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<PermissionResult> {
    val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
    return rxStartContractForResult(ActivityResultContracts.RequestMultiplePermissions(), permissions)
        .map { m ->
            PermissionResult(
                code,
                permissions,
                permissions.map { p -> if (m[p] == true) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED }
                    .toIntArray()
            )
        }
}

fun FragmentActivity.rxStartActivityWaitingForResume(
    intent: Intent,
    options: Bundle? = null,
): Observable<Any> {
    if (options == null) {
        startActivity(intent)
    } else {
        startActivity(intent, options)
    }
    return registerLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        .flatMap {
            registerLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
}

@Deprecated("use rxStartActivityForResult instead")
fun FragmentActivity.rxStartActivityForResultByFragment(
    intent: Intent,
    options: Bundle? = null,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<ActivityResult> {
    val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
    val fragment = getFragment(this)
    return fragment.run {
        val observable: Observable<ActivityResult> = getObservable(code)
        startActivityForResult(intent, code, options)
        return@run observable
    }
}

fun FragmentActivity.rxStartActivityForResult(
    intent: Intent,
    options: Bundle? = null,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<ActivityResult> {
    val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
    return rxStartContractForResult(ActivityResultContracts.StartActivityForResult(), intent.apply {
        options?.let {
            putExtra(ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE, options)
        }
    }).map {
        ActivityResult(code, it.resultCode, it.data)
    }
}


fun <I, O> FragmentActivity.rxStartContractForResult(contract: ActivityResultContract<I, O>, input: I): Observable<O> {
    return RxJavaPlugins.onAssembly(ObservableStartContractForResult(this, contract, input))
}


fun FragmentActivity.rxRequestInstallPackagesPermission(): Observable<Boolean> {
    val uri = Uri.parse("package:$packageName")
    val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        packageManager.canRequestPackageInstalls()
    } else {
        true
    }
    if (granted) {
        return Observable.just(true)
    }
    return rxStartActivityForResult(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)).map {
        return@map it.isOk
    }
}

fun Fragment.rxRequestSinglePermission(
    permission: String,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<Boolean> {
    return requireActivity().rxRequestSinglePermission(permission, requestCode)
}


fun Fragment.rxRequestPermissions(
    permissions: Array<String>,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<PermissionResult> {
    return requireActivity().rxRequestPermissions(permissions, requestCode)
}

fun Fragment.rxStartActivityForResult(
    intent: Intent,
    options: Bundle? = null,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<ActivityResult> {
    return requireActivity().rxStartActivityForResult(intent, options, requestCode)
}

fun Fragment.rxRequestInstallPackagesPermission(): Observable<Boolean> {
    return requireActivity().rxRequestInstallPackagesPermission()
}

fun LifecycleOwner.registerLifecycleEvent(vararg eventArray: Lifecycle.Event, once: Boolean = true): Observable<Lifecycle.Event> {
    return RxJavaPlugins.onAssembly(ObservableLifecycle(this, HashSet<Lifecycle.Event>().apply { addAll(eventArray) }, once))
}

fun <T> Observable<T>.autoDispose(disposeSource: DisposeSource): Observable<T> {
    return RxJavaPlugins.onAssembly(ObservableAutoDispose<T>(disposeSource, this))
}

fun <T> Observable<T>.autoDispose(owner: LifecycleOwner, vararg eventSet: Lifecycle.Event): Observable<T> {
    return autoDispose(LifeCycleDisposeSource(owner, eventSet.toHashSet()))
}

fun <T> Observable<T>.disposeOnDestroy(owner: LifecycleOwner): Observable<T> {
    return autoDispose(owner, Lifecycle.Event.ON_DESTROY)
}

fun <T> Observable<T>.disposeOnPause(owner: LifecycleOwner): Observable<T> {
    return autoDispose(owner, Lifecycle.Event.ON_PAUSE)
}

fun <T> Observable<T>.disposeOnPauseAndDestroy(owner: LifecycleOwner): Observable<T> {
    return autoDispose(owner, Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_DESTROY)
}













