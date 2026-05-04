package com.restrusher.partypuzz.ui.views.createPlayer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import com.restrusher.partypuzz.ui.common.LockScreenOrientation
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.common.CameraPermissionTextProvider
import com.restrusher.partypuzz.ui.common.PermissionDialog
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.utils.getActivity
import com.restrusher.partypuzz.utils.openAppSettings
import java.io.File

var permissionsToRequest = arrayOf(
    Manifest.permission.CAMERA
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CreatePlayerScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    playerId: Int = -1,
    navigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreatePlayerViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val mainActivity = context.getActivity()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { navigateBack() }
    }

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsToRequest.forEach { permission ->
            viewModel.onPermissionResult(
                permission = permission,
                isGranted = perms[permission] == true
            )
        }
    }

    val uri = remember {
        val tempFile = File.createTempFile(
            "picture_${System.currentTimeMillis()}", ".png", context.cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }

        FileProvider.getUriForFile(
            context.applicationContext,
            "${context.packageName}.provider",
            tempFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.onCapturedImage(uri)
    }

    setAppBarTitle(
        stringResource(if (uiState.isEditMode) R.string.edit_player else R.string.new_player)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    rememberSharedContentState(key = if (playerId == -1) "bounds" else "player_card_$playerId"),
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
                    })
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            PlayerFormContent(
                capturedImageUri = uiState.capturedImageUri,
                playerName = uiState.playerName,
                avatarRes = uiState.randomAvatarRes,
                existingPhotoPath = uiState.existingPhotoPath,
                gender = uiState.gender,
                interestedIn = uiState.interestedIn,
                isCouplesMode = uiState.isCouplesMode,
                onTakePicture = {
                    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isCameraPermissionGranted)
                        cameraLauncher.launch(uri)
                    else
                        multiplePermissionResultLauncher.launch(permissionsToRequest)
                },
                onGenerateRandomImage = {
                    val index = viewModel.randomAvatarIndex()
                    val resId = context.resources.getIdentifier(
                        "img_dummy_avatar_$index", "drawable", context.packageName
                    )
                    viewModel.onRandomAvatarRequested(if (resId != 0) resId else null)
                },
                onPlayerNameChanged = viewModel::onPlayerNameChanged,
                onGenerateRandomName = { viewModel.onPlayerNameChanged(viewModel.generateRandomName()) },
                onGenderSelected = viewModel::onGenderSelected,
                onInterestedInSelected = viewModel::onInterestedInSelected,
                modifier = Modifier.weight(1f)
            )

            ConfirmButton(
                onClick = { viewModel.confirmPlayer() },
                enabled = uiState.playerName.isNotBlank() && uiState.interestedIn != null,
                isEditMode = uiState.isEditMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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
                    mainActivity.openAppSettings()
                }
            )
        }
}

@Composable
fun PlayerFormContent(
    capturedImageUri: Uri,
    playerName: String,
    avatarRes: Int?,
    gender: Gender?,
    interestedIn: InterestedIn?,
    onTakePicture: () -> Unit,
    onGenerateRandomImage: () -> Unit,
    onPlayerNameChanged: (String) -> Unit,
    onGenerateRandomName: () -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onInterestedInSelected: (InterestedIn) -> Unit,
    isCouplesMode: Boolean,
    modifier: Modifier = Modifier,
    existingPhotoPath: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        StepLabel(stringResource(R.string.step_pick_face))
        ImageOptionsContainer(
            capturedImageUri = capturedImageUri,
            takePictureAction = onTakePicture,
            generateRandomImageAction = onGenerateRandomImage,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        EditPlayerCard(
            imageUri = capturedImageUri,
            avatarRes = avatarRes,
            existingPhotoPath = existingPhotoPath,
            onShuffleClick = onGenerateRandomImage,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        StepLabel(stringResource(R.string.step_pick_name))
        NameOptionsContainer(
            value = playerName,
            onValueChanged = onPlayerNameChanged,
            onGenerateRandomName = onGenerateRandomName,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Text(
            text = stringResource(R.string.name_auto_generated_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
        )
        AnimatedVisibility(visible = isCouplesMode) {
            Column(modifier = Modifier.fillMaxWidth()) {
                StepLabel(stringResource(R.string.step_pick_gender))
                GenderOptionsContainer(
                    selectedGender = gender,
                    onGenderSelected = onGenderSelected,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
        AnimatedVisibility(visible = isCouplesMode) {
            Column(modifier = Modifier.fillMaxWidth()) {
                StepLabel(stringResource(R.string.step_pick_interested_in))
                InterestedInOptionsContainer(
                    selectedInterestedIn = interestedIn,
                    onInterestedInSelected = onInterestedInSelected,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StepLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
}

@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val disabledBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val disabledText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) onPrimary else primary,
        animationSpec = tween(300), label = "bg"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isPressed) primary else onPrimary,
        animationSpec = tween(300), label = "text"
    )
    val bgColor = if (enabled) animatedBgColor else disabledBg
    val textColor = if (enabled) animatedTextColor else disabledText

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .background(color = bgColor, shape = RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (isEditMode) R.string.update else R.string.add_to_the_party
                ).uppercase(),
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerFormContentPreview() {
    PartyPuzzTheme {
        PlayerFormContent(
            capturedImageUri = Uri.EMPTY,
            playerName = "Alex",
            avatarRes = null,
            gender = null,
            interestedIn = null,
            isCouplesMode = false,
            onTakePicture = {},
            onGenerateRandomImage = {},
            onPlayerNameChanged = {},
            onGenerateRandomName = {},
            onGenderSelected = {},
            onInterestedInSelected = {}
        )
    }
}
