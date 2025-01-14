package com.vinio.camera.fragments

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.vinio.camera.R
import com.vinio.camera.databinding.FragmentGalleryBinding
import java.io.File

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding: FragmentGalleryBinding
        get() = (_binding
            ?: throw RuntimeException("FragmentGalleryBinding == null")) as FragmentGalleryBinding

    private lateinit var adapter: GalleryAdapter
    private var mediaFiles: MutableList<File> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val layoutManager = GridLayoutManager(requireContext(), 3) // 3 колонки
        binding.recyclerView.layoutManager = layoutManager
        loadMediaFiles()
        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_gallery_to_photo)
        }
        return binding.root
    }

    // Функция для загрузки медиа файлов из MediaStore
    private fun loadMediaFiles() {
        val mediaUri = MediaStore.Files.getContentUri("external")
        Log.d("URI", mediaUri.toString())

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " LIKE ? OR " +
                MediaStore.Files.FileColumns.MIME_TYPE + " LIKE ?"
        val selectionArgs = arrayOf("image/%", "video/%")

        val cursor = requireContext().contentResolver.query(
            mediaUri, projection, selection, selectionArgs, null
        )

        cursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            while (it.moveToNext()) {
                val filePath = it.getString(dataIndex)
                Log.d("URI", filePath.toString())
                val file = File(filePath)
                if (file.exists()) {
                    mediaFiles.add(file)
                }
            }
        }

        // Создаем адаптер после загрузки медиа файлов
        adapter = GalleryAdapter(
            mediaFiles,
            onMediaClick = { file ->
                // Передаем выбранный файл в FullscreenMediaFragment через NavController
                val bundle = Bundle().apply {
                    putSerializable("media_file", file) // Передаем файл как Serializable
                }

                // Навигация с передачей данных
                findNavController().navigate(R.id.action_gallery_to_fullScreen, bundle)
            }
        )

        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        mediaFiles.clear()
        loadMediaFiles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
