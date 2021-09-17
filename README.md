# RxAndroidExtensions

RxJava Android Extensions

[![](https://www.jitpack.io/v/dqh147258/RxAndroidExtensions.svg)](https://www.jitpack.io/#dqh147258/RxAndroidExtensions)

## Features

### Activity 
- Request permissions with Observable
- Request ActivityResult with Observable
- Request install packages from unknown sources with Observable

More features are in developing...


## How to use

### Dependencies

add dependencies in your project like this

```groovy
	allprojects {
		repositories {
			//...
			maven { url 'https://www.jitpack.io' }
		}
	}
```

```groovy
    dependencies {
	        implementation 'com.github.dqh147258:RxAndroidExtensions:1.0.2'
	}
```

### Request Permission

Request Single Permission
```kotlin
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
```
Request Multi-Permissions
```kotlin
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
```

Request install packages from unknown sources
```kotlin
    private fun requestInstallPackagesFromUnknownSources(activity: FragmentActivity) {
        activity.rxRequestInstallPackagesPermission()
            .subscribe {
                if (it) {
                    Log.d(TAG, "request install packages from unknown sources successfully")
                } else {
                    Log.e(TAG, "request install packages from unknown sources failed")
                }
            }
    }
```

### Start activity for result
```kotlin
    private fun startActivityForResult(activity: FragmentActivity) {
        val uri = Uri.parse("package:$packageName")
        activity.rxStartActivityForResult(Intent(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)))
            .subscribe {
                if (it.isOk) {
                    val intent = it.data
                    Log.d(TAG, "get activity result successfully")

                } else {
                    Log.e(TAG, "get activity result successfully but the result is false")
                }
            }
    }
```




