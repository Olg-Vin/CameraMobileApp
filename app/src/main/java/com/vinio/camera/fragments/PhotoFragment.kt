package com.vinio.camera.fragments

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.vinio.camera.R
import com.vinio.camera.databinding.CameraActionsBinding
import com.vinio.camera.databinding.CameraNavigationBinding
import com.vinio.camera.databinding.FragmentPhotoBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PhotoFragment : Fragment() {

    private var _binding: FragmentPhotoBinding? = null
    private val binding: FragmentPhotoBinding
        get() = (_binding
            ?: RuntimeException("[EXCEPTION] FragmentPhotoBinding is null")) as FragmentPhotoBinding

    private var _cameraNavigationBinding: CameraNavigationBinding? = null
    private val cameraNavigationBinding: CameraNavigationBinding
        get() = (_cameraNavigationBinding
            ?: RuntimeException("[EXCEPTION] CameraNavigationBinding is null")) as CameraNavigationBinding

    private var _cameraActionsBinding: CameraActionsBinding? = null
    private val cameraActionsBinding: CameraActionsBinding
        get() = (_cameraActionsBinding
            ?: RuntimeException("[EXCEPTION] FragmentPhotoBinding is null")) as CameraActionsBinding


    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    var isBackCamera: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        _cameraNavigationBinding = CameraNavigationBinding.bind(binding.root.findViewById(R.id.navigation_buttons))
        _cameraActionsBinding = CameraActionsBinding.bind(binding.root.findViewById(R.id.actions_buttons))
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        Log.d("CAMERA_APP", "Fragment photo onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CAMERA_APP", "Fragment photo onViewCreated")

        cameraNavigationBinding.buttonPhoto.setOnClickListener {
//            view.findNavController().navigate(R.id.action_video_to_photo)
        }

        cameraNavigationBinding.buttonVideo.setOnClickListener {
            view.findNavController().navigate(R.id.action_photo_to_video)
        }

        cameraActionsBinding.buttonGallery.setOnClickListener {
            // Ваш код для кнопки "Галерея"
        }

        cameraActionsBinding.buttonTake.setOnClickListener {
            takePhoto()
        }

        cameraActionsBinding.buttonChange.setOnClickListener {
            isBackCamera = !isBackCamera
            startCamera()
        }

    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val fileName = "IMG_${System.currentTimeMillis()}.jpeg"
        Log.d("CameraX", "Создание fileName: $fileName")

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Сохраняем фото
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CameraX", "Фото сохранено:")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Ошибка сохранения фото", exception)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector =
                if (isBackCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onStart() {
        super.onStart()
        Log.d("CAMERA_APP", "Fragment photo onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("CAMERA_APP", "Fragment photo onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("CAMERA_APP", "Fragment photo onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _cameraNavigationBinding = null
        _cameraActionsBinding = null
        cameraExecutor.shutdown()
        Log.d("CAMERA_APP", "Fragment photo onDestroyView")
    }
}