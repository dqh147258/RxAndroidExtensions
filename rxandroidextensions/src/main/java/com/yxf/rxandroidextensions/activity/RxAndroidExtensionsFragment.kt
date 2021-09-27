package com.yxf.rxandroidextensions.activity

import android.content.Intent
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class RxAndroidExtensionsFragment : Fragment() {


    private val requestMap = HashMap<Int, Subject<Any>>()


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val subject = requestMap[requestCode]?.let { it as Subject<ActivityResult> } ?: return
        requestMap.remove(requestCode)
        subject.onNext(ActivityResult(requestCode, resultCode, data))
        subject.onComplete()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val subject = requestMap[requestCode]?.let { it as Subject<PermissionResult> } ?: return
        requestMap.remove(requestCode)
        subject.onNext(PermissionResult(requestCode, permissions, grantResults))
    }

    fun <T> getObservable(id: Int): Observable<T> {
        return (requestMap[id]?.let { it as Subject<T> } ?: PublishSubject.create<T>()
            .also { requestMap[id] = (it as Subject<Any>) })
            .doOnDispose { requestMap.remove(id) }
    }
}