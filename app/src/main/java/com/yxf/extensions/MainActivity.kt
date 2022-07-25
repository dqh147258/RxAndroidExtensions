package com.yxf.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.yxf.extensions.databinding.ActivityMainBinding
import com.yxf.rxandroidextensions.*
import com.yxf.rxandroidextensions.activity.PermissionResult
import com.yxf.rxandroidextensions.activity.startContractForResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import leakcanary.LeakCanary
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val TAG = "${MainActivity::class.simpleName}"


    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "activity onCreate")
        val vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        vb.startTestActivity.setOnClickListener {
            startActivity(Intent(this, RxJavaTestActivity::class.java))
        }
        vb.checkLeak.setOnClickListener {
            LeakCanary.dumpHeap()
        }


        /*rxRequestInstallPackagesPermission().subscribe {
            Log.d(TAG, "request packages permission result : $it")
            requestPermissions(this)
        }
        Observable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .flatMap { registerLifecycleEvent(Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_DESTROY, once = true) }
            .subscribe {
                Log.d(TAG, "get lifecycle event : $it")
            }
        Observable.timer(4, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .flatMap { Observable.interval(2, 1, TimeUnit.SECONDS).disposeOnPause(this) }
            .subscribe {
                Log.d(TAG, "log subscribe interval value : $it")
            }

        handler.postDelayed({
            rxRequestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE))
                .subscribe {
                    Log.d(TAG, "get permission result: $it")
                }
        }, 5000)

        handler.postDelayed({
            rxStartActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                .subscribe {
                    Log.d(TAG, "get activity result: $it")
                }
        }, 10000)*/
        //tryStarActivityForResult()
    }

    private fun tryStarActivityForResult() {
        handler.postDelayed({
            rxStartContractForResult(ActivityResultContracts.StartActivityForResult(), Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                .subscribe {
                    Log.d(TAG, "get activity result")
                }
        }, 3000)
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "activity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "activity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "activity onStop")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "activity onStart")
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "on request permission result")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "on activity result")
    }

    @SuppressLint("CheckResult")
    private fun registerActivityResult(activity: FragmentActivity) {
        activity.rxStartContractForResult(
            ActivityResultContracts.StartActivityForResult(),
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        )
            .subscribe {
                if (it.resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "get activity result successfully")
                } else {
                    Log.w(TAG, "get activity result failed")
                }
            }
    }

    private fun startContractForResult(activity: FragmentActivity) {
        activity.startContractForResult(
            ActivityResultContracts.StartActivityForResult(),
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "get activity result successfully")
            } else {
                Log.w(TAG, "get activity result failed")
            }
        }
    }

    private fun registerLifeCycleEvent(activity: FragmentActivity) {
        activity.registerLifecycleEvent(Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_RESUME, once = false)
            .subscribe {
                Log.d(TAG, "current lifecycle is: $it")
            }
        activity.registerLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            .flatMap { activity.registerLifecycleEvent(Lifecycle.Event.ON_RESUME) }
            .subscribe {
                Log.d(TAG, "on pause again")
            }
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
                    if (PermissionResult.isDeniedForever(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            activity
                        )
                    ) {
                        Log.e(TAG, "write external storage permission has been denied forever")
                    }
                }
            }
    }

    @SuppressLint("CheckResult")
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

    @SuppressLint("CheckResult")
    private fun startActivityForResult(activity: FragmentActivity) {
        val uri = Uri.parse("package:$packageName")
        activity.rxStartActivityForResult(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
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

    @SuppressLint("CheckResult")
    private fun automaticDispose(owner: LifecycleOwner) {
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .disposeOnDestroy(owner)
            .subscribe {
                Log.d(TAG, "get interval value : $it")
            }

        Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .disposeOnPause(owner)
            .subscribe {
                Log.d(TAG, "get interval value : $it")
            }

        Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .disposeOnPauseAndDestroy(owner)
            .subscribe {
                Log.d(TAG, "get interval value : $it")
            }
    }


}