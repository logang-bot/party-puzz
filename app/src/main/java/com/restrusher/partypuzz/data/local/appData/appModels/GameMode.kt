package com.restrusher.partypuzz.data.local.appData.appModels

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class GameMode (
    @DrawableRes val imageId: Int,
    @StringRes val name: Int,
    @StringRes val description: Int,
)