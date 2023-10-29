package com.xbot.musifyze.data.repositories

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList

object MediaItemTree {
    private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var titleMap: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var isInitialized = false

    private const val ROOT_ID = "[rootID]"
    private const val ALBUM_ID = "[albumID]"
    private const val GENRE_ID = "[genreID]"
    private const val ARTIST_ID = "[artistID]"
    private const val ALBUM_PREFIX = "[album]"
    private const val GENRE_PREFIX = "[genre]"
    private const val ARTIST_PREFIX = "[artist]"
    private const val ITEM_PREFIX = "[item]"

    private val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")

    private class MediaItemNode(val item: MediaItem) {
        private val children: MutableList<MediaItem> = ArrayList()

        fun addChild(childID: String) {
            this.children.add(treeNodes[childID]!!.item)
        }

        fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        isBrowsable: Boolean,
        mediaType: @MediaMetadata.MediaType Int,
        album: String? = null,
        artist: String? = null,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setAlbumTitle(album)
            .setTitle(title)
            .setArtist(artist)
            .setGenre(genre)
            .setIsBrowsable(isBrowsable)
            .setIsPlayable(isPlayable)
            .setArtworkUri(imageUri)
            .setMediaType(mediaType)
            .build()

        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .setUri(sourceUri)
            .build()
    }

    fun initialize(context: Context) {
        if (isInitialized) return

        isInitialized = true

        // create root and folders for album/artist/genre.
        treeNodes[ROOT_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Root",
                    mediaId = ROOT_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
                )
            )
        treeNodes[ALBUM_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Albums",
                    mediaId = ALBUM_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS
                )
            )
        treeNodes[ARTIST_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Artists",
                    mediaId = ARTIST_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS
                )
            )
        treeNodes[GENRE_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Genres",
                    mediaId = GENRE_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_GENRES
                )
            )

        treeNodes[ROOT_ID]!!.addChild(ALBUM_ID)
        treeNodes[ROOT_ID]!!.addChild(ARTIST_ID)
        treeNodes[ROOT_ID]!!.addChild(GENRE_ID)

        mediaStoreContentProvider(context) { mediaItem ->
            val idInTree = ITEM_PREFIX + mediaItem.mediaId
            val idInMap = mediaItem.mediaMetadata.title.toString()
            val albumFolderIdInTree = ALBUM_PREFIX + mediaItem.mediaMetadata.albumTitle
            val artistFolderIdInTree = ARTIST_PREFIX + mediaItem.mediaMetadata.artist
            val genreFolderIdInTree = GENRE_PREFIX + mediaItem.mediaMetadata.genre

            treeNodes[idInTree] = MediaItemNode(mediaItem)
            titleMap[idInMap.lowercase()] = treeNodes[idInTree]!!

            // add into album folder
            if (!treeNodes.containsKey(albumFolderIdInTree)) {
                treeNodes[albumFolderIdInTree] =
                    MediaItemNode(
                        buildMediaItem(
                            title = mediaItem.mediaMetadata.albumTitle.toString(),
                            mediaId = albumFolderIdInTree,
                            isPlayable = true,
                            isBrowsable = true,
                            mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
                            imageUri = mediaItem.mediaMetadata.artworkUri
                        )
                    )
                treeNodes[ALBUM_ID]!!.addChild(albumFolderIdInTree)
            }
            treeNodes[albumFolderIdInTree]!!.addChild(idInTree)

            // add into artist folder
            if (!treeNodes.containsKey(artistFolderIdInTree)) {
                treeNodes[artistFolderIdInTree] =
                    MediaItemNode(
                        buildMediaItem(
                            title = mediaItem.mediaMetadata.artist.toString(),
                            mediaId = artistFolderIdInTree,
                            isPlayable = true,
                            isBrowsable = true,
                            mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
                            imageUri = mediaItem.mediaMetadata.artworkUri
                        )
                    )
                treeNodes[ARTIST_ID]!!.addChild(artistFolderIdInTree)
            }
            treeNodes[artistFolderIdInTree]!!.addChild(idInTree)

            // add into genre folder
            if (!treeNodes.containsKey(genreFolderIdInTree)) {
                treeNodes[genreFolderIdInTree] =
                    MediaItemNode(
                        buildMediaItem(
                            title = mediaItem.mediaMetadata.genre.toString(),
                            mediaId = genreFolderIdInTree,
                            isPlayable = true,
                            isBrowsable = true,
                            mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
                            imageUri = mediaItem.mediaMetadata.artworkUri
                        )
                    )
                treeNodes[GENRE_ID]!!.addChild(genreFolderIdInTree)
            }
            treeNodes[genreFolderIdInTree]!!.addChild(idInTree)
        }
    }

    fun getItem(id: String): MediaItem? {
        return treeNodes[id]?.item
    }

    fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }

    fun getChildren(id: String): List<MediaItem>? {
        return treeNodes[id]?.getChildren()
    }

    fun getRandomItem(): MediaItem {
        var curRoot = getRootItem()
        while (curRoot.mediaMetadata.isBrowsable == true) {
            val children = getChildren(curRoot.mediaId)!!
            curRoot = children.random()
        }
        return curRoot
    }

    fun getItemFromTitle(title: String): MediaItem? {
        return titleMap[title]?.item
    }

    private fun mediaStoreContentProvider(context: Context, action: (MediaItem) -> Unit) {
        val uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.GENRE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val genreColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)

            while (it.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val genre = cursor.getString(genreColumn)

                val albumCoverUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                action(
                    buildMediaItem(
                        title = title,
                        mediaId = id.toString(),
                        isPlayable = true,
                        isBrowsable = false,
                        mediaType = MediaMetadata.MEDIA_TYPE_MUSIC,
                        album = album,
                        artist = artist,
                        genre = genre,
                        sourceUri = contentUri,
                        imageUri = albumCoverUri
                    )
                )
            }
        }
    }
}