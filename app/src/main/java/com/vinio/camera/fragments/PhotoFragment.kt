package com.vinio.camera.fragments

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.vinio.camera.R
import com.vinio.camera.databinding.CameraActionsBinding
import com.vinio.camera.databinding.CameraNavigationBinding
import com.vinio.camera.databinding.FragmentPhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    var isBackCamera: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        _cameraNavigationBinding = CameraNavigationBinding.bind(binding.root.findViewById(R.id.navigation_buttons))
        _cameraActionsBinding = CameraActionsBinding.bind(binding.root.findViewById(R.id.actions_buttons))

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

//        cameraActionsBinding.galleryPreview.setOnClickListener {
//            setupGalleryButton()
//        }

        cameraActionsBinding.buttonTake.setOnClickListener {
            takePhoto()
        }

        cameraActionsBinding.buttonChange.setOnClickListener {
            isBackCamera = !isBackCamera
            startCamera()
        }

    }

    private fun takePhoto() {
        showShutterEffect()
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
                    Log.d("CameraX", "Фото сохранено: ${outputFileResults.savedUri}")
                    Glide.with(requireContext())
                        .load(outputFileResults.savedUri)
                        .into(cameraActionsBinding.galleryPreview)
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

    private suspend fun getLastMediaUri(): Uri? {
        return withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED
            )
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            val cursor = requireContext().contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                sortOrder
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val id = it.getLong(idIndex)
                    return@withContext Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
                }
            }
            null
        }
    }

    private fun setupGalleryButton() {
        val previewImage = cameraActionsBinding.galleryPreview

        lifecycleScope.launchWhenStarted {
            val lastMediaUri = getLastMediaUri()
            if (lastMediaUri != null) {
                Glide.with(requireContext())
                    .load(lastMediaUri)
                    .into(previewImage)
            } else {
                previewImage.setImageResource(R.drawable.baseline_add_photo_alternate_24)
            }
        }

        previewImage.setOnClickListener {
            // Обработчик клика по кнопке Галерея
            findNavController().navigate(R.id.action_photo_to_gallery)
            Toast.makeText(requireContext(), "Открываем Галерею!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showShutterEffect() {
        val overlay = View(requireContext()).apply {
            setBackgroundColor(Color.Black.toArgb())
            alpha = 0f
        }

        (requireActivity().window.decorView as ViewGroup).addView(overlay)

        overlay.animate()
            .alpha(1f)
            .setDuration(100) // Длительность эффекта затемнения
            .withEndAction {
                overlay.animate()
                    .alpha(0f)
                    .setDuration(100) // Длительность возвращения
                    .withEndAction {
                        (requireActivity().window.decorView as ViewGroup).removeView(overlay)
                    }
            }
            .start()
    }


    override fun onStart() {
        super.onStart()
        Log.d("CAMERA_APP", "Fragment photo onStart")
    }

    override fun onResume() {
        super.onResume()
        setupGalleryButton()
        cameraNavigationBinding.buttonPhoto.apply {
            textSize = 28f
            setTextColor(Color.White.toArgb())
        }
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
        Log.d("CAMERA_APP", "Fragment photo onDestroyView")
    }
}