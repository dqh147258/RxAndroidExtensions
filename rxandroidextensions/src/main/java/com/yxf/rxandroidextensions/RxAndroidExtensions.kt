package com.yxf.rxandroidextensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.yxf.rxandroidextensions.activity.ActivityResult
import com.yxf.rxandroidextensions.activity.PermissionResult
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


internal fun isPermissionGranted(activity: Activity, permission: String) =
    ActivityCompat.checkSelfPermission(
        activity,
        permission
    ) == PackageManager.PERMISSION_GRANTED

fun FragmentActivity.rxRequestSinglePermission(
    permission: String,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<Boolean> {
    if (isPermissionGranted(this, permission)) {
        return Observable.just(true)
    }
    return Observable.create {
        val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            it.onNext(result)
            it.onComplete()
        }
        launcher.launch(permission)
    }
}


fun FragmentActivity.rxRequestPermissions(
    permissions: Array<String>,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<PermissionResult> {
    val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
    return Observable.create {
        val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            val keys = map.keys.toTypedArray()
            val values = IntArray(keys.size) { PackageManager.PERMISSION_DENIED }
            for (i in keys.indices) {
                val key = keys[i]
                val result = if (map[key] == true) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
                values[i] = result
            }
            it.onNext(PermissionResult(code, keys, values))
        }
        launcher.launch(permissions)
    }
}

fun FragmentActivity.rxStartActivityForBack(
    intent: Intent,
    options: Bundle? = null,
): Observable<Any> {
    if (options == null) {
        startActivity(intent)
    } else {
        startActivity(intent, options)
    }
    return registerLifeCycleEvent(Lifecycle.Event.ON_PAUSE)
        .flatMap {
            registerLifeCycleEvent(Lifecycle.Event.ON_RESUME)
        }
}

fun FragmentActivity.rxStartActivityForResult(
    intent: Intent,
    options: Bundle? = null,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<ActivityResult> {
    return Observable.create {
        val launcher = this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
            it.onNext(ActivityResult(code, result.resultCode, result.data))
            it.onComplete()
        }
        options?.let {
            intent.putExtra(ActivityResultContracts.StartActivityForResult.EXTRA_ACTIVITY_OPTIONS_BUNDLE, options)
        }
        launcher.launch(intent)
    }
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

fun LifecycleOwner.registerLifeCycleEvent(vararg eventArray: Lifecycle.Event, once: Boolean = true): Observable<Lifecycle.Event> {
    return RxJavaPlugins.onAssembly(ObservableLifeCycle(this, HashSet<Lifecycle.Event>().apply { addAll(eventArray) }, once))
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













