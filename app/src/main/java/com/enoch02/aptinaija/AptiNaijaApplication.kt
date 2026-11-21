package com.enoch02.aptinaija

import android.app.Application
import com.enoch02.aptinaija.data.pack.QuestionPackImporter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AptiNaijaApplication : Application() {

    @Inject
    lateinit var packImporter: QuestionPackImporter

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { packImporter.importIfNeeded() }
    }
}
