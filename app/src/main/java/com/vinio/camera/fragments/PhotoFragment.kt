package com.vinio.camera.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.vinio.camera.R
import com.vinio.camera.databinding.CameraActionsBinding
import com.vinio.camera.databinding.CameraNavigationBinding
import com.vinio.camera.databinding.FragmentPhotoBinding

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        _cameraNavigationBinding = CameraNavigationBinding.bind(binding.root.findViewById(R.id.navigation_buttons))
        _cameraActionsBinding = CameraActionsBinding.bind(binding.root.findViewById(R.id.actions_buttons))

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
            // Ваш код для кнопки "Хоба"
        }

        cameraActionsBinding.buttonChange.setOnClickListener {
            // Ваш код для кнопки "Поворот камеры"
        }

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
        Log.d("CAMERA_APP", "Fragment photo onDestroyView")
    }
}