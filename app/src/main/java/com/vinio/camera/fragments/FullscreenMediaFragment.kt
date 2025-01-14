package com.vinio.camera.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.vinio.camera.R
import com.vinio.camera.databinding.FragmentFullscreenMediaBinding
import java.io.File

class FullscreenMediaFragment : Fragment() {

    private var _binding: FragmentFullscreenMediaBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaPlayer: MediaPlayer
    private var videoFile: File? = null
    private lateinit var mediaFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullscreenMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем переданный файл через arguments
        mediaFile = arguments?.getSerializable("media_file") as? File
            ?: throw IllegalArgumentException("File not found in arguments")

        // Устанавливаем изображение или видео
        val uri = Uri.fromFile(mediaFile)

        Log.d("TAG", "${uri.lastPathSegment}")
        Log.d("TAG", "${uri.lastPathSegment?.startsWith("IMG")}")

        if (uri.lastPathSegment?.startsWith("IMG") == true) {
            binding.fullscreenImageView.visibility = View.VISIBLE
            binding.fullscreenVideoView.visibility = View.GONE
            binding.fullscreenImageView.setImageURI(uri)
        } else {
            binding.fullscreenImageView.visibility = View.GONE
            binding.fullscreenVideoView.visibility = View.VISIBLE
            binding.fullscreenVideoView.setVideoURI(uri)
            binding.fullscreenVideoView.start()
        }

        // Обработчик для кнопки удаления
        binding.deleteButton.setOnClickListener {
            // Удаляем файл
            if (mediaFile.exists()) {
                mediaFile.delete()
                Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Закрываем экран
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(file: File): FullscreenMediaFragment {
            val fragment = FullscreenMediaFragment()
            val args = Bundle()
            args.putSerializable("media_file", file)
            fragment.arguments = args
            return fragment
        }
    }
}

