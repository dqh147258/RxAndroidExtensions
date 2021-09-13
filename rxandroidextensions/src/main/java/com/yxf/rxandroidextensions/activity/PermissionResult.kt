package com.yxf.rxandroidextensions.activity

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build

class PermissionResult(
    val requestCode: Int,
    val permissions: Array<out String>,
    val grantResults: IntArray
) {

    private var internalGrantPermissionList: ArrayList<String>? = null
    private var internalDeniedPermissionList: ArrayList<String>? = null

    val resultMap by lazy {
        return@lazy HashMap<String, Boolean>().apply {
            for (i in permissions.indices) {
                put(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED)
            }
        }
    }

    val grantPermissionList by lazy {
        if (internalGrantPermissionList == null) {
            classifyPermissions()
        }
        return@lazy internalGrantPermissionList!!
    }
    val deniedPermissionList by lazy {
        if (internalDeniedPermissionList == null) {
            classifyPermissions()
        }
        return@lazy internalDeniedPermissionList!!
    }

    public fun isDeniedForever(permission: String, activity: Activity): Boolean {
        val result = resultMap[permission]
            ?: true // if permission not exist in result map can not return right value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !result
            && !activity.shouldShowRequestPermissionRationale(permission)
        ) {
            return true
        }
        return false
    }


    private fun classifyPermissions() {
        internalGrantPermissionList = ArrayList()
        internalDeniedPermissionList = ArrayList()
        for (i in permissions.indices) {
            val result = grantResults[i] == PackageManager.PERMISSION_GRANTED
            if (result) {
                internalGrantPermissionList!!.add(permissions[i])
            } else {
                internalDeniedPermissionList!!.add(permissions[i])
            }
        }
    }


}