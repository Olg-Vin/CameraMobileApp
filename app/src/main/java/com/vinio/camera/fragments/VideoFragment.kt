package com.vinio.camera.fragments

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.vinio.camera.R
import com.vinio.camera.databinding.CameraActionsBinding
import com.vinio.camera.databinding.CameraNavigationBinding
import com.vinio.camera.databinding.FragmentVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VideoFragment : Fragment() {

//    Binding
    private var _binding: FragmentVideoBinding? = null
    private val binding: FragmentVideoBinding
        get() = (_binding
            ?: RuntimeException("[EXCEPTION] FragmentVideoBinding is null")) as FragmentVideoBinding

    private var _cameraNavigationBinding: CameraNavigationBinding? = null
    private val cameraNavigationBinding: CameraNavigationBinding
        get() = (_cameraNavigationBinding
            ?: RuntimeException("[EXCEPTION] CameraNavigationBinding is null")) as CameraNavigationBinding

    private var _cameraActionsBinding: CameraActionsBinding? = null
    private val cameraActionsBinding: CameraActionsBinding
        get() = (_cameraActionsBinding
            ?: RuntimeException("[EXCEPTION] FragmentPhotoBinding is null")) as CameraActionsBinding

//    Camera vars
    private var videoCapture: VideoCapture<Recorder>? = null
    private var isBackCamera: Boolean = true
    private var recording: Recording? = null

//    Timer
    private var startTime: Long = 0
    private var timerJob: Job? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        _cameraNavigationBinding =
            CameraNavigationBinding.bind(binding.root.findViewById(R.id.navigation_buttons))
        _cameraActionsBinding =
            CameraActionsBinding.bind(binding.root.findViewById(R.id.actions_buttons))

        startCamera()

        Log.d("CAMERA_APP", "Fragment video onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CAMERA_APP", "Fragment video onViewCreated")

        cameraNavigationBinding.buttonPhoto.setOnClickListener {
            view.findNavController().navigate(R.id.action_video_to_photo)
        }

        cameraNavigationBinding.buttonVideo.setOnClickListener {
//            view.findNavController().navigate(R.id.action_photo_to_video)
        }

//        cameraActionsBinding.buttonGallery.setOnClickListener {
//            // Ваш код для кнопки "Галерея"
//        }

        cameraActionsBinding.buttonTake.setOnClickListener {
            captureVideo()
        }

        cameraActionsBinding.buttonChange.setOnClickListener {
            isBackCamera = !isBackCamera
            startCamera()
        }

    }

    private fun captureVideo() {
        val context: Context = requireContext()
        val videoCapture = this.videoCapture ?: return

        cameraActionsBinding.buttonTake.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            Log.d("CameraX", "Останова")
            return
        }

        // create and start a new recording session
        val fileName = "Video_${System.currentTimeMillis()}.mp4"
        Log.d("CameraX", "Создание fileName: $fileName")
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        cameraActionsBinding.buttonTake.apply {
                            setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                            isEnabled = true
                        }
                        startTimer()
                    }

                    is VideoRecordEvent.Finalize -> {
                        stopTimer()
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            Log.d("CameraX", msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e("CameraX", "Video capture ends with error: ${recordEvent.error}")
                        }
                        cameraActionsBinding.buttonTake.apply {
                            setBackgroundColor(ContextCompat.getColor(context, R.color.main))
                            isEnabled = true
                        }
                    }
                }
            }
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

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector =
                if (isBackCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()

        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000).toInt()
                val minutes = seconds / 60
                val secondsFormatted = seconds % 60

                val timeText = String.format("%02d:%02d", minutes, secondsFormatted)
                binding.timerTextView.text = timeText

                delay(1000)  // Задержка на 1 секунду
            }
        }
    }

    private fun stopTimer() {
        val timeText = "00:00"
        binding.timerTextView.text = timeText
        timerJob?.cancel()  // Останавливаем корутину
    }


    override fun onStart() {
        super.onStart()
        Log.d("CAMERA_APP", "Fragment video onStart")
    }

    override fun onResume() {
        super.onResume()
        cameraNavigationBinding.buttonVideo.apply {
            textSize = 28f
            setTextColor(Color.White.toArgb())
        }
        Log.d("CAMERA_APP", "Fragment video onResume")
    }

    override fun onStop() {
        super.onStop()
        _binding = null
        _cameraNavigationBinding = null
        _cameraActionsBinding = null
        Log.d("CAMERA_APP", "Fragment video onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _cameraNavigationBinding = null
        _cameraActionsBinding = null
        Log.d("CAMERA_APP", "Fragment video onDestroyView")
    }
}