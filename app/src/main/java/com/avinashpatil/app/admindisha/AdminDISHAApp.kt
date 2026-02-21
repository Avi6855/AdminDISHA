package com.avinashpatil.app.admindisha

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application class for DISHA Computer app.
 * The @HiltAndroidApp annotation triggers Hilt's code generation and
 * serves as the application-level dependency container.
 */
@HiltAndroidApp
class AdminDISHAApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

    }


}