package com.yxf.extensions

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.yxf.extensions.databinding.ActivityRxJavaActivityBinding
import com.yxf.rxandroidextensions.autoDispose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class RxJavaTestActivity : AppCompatActivity() {


    private val TAG = "RxJavaTestActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vb = ActivityRxJavaActivityBinding.inflate(layoutInflater)
        setContentView(vb.root)
        Log.d(TAG, "onCreate")
        Observable.timer(500, TimeUnit.SECONDS)
            /*.doOnDispose {
                Log.d(TAG, "timer dispose")
            }*/
            .autoDispose(this, Lifecycle.Event.ON_DESTROY)
            .subscribe {
                Log.d(TAG, "timer triggered")
            }

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            Log.d(TAG, "finish")
        }, 5000)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }


}