package com.xbot.musifyze.ui.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xbot.musifyze.ui.components.IconButton
import com.xbot.musifyze.ui.features.player.MusicPlayerMinHeight
import com.xbot.musifyze.ui.utils.MediaData
import com.xbot.musifyze.ui.utils.MediaLibrary
import com.xbot.musifyze.ui.utils.rememberMediaLibrary

@Composable
fun CategoriesScreenRoute(
    navigate: (String) -> Unit = {}
) {
    val mediaLibrary = rememberMediaLibrary()

    CategoriesScreenContent(
        mediaLibrary = mediaLibrary,
        navigate = navigate
    )
}

@Composable
private fun CategoriesScreenContent(
    modifier: Modifier = Modifier,
    mediaLibrary: MediaLibrary,
    navigate: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = MusicPlayerMinHeight)
    ) {
        items(mediaLibrary.categories) { category ->
            CategoryTitle(text = category.title ?: "Unknown")
            CategoryContent(
                mediaLibrary = mediaLibrary,
                category = category,
                onClick = navigate
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CategoryTitle(
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.h4
) {
    ListItem(
        text = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = textStyle
            )
        },
        trailing = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Expand more"
                )
            }
        }
    )
}

@Composable
private fun CategoryContent(
    mediaLibrary: MediaLibrary,
    category: MediaData.Folder,
    onClick: (String) -> Unit = {}
) {
    val itemsInCategory = remember { mutableStateOf<List<MediaData.Folder>>(emptyList()) }

    LaunchedEffect(category) {
        itemsInCategory.value = mediaLibrary.getItemsInFolder(category).map {
            it as MediaData.Folder
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(itemsInCategory.value) { item ->
            CategoryCard(
                item = item,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    item: MediaData.Folder,
    onClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(AlbumCoverSize),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1.0f)
                .clip(MaterialTheme.shapes.medium)
                .clickable {
                    onClick(item.title ?: "Unknown")
                },
            model = item.albumUri,
            contentDescription = item.title
        )
        Text(
            text = item.title ?: "Unknown",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val AlbumCoverSize = 150.dp
