package com.yxf.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.yxf.rxandroidextensions.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val TAG = "${MainActivity::class.simpleName}"

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "activity onCreate")
        setContentView(R.layout.activity_main)
        rxRequestInstallPackagesPermission().subscribe {
            Log.d(TAG, "request packages permission result : $it")
            requestPermissions(this)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "activity onResume")
        Observable.interval(2, 1, TimeUnit.SECONDS)
            .map {
                Log.d(TAG, "log map interval value : $it")
                return@map it
            }
            .disposeOnPause(this)
            .subscribe {
                Log.d(TAG, "log subscribe interval value : $it")
            }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "activity onPause")
    }

    @SuppressLint("CheckResult")
    private fun requestPermissions(activity: FragmentActivity) {
        activity.rxRequestPermissions(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        ).subscribe {
            for ((_, v) in it.resultMap.entries.withIndex()) {
                when (v.key) {
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        when {
                            v.value -> {
                                Log.d(TAG, "get permission(${v.key}) successfully")
                            }
                            it.isDeniedForever(v.key, this) -> {
                                Log.w(
                                    TAG,
                                    "get permission(${v.key}) failed and has been denied forever"
                                )
                            }
                            else -> {
                                Log.w(TAG, "get permission(${v.key}) failed")
                            }
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun requestSinglePermission(activity: FragmentActivity) {
        activity.rxRequestSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe {
                if (it) {
                    Log.d(TAG, "request write external storage successfully")
                } else {
                    Log.e(TAG, "request write external storage failed")
                }
            }
    }

    private fun requestInstallPackagesFromUnknownSources(activity: FragmentActivity) {
        activity.rxRequestInstallPackagesPermission()
            .subscribe {
                if (it) {
                    Log.d(TAG, "request install packages from unknown packages successfully")
                } else {
                    Log.e(TAG, "request install packages failed")
                }
            }
    }

    private fun startActivityForResult(activity: FragmentActivity) {
        val uri = Uri.parse("package:$packageName")
        activity.rxStartActivityForResult(
            Intent(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    uri
                )
            )
        )
            .subscribe {
                if (it.isOk) {
                    val intent = it.data
                    Log.d(TAG, "get activity result successfully")

                } else {
                    Log.e(TAG, "get activity result successfully but the result is false")
                }
            }
    }
}