package com.yxf.rxandroidextensions.activity

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicInteger


internal val nextLocalRequestCode = AtomicInteger()

fun <I, O> FragmentActivity.startContractForResult(
    contract: ActivityResultContract<I, O>,
    input: I,
    callback: ActivityResultCallback<O>
) {
    val key = "activity_rq_for_result#${nextLocalRequestCode.getAndIncrement()}"
    val registry = activityResultRegistry
    var launcher: ActivityResultLauncher<I>? = null
    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (Lifecycle.Event.ON_DESTROY == event) {
                launcher?.unregister()
                lifecycle.removeObserver(this)
            }
        }
    }
    lifecycle.addObserver(observer)
    val newCallback = ActivityResultCallback<O> {
        launcher?.unregister()
        lifecycle.removeObserver(observer)
        callback.onActivityResult(it)
    }
    launcher = registry.register(key, contract, newCallback)
    launcher.launch(input)
}