package com.fadhly.gestura

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private val translationViewModel: TranslationViewModel by viewModels {
        TranslationViewModelFactory(TranslationRepository(ApiConfig.retrofit.create(ApiService::class.java)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("CameraActivity", "Activity created")

        previewView = findViewById(R.id.previewView)

        // Observe the translated text from the ViewModel
        translationViewModel.translatedText.observe(this, Observer { translatedText ->
            Toast.makeText(this, "Translated Text: $translatedText", Toast.LENGTH_SHORT).show()
        })

        // Check for camera permission
        if (allPermissionsGranted()) {
            Log.d("CameraActivity", "Permissions granted, starting camera")
            startCamera()
        } else {
            Log.d("CameraActivity", "Requesting permissions")
            requestPermissions()
        }
    }

    private fun startCamera() {
        Log.d("CameraActivity", "Initializing camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            Log.d("CameraActivity", "Camera provider is ready")
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Use a dedicated executor for frame analysis
            val analysisExecutor = Executors.newSingleThreadExecutor()

            Log.d("CameraActivity", "Setting image analyzer")

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), { imageProxy ->
                Log.d("CameraActivity", "Received a frame for analysis")
                processImageProxy(imageProxy)
            })

            try {
                cameraProvider.unbindAll()
                Log.d("CameraActivity", "Binding use cases to lifecycle")
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Failed to bind use cases", exc)
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        Thread.sleep(100) // 100ms delay to avoid overwhelming the system

        Log.d("CameraActivity", "processImageProxy function called") // Check if this function is reached
        Log.d("CameraActivity", "Processing image frame")
        val bitmap = imageProxy.toBitmap()

        // Send the bitmap frame to the ViewModel
        if (bitmap != null) {
            Log.d("CameraActivity", "Bitmap created, sending to ViewModel")
            Log.d("CameraActivity", "Sending frame to ViewModel")
            lifecycleScope.launch(Dispatchers.IO) {
                translationViewModel.translateSignLanguageFrame(bitmap)
            }
        } else {
            Log.e("CameraActivity", "Failed to convert image proxy to bitmap")
        }
        imageProxy.close()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
            } else {
                // Handle the case when permissions are not granted
            }
        }
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}