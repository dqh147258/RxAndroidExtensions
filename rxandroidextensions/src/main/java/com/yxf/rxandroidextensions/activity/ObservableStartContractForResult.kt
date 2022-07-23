package com.yxf.rxandroidextensions.activity

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleDestroyedException
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.yxf.rxandroidextensions.runOnMainThread
import com.yxf.rxandroidextensions.runOnMainThreadSync
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import java.util.concurrent.atomic.AtomicReference

internal class ObservableStartContractForResult<I, O>(
    private var fragmentActivity: FragmentActivity?,
    private val contract: ActivityResultContract<I, O>,
    private val input: I
) : Observable<O>() {

    override fun subscribeActual(observer: Observer<in O>) {
        observer.onSubscribe(StartContractForResultObserver(observer, fragmentActivity, contract, input))
        fragmentActivity = null
    }

    private inner class StartContractForResultObserver(
        private val downStream: Observer<in O>,
        private var activity: FragmentActivity?,
        private val contract: ActivityResultContract<I, O>,
        private val input: I
    ) : AtomicReference<Disposable>(), Disposable {


        private var launcher: ActivityResultLauncher<I>? = null

        val observer = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (Lifecycle.Event.ON_DESTROY == event) {
                    launcher?.unregister()
                    source.lifecycle.removeObserver(this)
                    releaseActivity()
                    downStream.onError(LifecycleDestroyedException())
                }
            }
        }


        init {
            runOnMainThreadSync {
                val key = "activity_rq_for_result#${nextLocalRequestCode.getAndIncrement()}"
                val registry = activity!!.activityResultRegistry
                activity!!.lifecycle.addObserver(observer)
                val newCallback = ActivityResultCallback<O> {
                    launcher?.unregister()
                    activity?.lifecycle?.removeObserver(observer)
                    releaseActivity()
                    downStream.onNext(it)
                    downStream.onComplete()
                }
                launcher = registry.register(key, contract, newCallback)
                launcher?.launch(input)
            }
        }

        private fun releaseActivity() {
            activity = null
        }

        private fun releaseLauncher() {
            launcher?.unregister()
            launcher = null
        }

        private fun releaseObserver() {
            activity?.lifecycle?.removeObserver(observer)
        }


        override fun dispose() {
            if (isDisposed) {
                return
            }
            DisposableHelper.dispose(this)
            runOnMainThread{
                releaseObserver()
                releaseLauncher()
                releaseActivity()
            }
        }

        override fun isDisposed(): Boolean {
            return get() == DisposableHelper.DISPOSED
        }

    }

}