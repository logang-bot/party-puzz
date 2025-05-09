package com.restrusher.partypuzz.ui.views.createPlayer

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.os.BuildCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.CameraPermissionTextProvider
import com.restrusher.partypuzz.ui.common.PermissionDialog
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.utils.getActivity
import com.restrusher.partypuzz.utils.openAppSettings
import java.io.File
import java.util.Objects

var permissionsToRequest = arrayOf(
    Manifest.permission.CAMERA
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CreatePlayerScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    viewModel: CreatePlayerViewModel = viewModel()
) {
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val context = LocalContext?.current
    val mainActivity = context?.getActivity()
    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        permissionsToRequest.forEach { permission ->
            viewModel.onPermissionResult(
                permission = permission,
                isGranted = perms[permission] == true
            )
        }
    }

    val tempFile = File.createTempFile(
        "picture_${System.currentTimeMillis()}", ".png", context?.cacheDir
    ).apply {
        createNewFile()
    }
    val uri = FileProvider.getUriForFile(
        context?.applicationContext!!,
        "${context?.packageName}.provider",
        tempFile
    )

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()) { it ->
        capturedImageUri = uri
    }

    setAppBarTitle(stringResource(id = R.string.create_player))
    var playerName by remember { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .sharedBounds(
                rememberSharedContentState(key = "bounds"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(
                    tween(
                        500, easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut(
                    tween(
                        500, easing = FastOutSlowInEasing
                    )
                ),
                boundsTransform = BoundsTransform { _: Rect, _: Rect ->
                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                }),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ImageOptionsContainer(
                {
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier
                    .padding(24.dp))
            EditPlayerCard(capturedImageUri)
            NameOptionsContainer(value = playerName, onValueChanged = { playerName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp))
        }

        Button(onClick = {  }, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)) {
            Text(text = stringResource(id = R.string.confirm).uppercase(), style = MaterialTheme.typography.headlineSmall)
        }
    }

    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> CameraPermissionTextProvider()
                    else -> return@forEach
                },
                isPermanentlyDeclined = !mainActivity!!.shouldShowRequestPermissionRationale(
                    permission
                ),
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = {
                    mainActivity?.openAppSettings()
                }
            )
        }
}

//fun Context.createTempPictureUri(
//    provider: String = "${BuildConfig.APPLICATION_ID}.provider",
//    fileName: String = "picture_${System.currentTimeMillis()}",
//    fileExtension: String = ".png"
//): Uri {
//    val tempFile = File.createTempFile(
//        fileName, fileExtension, cacheDir
//    ).apply {
//        createNewFile()
//    }
//
//    return FileProvider.getUriForFile(applicationContext, provider, tempFile)
//}

@Composable
fun ImageOptionsContainer(
    takePictureAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        ImageOptionButton(R.drawable.ic_camera, R.string.take_photo, takePictureAction)
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 5.dp))
        ImageOptionButton(R.drawable.ic_random, R.string.generate_random_image, {})
    }
}

@Composable
fun ImageOptionButton(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    action: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier
        .clickable(onClick = action)
        .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(icon),
            contentDescription = stringResource(text),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(text), lineHeight = 16.sp, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun EditPlayerCard(
    imageUri: Uri,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(300.dp)
            .clip(RoundedCornerShape(70.dp))
    ) {
        if (imageUri.path?.isNotEmpty() == true) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(
                    id = R.string.player_avatar
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_dummy_avatar),
                contentDescription = stringResource(
                    id = R.string.player_avatar
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent, colorResource(id = R.color.black)
                        )
                    )
                )
                .padding(top = 50.dp, start = 6.dp, end = 6.dp, bottom = 5.dp)
        ) {
            Text(
                text = "John Doe Something",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.white),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }
    }
}

@Composable
fun NameOptionsContainer(
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChanged,
            label = { Text(text = stringResource(id = R.string.players_name), modifier = Modifier.alpha(0.4f)) },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        Image(
            painter = painterResource(R.drawable.ic_random),
            contentDescription = stringResource(R.string.generate_random_name),
            modifier = Modifier.fillMaxWidth(),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun CreatePlayerScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                CreatePlayerScreen({}, animatedVisibilityScope = this)
            }
        }
    }
}