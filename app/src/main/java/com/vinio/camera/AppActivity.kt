package com.vinio.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
        if (checkPermissions()) {
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
        val storagePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val storageReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return cameraPermission && audioPermission && storagePermission && storageReadPermission
    }

    //    Запрос разрешений
    fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        requestPermissionsLauncher.launch(permissions)
    }

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            run {
                val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
                val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
                val storageGranted = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
                } else {
                    true
                }
                if (!cameraGranted) {
                    Toast.makeText(this, "Нет доступа к камере. Завершаемся!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish() // Завершаем приложение
                } else if (!audioGranted) {
                    Toast.makeText(this, "Немое кино..",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!storageGranted) {
                    Toast.makeText(this, "Нет доступа к хранилищу, нельзя сохранить фото",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Все разрешения предоставлены",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
}