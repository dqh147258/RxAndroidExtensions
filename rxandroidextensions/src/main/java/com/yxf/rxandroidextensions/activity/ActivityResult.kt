package com.yxf.rxandroidextensions.activity

import android.app.Activity
import android.content.Intent

class ActivityResult(val requestCode: Int, val resultCode: Int, val data: Intent?) {

    val isOk = resultCode == Activity.RESULT_OK
    val hasData = data != null


}
