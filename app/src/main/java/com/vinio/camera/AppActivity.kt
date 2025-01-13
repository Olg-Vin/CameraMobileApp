package com.vinio.camera

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (checkPermissions()){
            Toast.makeText(this, "Permissions already exist", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions()
        }
    }

//    Проверка наличия разрешений
    fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val audioPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return cameraPermission && audioPermission
    }

//    Запрос разрешений
    fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        requestPermissionsLauncher.launch(permissions)
    }
    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            run {
                val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
                val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

                if (cameraGranted && audioGranted) {
                    // Все разрешения предоставлены
                    Toast.makeText(
                        this,
                        "Permissions granted. Starting camera...",
                        Toast.LENGTH_SHORT
                    ).show()
                    // startCamera() // Например, запустить камеру
                } else {
                    // Одно или оба разрешения не предоставлены
                    Toast.makeText(
                        this,
                        "Permissions are required to use the camera.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
}