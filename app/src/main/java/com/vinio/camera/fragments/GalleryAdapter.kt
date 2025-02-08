package com.vinio.camera.fragments

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vinio.camera.databinding.GalleryItemBinding
import java.io.File

class GalleryAdapter(
    private var mediaFiles: MutableList<File>,
    private val onMediaClick: (File) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GalleryAdapter.GalleryViewHolder {
        val binding = GalleryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }
    override fun onBindViewHolder(holder: GalleryAdapter.GalleryViewHolder, position: Int) {
        val file = mediaFiles[position]
        holder.bind(file)
    }
    override fun getItemCount(): Int = mediaFiles.size
    inner class GalleryViewHolder(private val binding: GalleryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            val uri = Uri.fromFile(file)
            Glide.with(binding.root.context)
                .load(uri)
                .into(binding.thumbnail)

            if (uri.lastPathSegment?.startsWith("Video") == true) {
                binding.videoIcon.visibility = View.VISIBLE
            } else {
                binding.videoIcon.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onMediaClick(file)
            }
        }
    }
}