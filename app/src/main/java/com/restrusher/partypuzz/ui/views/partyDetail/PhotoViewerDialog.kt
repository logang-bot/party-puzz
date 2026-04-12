package com.restrusher.partypuzz.ui.views.partyDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.entities.PartyPhotoEntity
import java.io.File

@Composable
fun PhotoViewerDialog(
    photos: List<PartyPhotoEntity>,
    initialIndex: Int,
    downloadResult: DownloadResult?,
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit,
    onDownloadResultConsumed: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(id = R.string.download_success)
    val failedMessage = stringResource(id = R.string.download_failed)

    LaunchedEffect(downloadResult) {
        val result = downloadResult ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(if (result == DownloadResult.SUCCESS) successMessage else failedMessage)
        onDownloadResultConsumed()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            PhotoPager(
                photos = photos,
                pagerState = pagerState,
                modifier = Modifier.fillMaxSize()
            )
            PhotoViewerTopBar(
                onDismiss = onDismiss,
                onDownload = { onDownload(photos[pagerState.currentPage].photoPath) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PhotoPager(
    photos: List<PartyPhotoEntity>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        AsyncImage(
            model = ImageRequest.Builder(context).data(File(photos[page].photoPath)).build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PhotoViewerTopBar(
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(4.dp)
    ) {
        IconButton(onClick = onDismiss) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.close),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = stringResource(id = R.string.photo_options),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.download_photo)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDownload()
                    }
                )
            }
        }
    }
}
