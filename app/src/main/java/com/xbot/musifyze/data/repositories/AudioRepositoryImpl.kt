package com.xbot.musifyze.data.repositories

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.xbot.musifyze.data.models.AudioDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val context: Context
) : AudioRepository {

    override suspend fun getAllAudioFromDevice(): List<AudioDataModel> {
        return withContext(Dispatchers.IO) {
            val audioList = mutableListOf<AudioDataModel>()

            val uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)

            cursor?.use {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (it.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val duration = cursor.getLong(durationColumn)
                    val album = cursor.getString(albumColumn)
                    val albumId = cursor.getLong(albumIdColumn)

                    val albumCoverUri = ContentUris.withAppendedId(
                        ALBUM_ART_URI,
                        albumId
                    )

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    audioList.add(
                        AudioDataModel(
                            id = id,
                            uri = contentUri,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            albumCoverUri = albumCoverUri
                        )
                    )
                }
            }

            audioList
        }
    }

    companion object {
        private val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}