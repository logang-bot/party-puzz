package com.restrusher.partypuzl.screenshots

import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.navigation.CircleMasterRoute
import com.restrusher.partypuzl.navigation.CreatePlayerScreen
import com.restrusher.partypuzl.navigation.CustomPackEditorRoute
import com.restrusher.partypuzl.navigation.GameConfigScreen
import com.restrusher.partypuzl.navigation.PartiesScreen
import com.restrusher.partypuzl.navigation.PartyDetailScreen
import com.restrusher.partypuzl.navigation.SettingsScreen
import com.restrusher.partypuzl.navigation.SimonSaysRoute
import com.restrusher.partypuzl.navigation.TapWarRoute
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Walks the app for the Play Store listing, once per language × theme variant. The shots land on
 * the PC in `store-screenshots/` — see docs/store-screenshots.md. Each test numbers its shots from
 * a fixed offset so the files sort in listing order.
 */
@RunWith(Parameterized::class)
class ScreenshotTour(private val variant: TourArgs) {

    private lateinit var driver: TourDriver

    @Before
    fun setUp() {
        driver = TourDriver(variant)
    }

    @After
    fun tearDown() = driver.close()

    @Test
    fun homeAndSetup() {
        driver.launch(firstShotNumber = 0)
        driver.capture("home")
        driver.navigate(classicConfigRoute())
        driver.capture("game_config")
        driver.navigate(CreatePlayerScreen())
        driver.capture("create_player")
    }

    @Test
    fun gameDeal() {
        driver.launch(firstShotNumber = 3)
        startClassicGame()
        promoteTruthOrDare()
        driver.capture("game_deal_choice")
        driver.tapExactText(R.string.truth)
        driver.settle(CARD_REVEAL_MS)
        driver.capture("game_challenge")
    }

    @Test
    fun miniGames() {
        driver.launch(firstShotNumber = 5)
        driver.navigate(CircleMasterRoute)
        driver.settle(COUNTDOWN_MS)
        driver.drawCircle()
        driver.settle(TourDriver.LONG_SETTLE_MS)
        driver.capture("minigame_circle_master")
        driver.navigate(tapWarRoute())
        driver.settle(COUNTDOWN_MS)
        driver.capture("minigame_tap_war")
        driver.navigate(SimonSaysRoute)
        driver.settle(SIMON_FIRST_FLASH_MS)
        driver.capture("minigame_simon_says")
    }

    @Test
    fun partiesAndPacks() {
        driver.launch(firstShotNumber = 8)
        driver.navigate(PartiesScreen)
        driver.capture("parties")
        driver.navigate(PartyDetailScreen(driver.demo.partyId))
        driver.capture("party_detail")
        driver.navigate(CustomPackEditorRoute(driver.demo.packId))
        driver.capture("custom_pack")
        driver.navigate(SettingsScreen)
        driver.capture("settings")
    }

    private fun startClassicGame() {
        driver.navigate(classicConfigRoute())
        driver.tapText(R.string.start_the_party)
        driver.settle(TourDriver.LONG_SETTLE_MS)
        driver.tapScreenCenter()
        driver.settle(TourDriver.LONG_SETTLE_MS)
    }

    private fun classicConfigRoute() = GameConfigScreen(
        gameModeName = R.string.standard_game_mode,
        gameModeImage = R.drawable.ic_standard,
        gameModeDescription = R.string.standard_description,
        partyId = driver.demo.partyId
    )

    private fun promoteTruthOrDare() {
        driver.tapTextIfShown(R.string.truth_or_dare)
        driver.settle(TourDriver.LONG_SETTLE_MS)
    }

    private fun tapWarRoute(): TapWarRoute {
        val (first, second) = driver.demo.players
        return TapWarRoute(
            player1Name = first.nickName,
            player1PhotoPath = null,
            player1AvatarName = first.avatarName,
            player2Name = second.nickName,
            player2PhotoPath = null,
            player2AvatarName = second.avatarName
        )
    }

    companion object {
        private const val COUNTDOWN_MS = 4_500L
        private const val SIMON_FIRST_FLASH_MS = 4_700L
        private const val CARD_REVEAL_MS = 2_500L

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun variants(): List<TourArgs> = TourArgs.variantsToRun()

        @JvmStatic
        @BeforeClass
        fun tidyStatusBar() = StatusBarDemoMode.enter()

        @JvmStatic
        @AfterClass
        fun restoreStatusBar() = StatusBarDemoMode.exit()
    }
}
