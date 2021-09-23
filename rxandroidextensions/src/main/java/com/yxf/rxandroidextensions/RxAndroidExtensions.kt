package com.yxf.rxandroidextensions

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.yxf.rxandroidextensions.activity.ActivityResult
import com.yxf.rxandroidextensions.activity.PermissionResult

import com.yxf.rxandroidextensions.activity.RxAndroidExtensionsFragment
import com.yxf.rxandroidextensions.lifecycle.ObservableLifeCycle
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

private fun getFragment(activity: FragmentActivity): RxAndroidExtensionsFragment {
    return activity.supportFragmentManager.findFragmentByTag(Ex.TAG)
        ?.let { return@let (it as RxAndroidExtensionsFragment) }
        ?: RxAndroidExtensionsFragment().also {
            activity.supportFragmentManager.beginTransaction().add(it, Ex.TAG)
                .commitNow()
        }
}

fun FragmentActivity.rxRequestSinglePermission(
    permission: String,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<Boolean> {
    return rxRequestPermissions(arrayOf(permission), requestCode)
        .map {
            return@map it.grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
}


fun FragmentActivity.rxRequestPermissions(
    permissions: Array<out String>,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<PermissionResult> {
    val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
    val fragment = getFragment(this)
    return fragment.run {
        val subject: Observable<PermissionResult> = getSubject(code)
        fragment.requestPermissions(permissions, code)
        return@run subject
    }
}

fun FragmentActivity.rxStartActivityForResult(
    intent: Intent,
    options: Bundle? = null,
    requestCode: Int = Ex.AUTOMATIC_REQUEST_CODE
): Observable<ActivityResult> {
    val code = if (requestCode == Ex.AUTOMATIC_REQUEST_CODE) Ex.requestId() else requestCode
    val fragment = getFragment(this)
    return fragment.run {
        val subject: Observable<ActivityResult> = getSubject(code)
        startActivityForResult(intent, code, options)
        return@run subject
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
    permissions: Array<out String>,
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


fun <T> Observable<T>.disposeOnDestroy(owner: LifecycleOwner): Observable<T> {
    return RxJavaPlugins.onAssembly(
        ObservableLifeCycle<T>(
            this,
            owner.lifecycle,
            disposeOnDestroy = true
        )
    )
}

fun <T> Observable<T>.disposeOnPause(owner: LifecycleOwner): Observable<T> {
    return RxJavaPlugins.onAssembly(
        ObservableLifeCycle<T>(
            this,
            owner.lifecycle,
            disposeOnPause = true
        )
    )
}

fun <T> Observable<T>.disposeOnPauseAndDestroy(owner: LifecycleOwner): Observable<T> {
    return RxJavaPlugins.onAssembly(
        ObservableLifeCycle<T>(
            this,
            owner.lifecycle,
            disposeOnPause = true, disposeOnDestroy = true
        )
    )
}













