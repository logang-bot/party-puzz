package com.restrusher.partypuzz.navigation

import com.restrusher.partypuzz.R

object NavigationTitles {
    private val navigationRoutesMap = mapOf(
        HomeScreen::class.qualifiedName to R.string.home_screen,
        GameConfigScreen::class.qualifiedName to R.string.prepare_your_party,
    )

    fun getScreenTitle(route: String?): Int {
        return navigationRoutesMap[route] ?: R.string.home_screen
    }
}