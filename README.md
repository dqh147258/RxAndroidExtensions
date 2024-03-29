# RxAndroidExtensions

RxJava Android Extensions

[![](https://www.jitpack.io/v/dqh147258/RxAndroidExtensions.svg)](https://www.jitpack.io/#dqh147258/RxAndroidExtensions)

## Features

### Activity 
- Request permissions with Observable
- Request ActivityResult with Observable
- registerActivityResult with Observable
- Request install packages from unknown sources with Observable

### Auto dispose
- Dispose onDestroy by LifeCycleOwner
- Dispose onPause by LifeCycleOwner
- Auto dispose on class which implement DisposeSource

### Register LifeCycle event
- Register life cycle event from LifeCycleOwner

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
	        implementation 'com.github.dqh147258:RxAndroidExtensions:1.1.9'
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

### Register Activity Result
```kotlin
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
```

### Automatic dispose

```kotlin
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
```

### Register lifecycle event
```kotlin
    private fun registerLifeCycleEvent(activity: FragmentActivity) {
        activity.registerLifecycleEvent(Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_RESUME)
            .subscribe {
                Log.d(TAG, "current lifecycle is: $it")
            }
        activity.registerLifecycleEvent(Lifecycle.Event.ON_PAUSE, once = true)
            .flatMap { activity.registerLifecycleEvent(Lifecycle.Event.ON_RESUME, once = true) }
            .subscribe {
                Log.d(TAG, "on resume again")
            }
    }
```





