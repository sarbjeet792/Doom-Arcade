package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.managers.SaveManager
import com.example.model.PlayerSaveData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `verify app_name resource and SaveManager corruption recovery`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(context is DoomArcadeApplication)
        assertEquals("Doom Arcade", context.getString(R.string.app_name))

        val saveManager = SaveManager(context)
        val initial = PlayerSaveData(
            playerName = "CYBER_CHAMP",
            level = 4,
            xp = 320,
            arcadeWins = 3
        )
        assertTrue(saveManager.saveData(initial))

        saveManager.simulateCorruptPrimarySaveForTest()
        val recovered = saveManager.loadSaveData()
        assertEquals("CYBER_CHAMP", recovered.playerName)
        assertEquals(4, recovered.level)
        assertTrue("sky_temple" in recovered.unlockedArenaIds)
        assertNotNull(recovered.lastRecoveryNote)
    }

    @Test
    fun `verify CUJ from Modern Home Dashboard to Selection to Combat Arena with Surge and Styles`() {
        composeTestRule.waitForIdle()

        // 1. Modern Home Dashboard displays Quick Fight CTA and 2x2 Bento Mode Grid
        composeTestRule.onNodeWithTag("main_menu_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_quick_fight").assertIsDisplayed()
        composeTestRule.onNodeWithTag("mode_card_arcade").performClick()
        composeTestRule.waitForIdle()

        // 2. Selection screen displays fighters, Fight Styles, and launches match
        composeTestRule.onNodeWithTag("selection_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("select_player_vex").performClick()
        composeTestRule.onNodeWithTag("btn_start_match").performClick()
        composeTestRule.waitForIdle()

        // 3. Combat match screen renders 2.5D arena canvas, Fight Style HUD pills, Overdrive Surge, and touch controls
        composeTestRule.onNodeWithTag("combat_match_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("arena_25d_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hud_style_power").performClick()
        composeTestRule.onNodeWithTag("btn_touch_surge").performClick()
        composeTestRule.onNodeWithTag("btn_touch_light").performClick()
        composeTestRule.onNodeWithTag("btn_touch_heavy").performClick()
        composeTestRule.onNodeWithTag("btn_pause_match").performClick()
        composeTestRule.waitForIdle()

        // 4. Pause modal opens
        composeTestRule.onNodeWithTag("pause_menu_modal").assertIsDisplayed()
    }
}
