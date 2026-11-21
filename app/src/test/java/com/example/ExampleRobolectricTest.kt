package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.down
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.engine.CombatPhase
import com.example.engine.CombatState
import com.example.engine.PriorityHolder
import com.example.model.AdventureCatalog
import com.example.model.CardCatalog
import com.example.model.CardTemplate
import com.example.model.UnitOnBoard
import com.example.ui.components.BattleBoard
import com.example.ui.theme.PathOfChampionsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w850dp-h390dp-land")
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Con Đường Anh Hùng", appName)
  }

  @Test
  fun `battleBoard arranges AI bench top shared battlefield center and player bench bottom in landscape`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val enemyUnit = UnitOnBoard(
      instanceId = "e_1",
      template = CardCatalog.getCard("card_cithria"),
      ownerIsPlayer = false,
      power = 2,
      health = 2,
      maxHealth = 2,
      keywords = emptySet()
    )
    val playerUnits = (1..6).map { idx ->
      UnitOnBoard(
        instanceId = "p_$idx",
        template = CardCatalog.getCard("card_cithria"),
        ownerIsPlayer = true,
        power = 2,
        health = 2,
        maxHealth = 2,
        keywords = emptySet()
      )
    }
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 15,
      enemyMaxHp = 15,
      playerBoard = playerUnits,
      enemyBoard = listOf(enemyUnit),
      phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
      attackingUnitIds = listOf("p_1")
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 600.dp, height = 270.dp)) {
          BattleBoard(combat = combatState)
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("battle_board").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_bench_zone").assertIsDisplayed()
    composeTestRule.onNodeWithTag("shared_battlefield_zone").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_bench_zone").assertIsDisplayed()

    val boardBounds = composeTestRule.onNodeWithTag("battle_board").getUnclippedBoundsInRoot()
    val aiBounds = composeTestRule.onNodeWithTag("enemy_bench_zone").getUnclippedBoundsInRoot()
    val fieldBounds = composeTestRule.onNodeWithTag("shared_battlefield_zone").getUnclippedBoundsInRoot()
    val playerBounds = composeTestRule.onNodeWithTag("player_bench_zone").getUnclippedBoundsInRoot()

    // Landscape orientation: board is wider than tall
    assertTrue((boardBounds.right - boardBounds.left) > (boardBounds.bottom - boardBounds.top))
    // Strict vertical ordering: AI Bench (top) -> Shared Battlefield (center) -> Player Bench (bottom)
    assertTrue(aiBounds.bottom <= fieldBounds.top)
    assertTrue(fieldBounds.bottom <= playerBounds.top)

    // Attacking unit p_1 renders once (in Shared Battlefield, not duplicated on Player Bench)
    assertEquals(1, composeTestRule.onAllNodesWithTag("board_unit_p_1").fetchSemanticsNodes().size)
    // Remaining bench units p_2..p_6 and AI bench unit e_1 render cleanly
    assertEquals(1, composeTestRule.onAllNodesWithTag("board_unit_e_1").fetchSemanticsNodes().size)
    assertEquals(1, composeTestRule.onAllNodesWithTag("board_unit_p_6").fetchSemanticsNodes().size)
  }

  @Test
  fun `combatScreen renders LoR layout with current_max mana and no wireframe labels`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val mysticShot = CardCatalog.getCard("card_mystic_shot")
    val hand = listOf(
      com.example.model.HandCardInstance(
        instanceId = "hc_1",
        template = cithria,
        effectiveCost = cithria.cost
      ),
      com.example.model.HandCardInstance(
        instanceId = "hc_2",
        template = mysticShot,
        effectiveCost = mysticShot.cost
      )
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_yasuo",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 4,
      playerNexusHp = 45,
      playerMaxHp = 45,
      enemyNexusHp = 16,
      enemyMaxHp = 20,
      playerMana = 4,
      playerMaxMana = 6,
      playerSpellMana = 2,
      enemyMana = 3,
      enemyMaxMana = 5,
      enemySpellMana = 1,
      playerHand = hand,
      enemyHand = listOf(cithria, mysticShot),
      playerBoard = listOf(
        UnitOnBoard(
          instanceId = "p_1",
          template = cithria,
          ownerIsPlayer = true,
          power = 4,
          health = 3,
          maxHealth = 3,
          keywords = emptySet()
        )
      ),
      enemyBoard = listOf(
        UnitOnBoard(
          instanceId = "e_1",
          template = cithria,
          ownerIsPlayer = false,
          power = 2,
          health = 2,
          maxHealth = 2,
          keywords = emptySet()
        )
      )
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("enemy_mana_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_mana_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_spell_mana_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_spell_mana_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_spell_mana_gem_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_spell_mana_gem_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("player_spell_mana_gem_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_nexus_hp").assertIsDisplayed().assertTextEquals("16")
    composeTestRule.onNodeWithTag("player_nexus_hp").assertIsDisplayed().assertTextEquals("45")
    val enemyHpBounds = composeTestRule.onNodeWithTag("enemy_nexus_hp").getUnclippedBoundsInRoot()
    val playerHpBounds = composeTestRule.onNodeWithTag("player_nexus_hp").getUnclippedBoundsInRoot()
    assertTrue(enemyHpBounds.bottom < playerHpBounds.top)
    composeTestRule.onNodeWithTag("combat_log_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("end_round_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_hand_count").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_cithria").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").assertIsDisplayed()

    // Verify hand cards stay within screen bottom & right bounds (never clipped)
    val cithriaBounds = composeTestRule.onNodeWithTag("hand_card_card_cithria").getUnclippedBoundsInRoot()
    val mysticBounds = composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").getUnclippedBoundsInRoot()
    assertTrue("cithriaBounds=$cithriaBounds expected bottom <= 390.dp", cithriaBounds.bottom <= 390.dp)
    assertTrue("mysticBounds=$mysticBounds expected bottom <= 390.dp", mysticBounds.bottom <= 390.dp)
    assertTrue("mysticBounds=$mysticBounds expected right <= 850.dp", mysticBounds.right <= 850.dp)

    // Verify Spell Mana badge and all 3 sockets stay unclipped inside screen bounds and above hand
    val spellBadgeBounds = composeTestRule.onNodeWithTag("player_spell_mana_badge").getUnclippedBoundsInRoot()
    assertTrue(spellBadgeBounds.right <= 850.dp)
    assertTrue(spellBadgeBounds.bottom <= 390.dp)
    assertTrue(spellBadgeBounds.bottom <= mysticBounds.top)

    // Verify right-edge mana dial texts always display X/10 even when maxMana < 10
    assertEquals(
      1,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("3/10")
      ).fetchSemanticsNodes().size
    )
    assertEquals(
      1,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("4/10")
      ).fetchSemanticsNodes().size
    )
    composeTestRule.onNodeWithTag("player_spell_mana_text").assertTextEquals("2")

    // Verify wireframe placeholder labels are gone
    assertEquals(
      0,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("AI BENCH", substring = true)
      ).fetchSemanticsNodes().size
    )
    assertEquals(
      0,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("PLAYER BENCH", substring = true)
      ).fetchSemanticsNodes().size
    )
    assertEquals(
      0,
      composeTestRule.onAllNodes(
        androidx.compose.ui.test.hasText("Shared Battlefield", substring = true)
      ).fetchSemanticsNodes().size
    )
  }

  @Test
  fun `anivia newly generated card drawables decode and render in CombatScreen without crashing`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val aniviaCards = listOf(
      "card_anivia",
      "card_flame_eyed_caprine",
      "card_harsh_winds",
      "card_mortal_prophet",
      "card_guardian_follower",
      "card_wings_of_cryophoenix"
    ).mapIndexed { idx, id ->
      val tmpl = CardCatalog.getCard(id)
      com.example.model.HandCardInstance(
        instanceId = "anv_hc_$idx",
        template = tmpl,
        effectiveCost = tmpl.cost
      )
    }
    val eggUnit = UnitOnBoard(
      instanceId = "egg_1",
      template = CardCatalog.getCard("card_eggnivia"),
      ownerIsPlayer = true,
      power = 0,
      health = 1,
      maxHealth = 1,
      keywords = emptySet()
    )
    val stormbird = UnitOnBoard(
      instanceId = "storm_1",
      template = CardCatalog.getCard("card_crystal_stormbird"),
      ownerIsPlayer = false,
      power = 3,
      health = 3,
      maxHealth = 3,
      keywords = emptySet()
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 2,
      playerNexusHp = 35,
      playerMaxHp = 35,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 5,
      playerMaxMana = 5,
      playerHand = aniviaCards,
      enemyHand = listOf(CardCatalog.getCard("card_harsh_winds")),
      playerBoard = listOf(eggUnit),
      enemyBoard = listOf(stormbird)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("battle_board").assertIsDisplayed()
    composeTestRule.onNodeWithTag("enemy_hand_count").assertIsDisplayed()
    composeTestRule.onNodeWithTag("end_round_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_anivia").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_harsh_winds").assertIsDisplayed()
    composeTestRule.onNodeWithTag("board_unit_egg_1").assertIsDisplayed()
  }

  @Test
  fun `homeHubScreen renders champion roster and command card with LoR backdrop`() {
    val aniviaLoadout = com.example.model.ChampionLoadout(
      championId = "champ_anivia",
      isUnlocked = true,
      starLevel = 2,
      championLevel = 5
    )
    val uiState = com.example.viewmodel.GameUiState(
      selectedChampionId = "champ_anivia",
      championLoadouts = mapOf("champ_anivia" to aniviaLoadout)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.HomeHubScreen(
            uiState = uiState,
            onSelectChampion = {},
            onUnlockChampion = {},
            onOpenChampionDetail = {},
            onOpenAdventures = {},
            onResumeRun = {},
            onAbandonRun = {},
            onGrantUnlimitedTestResources = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("roster_champ_champ_anivia").assertIsDisplayed()
    composeTestRule.onNodeWithTag("champion_mastery_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("embark_adventure_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_battle_button").assertIsDisplayed()
  }

  @Test
  fun `championDetailScreen renders LoR 3-column layout with Star Powers Bonus Stars Deck and Relic sockets`() {
    val aniviaLoadout = com.example.model.ChampionLoadout(
      championId = "champ_anivia",
      isUnlocked = true,
      starLevel = 2,
      championLevel = 8,
      fragments = 60,
      unlockedRelicSlots = 2
    )
    val uiState = com.example.viewmodel.GameUiState(
      selectedChampionId = "champ_anivia",
      championLoadouts = mapOf("champ_anivia" to aniviaLoadout)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.ChampionDetailScreen(
            championId = "champ_anivia",
            uiState = uiState,
            onUpgradeStar = { _, _ -> },
            onUnlockConstellation = { _, _ -> },
            onEquipRelic = { _, _, _ -> },
            onCosmicForge = {},
            onMaxChampionForTesting = {},
            onBack = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Header + Left Rail + Star Powers sub-tab + Right Relic Sockets
    composeTestRule.onNodeWithTag("champ_detail_back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("max_champ_test_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("champ_section_overview").assertIsDisplayed()
    composeTestRule.onNodeWithTag("champ_section_constellation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("champ_section_starter_deck").assertIsDisplayed()
    composeTestRule.onNodeWithTag("champ_section_relics").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_star_powers").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_bonus_stars").assertIsDisplayed()
    composeTestRule.onNode(androidx.compose.ui.test.hasText("STAR POWERS")).assertIsDisplayed()
    composeTestRule.onNode(androidx.compose.ui.test.hasText("BONUS STARS")).assertIsDisplayed()
    composeTestRule.onNodeWithTag("upgrade_star_3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("relic_socket_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("relic_socket_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("relic_socket_2").assertIsDisplayed()

    // Switch to Bonus Stars sub-tab
    composeTestRule.onNodeWithTag("tab_bonus_stars").performClick()
    composeTestRule.waitForIdle()
    val firstNodeId = CardCatalog.getChampion("champ_anivia").constellationNodes.first().id
    composeTestRule.onNodeWithTag("unlock_node_$firstNodeId").assertIsDisplayed()

    // Switch to Starter Deck slim strips and click second card to update right-column card showcase
    composeTestRule.onNodeWithTag("champ_section_starter_deck").performClick()
    composeTestRule.waitForIdle()
    val deck = CardCatalog.getChampion("champ_anivia").baseDeck
    val firstDeckCardId = deck.first().cardId
    val secondDeckEntry = deck[1]
    val secondDeckCard = CardCatalog.getCard(secondDeckEntry.cardId)
    composeTestRule.onNodeWithTag("deck_strip_$firstDeckCardId").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deck_strip_${secondDeckEntry.cardId}").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(
      androidx.compose.ui.test.hasText("${secondDeckCard.name} • x${secondDeckEntry.copies}", substring = true)
    ).assertIsDisplayed()

    // Switch to Relics tab
    composeTestRule.onNodeWithTag("champ_section_relics").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("relic_slot_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cosmic_forge_button").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `championDetailScreen compact portrait renders underline tabs and starter deck`() {
    val yasuoLoadout = com.example.model.ChampionLoadout(
      championId = "champ_yasuo",
      isUnlocked = true,
      starLevel = 1,
      championLevel = 4
    )
    val uiState = com.example.viewmodel.GameUiState(
      selectedChampionId = "champ_yasuo",
      championLoadouts = mapOf("champ_yasuo" to yasuoLoadout)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 390.dp, height = 780.dp)) {
          com.example.ui.screens.ChampionDetailScreen(
            championId = "champ_yasuo",
            uiState = uiState,
            onUpgradeStar = { _, _ -> },
            onUnlockConstellation = { _, _ -> },
            onEquipRelic = { _, _, _ -> },
            onCosmicForge = {},
            onMaxChampionForTesting = {},
            onBack = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("champ_section_constellation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("champ_section_starter_deck").assertIsDisplayed()
    composeTestRule.onNodeWithTag("upgrade_star_2").assertIsDisplayed()

    composeTestRule.onNodeWithTag("champ_section_starter_deck").performClick()
    composeTestRule.waitForIdle()
    val firstDeckCardName = CardCatalog.getCard(
      CardCatalog.getChampion("champ_yasuo").baseDeck.first().cardId
    ).name
    composeTestRule.onNode(
      androidx.compose.ui.test.hasText(firstDeckCardName)
    ).assertIsDisplayed()
  }

  @Test
  fun `adventureSelectAndMapScreens render ornate LoR metallic panels and nodes`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val firstNode = adv.mapNodes.first()
    val run = com.example.model.RunState(
      runId = "test_run",
      championId = "champ_anivia",
      adventureId = adv.id,
      currentNodeId = firstNode.id,
      completedNodeIds = listOf(firstNode.id),
      currentNexusHp = 35,
      maxNexusHp = 35,
      gold = 100,
      rerollsRemaining = 2,
      revivesRemaining = 1,
      activePowerIds = emptyList(),
      runDeck = CardCatalog.getChampion("champ_anivia").baseDeck,
      battlesWon = 0
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.AdventureMapScreen(
            run = run,
            isNodeAvailable = { r, node ->
              val current = adv.mapNodes.first { it.id == r.currentNodeId }
              current.nextNodeIds.contains(node.id)
            },
            onSelectNode = {},
            onAbandonRun = {},
            onBackHome = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("map_back_home_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("map_node_${firstNode.id}").assertIsDisplayed()
  }

  @Test
  fun `rightManaActionDial displays ENEMY TURN ATTACK BLOCK CAST and PASS labels by context`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val mysticShot = CardCatalog.getCard("card_mystic_shot")
    val playerUnit = UnitOnBoard(
      instanceId = "p_1",
      template = cithria,
      ownerIsPlayer = true,
      power = 2,
      health = 2,
      maxHealth = 2,
      keywords = emptySet()
    )
    val enemyUnit = UnitOnBoard(
      instanceId = "e_1",
      template = cithria,
      ownerIsPlayer = false,
      power = 2,
      health = 2,
      maxHealth = 2,
      keywords = emptySet()
    )
    val base = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 1,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      playerNexusHp = 30,
      playerMaxHp = 30,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerBoard = listOf(playerUnit),
      enemyBoard = listOf(enemyUnit)
    )
    val currentState = androidx.compose.runtime.mutableStateOf(base)

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.components.RightManaActionDial(
            combat = currentState.value,
            dialWidth = 180.dp,
            dialHeight = 190.dp,
            actionDiameter = 76.dp,
            descFontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
            statFontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
            onEnterAttack = {},
            onCancelAttack = {},
            onCommitAttack = {},
            onCommitBlock = {},
            onPassTurn = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("end_round_button").assertTextEquals("BỎ QUA")

    composeTestRule.runOnUiThread {
      currentState.value = base.copy(priorityHolder = com.example.engine.PriorityHolder.ENEMY)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("end_round_button").assertTextEquals("ĐỊCH\nĐANG ĐI")

    composeTestRule.runOnUiThread {
      currentState.value = base.copy(
        phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
        attackingUnitIds = listOf("p_1")
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("commit_attack_button").assertTextEquals("TẤN CÔNG\n1")

    composeTestRule.runOnUiThread {
      currentState.value = base.copy(
        playerHasAttackToken = false,
        enemyHasAttackToken = false,
        playerInitiatedCombat = false,
        phase = CombatPhase.COMBAT_DECLARE_BLOCK_PHASE,
        attackingUnitIds = listOf("e_1"),
        blockPairings = mapOf("e_1" to "p_1")
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("commit_block_button").assertTextEquals("CHẶN\n1")

    composeTestRule.runOnUiThread {
      currentState.value = base.copy(
        phase = CombatPhase.SPELL_STACK_PHASE,
        spellStack = listOf(
          com.example.engine.SpellStackEntry(
            id = "st_1",
            sourceIsPlayer = false,
            card = mysticShot
          )
        )
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("end_round_button").assertTextEquals("DÙNG")
  }

  @Test
  fun `cardTile loads globals-vi_vn json from assets and shows localized keyword and spell speed tooltips on tap`() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    com.example.model.LocalizationRepository.clearForTesting()
    val loaded = CardCatalog.ensureLocalized(context)
    assertTrue("Expected globals-vi_vn.json asset to load", loaded)

    val testUnit = CardTemplate(
      id = "test_elusive_unit",
      name = "Ám Ảnh Đêm",
      region = com.example.model.Region.SHADOW_ISLES,
      type = com.example.model.CardType.UNIT,
      cost = 3,
      basePower = 3,
      baseHealth = 2,
      keywords = setOf(com.example.model.Keyword.ELUSIVE, com.example.model.Keyword.QUICK_ATTACK),
      rarity = com.example.model.Rarity.EPIC,
      description = "Khi xuất trận: Đóng Băng 1 kẻ địch."
    )
    val testSpell = CardCatalog.getCard("card_harsh_winds")

    composeTestRule.setContent {
      PathOfChampionsTheme {
        androidx.compose.foundation.layout.Column {
          com.example.ui.components.CardTile(template = testUnit)
          com.example.ui.components.CardTile(template = testSpell)
        }
      }
    }
    composeTestRule.waitForIdle()

    // Verify localized region, rarity, spell speed, and keyword badges
    composeTestRule.onNode(androidx.compose.ui.test.hasText("Quần Đảo Bóng Đêm")).assertIsDisplayed()
    composeTestRule.onNode(androidx.compose.ui.test.hasText("• SỬ THI", substring = true)).assertIsDisplayed()
    composeTestRule.onNodeWithTag("spell_speed_badge_Burst").assertIsDisplayed()
    composeTestRule.onNodeWithTag("keyword_badge_Elusive").assertIsDisplayed()

    // Tap Elusive badge -> opens official Vietnamese tooltip popup from globals-vi_vn.json
    composeTestRule.onNodeWithTag("keyword_badge_Elusive").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("keyword_tooltip_dialog").assertIsDisplayed()
    composeTestRule.onNode(
      androidx.compose.ui.test.hasText("Chỉ có thể bị chặn bởi một đơn vị có Bóng Ma.")
    ).assertIsDisplayed()

    // Close tooltip and tap Spell Speed badge
    composeTestRule.onNodeWithTag("close_keyword_tooltip_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("spell_speed_badge_Burst").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("keyword_tooltip_dialog").assertIsDisplayed()
    composeTestRule.onNode(
      androidx.compose.ui.test.hasText(
        "Có thể sử dụng bất cứ khi nào bạn được hành động. Xảy ra ngay lập tức và cho phép bạn tiếp tục sử dụng các lá bài khác."
      )
    ).assertIsDisplayed()
  }

  @Test
  fun `attackSwordIndicatorMovesBetweenPlayerSideAndEnemySideByAttackToken`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val playerState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 1,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      playerNexusHp = 30,
      playerMaxHp = 30,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerHasAttackToken = true,
      enemyHasAttackToken = false
    )
    val stateHolder = androidx.compose.runtime.mutableStateOf(playerState)

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.components.RightManaActionDial(
            combat = stateHolder.value,
            dialWidth = 180.dp,
            dialHeight = 190.dp,
            actionDiameter = 76.dp,
            descFontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
            statFontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
            onEnterAttack = {},
            onCancelAttack = {},
            onCommitAttack = {},
            onCommitBlock = {},
            onPassTurn = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNode(androidx.compose.ui.test.hasText("LƯỢT CÔNG")).assertIsDisplayed()
    val playerSwordBounds = composeTestRule.onNode(androidx.compose.ui.test.hasText("LƯỢT CÔNG")).getUnclippedBoundsInRoot()

    composeTestRule.runOnUiThread {
      stateHolder.value = playerState.copy(
        playerHasAttackToken = false,
        enemyHasAttackToken = true
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNode(androidx.compose.ui.test.hasText("ĐỊCH CÔNG")).assertIsDisplayed()
    val enemySwordBounds = composeTestRule.onNode(androidx.compose.ui.test.hasText("ĐỊCH CÔNG")).getUnclippedBoundsInRoot()

    // Player's attack sword sits on the lower side; Enemy's attack sword sits on the upper side
    assertTrue(playerSwordBounds.top > enemySwordBounds.top)
  }

  @Test
  fun `aniviaLevelUpTransformsArtworkAndTriggersBattleLevelUpOverlay`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val aniviaLv1 = UnitOnBoard(
      instanceId = "anv_1",
      template = CardCatalog.getCard("card_anivia"),
      ownerIsPlayer = true,
      power = 3,
      health = 5,
      maxHealth = 5,
      keywords = setOf(com.example.model.Keyword.ELEMENTAL),
      isLeveledUp = false
    )
    val aniviaLv2 = aniviaLv1.copy(
      power = 4,
      health = 6,
      maxHealth = 6,
      isLeveledUp = true
    )

    assertEquals(R.drawable.img_champ_anivia, com.example.ui.components.unitPortraitRes(aniviaLv1))
    assertEquals(R.drawable.img_wings_of_cryophoenix, com.example.ui.components.unitPortraitRes(aniviaLv2))

    val baseCombat = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 9,
      playerNexusHp = 35,
      playerMaxHp = 35,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 9,
      playerMaxMana = 9,
      playerBoard = listOf(aniviaLv1)
    )
    val combatHolder = androidx.compose.runtime.mutableStateOf(baseCombat)

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatHolder.value,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()
    assertEquals(0, composeTestRule.onAllNodesWithTag("champion_level_up_overlay").fetchSemanticsNodes().size)

    // Simulate Round Start Enlightened leveling up Anivia to Lv2
    composeTestRule.runOnUiThread {
      combatHolder.value = baseCombat.copy(
        round = 10,
        playerMana = 10,
        playerMaxMana = 10,
        isEnlightened = true,
        playerBoard = listOf(aniviaLv2)
      )
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("champion_level_up_overlay").assertIsDisplayed()
    composeTestRule.onNode(androidx.compose.ui.test.hasText("THĂNG CẤP • CẤP 2", ignoreCase = true)).assertIsDisplayed()
  }

  @Test
  fun `playerHandOverlayExpandsToCenterOnTapAndCollapsesOnScrimTap`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val mysticShot = CardCatalog.getCard("card_mystic_shot")
    val hand = listOf(
      com.example.model.HandCardInstance(
        instanceId = "hc_1",
        template = cithria,
        effectiveCost = cithria.cost
      ),
      com.example.model.HandCardInstance(
        instanceId = "hc_2",
        template = mysticShot,
        effectiveCost = mysticShot.cost
      )
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_yasuo",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 2,
      playerNexusHp = 35,
      playerMaxHp = 35,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 4,
      playerMaxMana = 4,
      playerHand = hand
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Collapsed by default: scrim is absent and rightmost card sits on the right half
    assertEquals(0, composeTestRule.onAllNodesWithTag("player_hand_dismiss_scrim").fetchSemanticsNodes().size)
    val collapsedRightBounds = composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").getUnclippedBoundsInRoot()
    assertTrue(collapsedRightBounds.left > 600.dp)

    // Tap collapsed hand stack -> expands to bottom-center fan and shows dismiss scrim
    composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("player_hand_dismiss_scrim").assertIsDisplayed()
    val expandedCenterX = (
      composeTestRule.onNodeWithTag("hand_card_card_cithria").getUnclippedBoundsInRoot().left +
        composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").getUnclippedBoundsInRoot().right
      ) / 2f
    assertTrue(expandedCenterX in 340.dp..510.dp)

    // Tap scrim outside hand -> collapses back to bottom-right corner
    composeTestRule.onNodeWithTag("player_hand_dismiss_scrim").performClick()
    composeTestRule.waitForIdle()
    assertEquals(0, composeTestRule.onAllNodesWithTag("player_hand_dismiss_scrim").fetchSemanticsNodes().size)
    val recollapsedRightBounds = composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").getUnclippedBoundsInRoot()
    assertTrue(recollapsedRightBounds.left > 600.dp)
  }

  @Test
  fun `spellAndSkillVfxOverlayRendersAndAutoCleansWithoutBlockingUi`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val harshWinds = CardCatalog.getCard("card_harsh_winds")
    val glacialStorm = CardCatalog.getCard("skill_glacial_storm")
    val anivia = UnitOnBoard("anv_1", CardCatalog.getCard("card_anivia"), true, 3, 5, 5, setOf(com.example.model.Keyword.ELEMENTAL))

    val stackedState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 6,
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 6,
      playerMaxMana = 6,
      playerBoard = listOf(anivia),
      spellStack = listOf(
        com.example.engine.SpellStackEntry(
          id = "st_glacial_1",
          sourceIsPlayer = true,
          card = glacialStorm,
          targetUnitId = null,
          damageBonus = 0
        )
      )
    )
    val combatHolder = androidx.compose.runtime.mutableStateOf(stackedState)

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatHolder.value,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()
    assertEquals(0, composeTestRule.onAllNodesWithTag("spell_skill_vfx_overlay").fetchSemanticsNodes().size)

    // Pause clock to inspect mid-animation VFX for Burst spell & resolved Glacial Storm skill
    composeTestRule.mainClock.autoAdvance = false
    try {
      composeTestRule.runOnUiThread {
        combatHolder.value = stackedState.copy(
          spellStack = emptyList(),
          playerGraveyard = listOf(glacialStorm, harshWinds)
        )
      }
      composeTestRule.waitForIdle()
      composeTestRule.mainClock.advanceTimeBy(300L)
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("spell_skill_vfx_overlay").assertIsDisplayed()
      composeTestRule.onNodeWithTag("spell_skill_vfx_skill_glacial_storm").assertIsDisplayed()
      composeTestRule.onNodeWithTag("spell_skill_vfx_card_harsh_winds").assertIsDisplayed()

      // Non-touch-blocking: HUD controls remain clickable while VFX plays
      composeTestRule.onNodeWithTag("emote_button").performClick()
      composeTestRule.mainClock.advanceTimeByFrame()
      composeTestRule.waitForIdle()
      composeTestRule.onNode(androidx.compose.ui.test.hasText("Gửi Biểu Cảm", ignoreCase = true)).assertIsDisplayed()

      // Advance past 900ms animation duration -> overlay auto-cleans
      composeTestRule.mainClock.advanceTimeBy(1000L)
      composeTestRule.mainClock.advanceTimeByFrame()
      composeTestRule.waitForIdle()
      assertEquals(0, composeTestRule.onAllNodesWithTag("spell_skill_vfx_overlay").fetchSemanticsNodes().size)
    } finally {
      composeTestRule.mainClock.autoAdvance = true
    }
  }

  @Test
  fun `enlargedBoardCardsAndExpandedBattlefieldDragDropWorkSmoothly`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val wildMystics = CardCatalog.getCard("card_wild_mystics")
    val guardian = CardCatalog.getCard("card_guardian_follower")
    val cithria = CardCatalog.getCard("card_cithria")
    val hand = listOf(
      com.example.model.HandCardInstance(
        instanceId = "hc_mystics",
        template = wildMystics,
        effectiveCost = wildMystics.cost
      )
    )
    val benchUnit = UnitOnBoard(
      instanceId = "p_guard_1",
      template = guardian,
      ownerIsPlayer = true,
      power = 4,
      health = 5,
      maxHealth = 5,
      keywords = setOf(com.example.model.Keyword.CHALLENGER)
    )
    val enemyUnit = UnitOnBoard(
      instanceId = "e_cith_1",
      template = cithria,
      ownerIsPlayer = false,
      power = 2,
      health = 2,
      maxHealth = 2,
      keywords = setOf(com.example.model.Keyword.TOUGH)
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 3,
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 4,
      playerMaxMana = 4,
      playerHand = hand,
      playerBoard = listOf(benchUnit),
      enemyBoard = listOf(enemyUnit)
    )

    var playedCardId: String? = null
    var draggedBenchAttackUnitId: String? = null

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onPlayTargetedCard = { cardId, _ -> playedCardId = cardId },
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onDragBenchUnitToAttack = { unitId -> draggedBenchAttackUnitId = unitId },
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. Verify board unit tokens are ~15%+ larger than previous ~57.7dp x 71.5dp size
    val unitBounds = composeTestRule.onNodeWithTag("board_unit_p_guard_1").getUnclippedBoundsInRoot()
    val unitWidth = unitBounds.right - unitBounds.left
    val unitHeight = unitBounds.bottom - unitBounds.top
    assertTrue("Expected enlarged board unit width >= 65.dp, got $unitWidth", unitWidth >= 65.dp)
    assertTrue("Expected enlarged board unit height >= 80.dp, got $unitHeight", unitHeight >= 80.dp)
    // Enemy unit without .jpg drawable renders its name/artwork cleanly
    composeTestRule.onNodeWithTag("board_unit_e_cith_1").assertIsDisplayed()

    // 2. Dragging bench unit high up toward enemy side of expanded battlefield still triggers attack cleanly
    composeTestRule.onNodeWithTag("board_unit_p_guard_1").performTouchInput {
      val start = center
      down(start)
      moveTo(Offset(start.x - 90f, start.y - 210f))
      up()
    }
    composeTestRule.waitForIdle()
    assertEquals("p_guard_1", draggedBenchAttackUnitId)

    // 3. Expand hand and drag unit card onto wide upper-left battlefield zone -> plays cleanly
    composeTestRule.onNodeWithTag("hand_card_card_wild_mystics").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("hand_card_card_wild_mystics").performTouchInput {
      val start = center
      down(start)
      moveTo(Offset(start.x - 160f, start.y - 180f))
      up()
    }
    composeTestRule.waitForIdle()
    assertEquals("hc_mystics", playedCardId)
  }

  @Test
  fun `aniviaDeckCardsHaveValidPortraitsAndShowClearInfoInHandAndOnBoard`() {
    val aniviaDeckIds = CardCatalog.getChampion("champ_anivia").baseDeck.map { it.cardId }
    assertEquals(9, aniviaDeckIds.size)
    aniviaDeckIds.forEach { cardId ->
      val resId = com.example.ui.components.cardPortraitRes(cardId)
      assertTrue("Expected drawable resource for $cardId", resId != null && resId != 0)
    }

    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val boardAnivia = UnitOnBoard(
      instanceId = "p_anivia_1",
      template = CardCatalog.getCard("card_anivia"),
      ownerIsPlayer = true,
      power = 3,
      health = 5,
      maxHealth = 5,
      keywords = setOf(com.example.model.Keyword.ELEMENTAL)
    )
    val boardProphet = UnitOnBoard(
      instanceId = "p_prophet_1",
      template = CardCatalog.getCard("card_mortal_prophet"),
      ownerIsPlayer = true,
      power = 0,
      health = 5,
      maxHealth = 5,
      keywords = emptySet()
    )
    val mysticsTpl = CardCatalog.getCard("card_wild_mystics")
    val harshWindsTpl = CardCatalog.getCard("card_harsh_winds")
    val handMystics = com.example.model.HandCardInstance(
      instanceId = "hc_mystics",
      template = mysticsTpl,
      effectiveCost = mysticsTpl.cost
    )
    val handHarshWinds = com.example.model.HandCardInstance(
      instanceId = "hc_harsh",
      template = harshWindsTpl,
      effectiveCost = harshWindsTpl.cost
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 6,
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 6,
      playerMaxMana = 6,
      playerSpellMana = 3,
      playerHasAttackToken = true,
      phase = CombatPhase.MAIN_ACTION_PHASE,
      priorityHolder = com.example.engine.PriorityHolder.PLAYER,
      playerBoard = listOf(boardAnivia, boardProphet),
      enemyBoard = emptyList(),
      playerHand = listOf(handMystics, handHarshWinds)
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onPlayTargetedCard = { _, _ -> },
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onDragBenchUnitToAttack = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // On board: clean UnitTokens are displayed; tapping opens UnitInspectOverlay with full name & keywords
    composeTestRule.onNodeWithTag("board_unit_p_anivia_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("board_unit_p_prophet_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("board_unit_p_prophet_1").performClick()
    composeTestRule.waitForIdle()
    assertTrue(composeTestRule.onAllNodesWithText("Tiên Tri Chết Chóc").fetchSemanticsNodes().isNotEmpty())
    composeTestRule.onRoot().performTouchInput { click(Offset(8f, 8f)) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("board_unit_p_anivia_1").performClick()
    composeTestRule.waitForIdle()
    assertTrue(composeTestRule.onAllNodesWithText("Anivia").fetchSemanticsNodes().isNotEmpty())
    assertTrue(
      composeTestRule.onAllNodesWithText(com.example.model.Keyword.ELEMENTAL.displayName)
        .fetchSemanticsNodes()
        .isNotEmpty()
    )
    composeTestRule.onRoot().performTouchInput { click(Offset(8f, 8f)) }
    composeTestRule.waitForIdle()

    // In hand: card names, keywords/spell speed, and ability descriptions are clearly shown when expanded
    composeTestRule.onNodeWithTag("hand_card_card_harsh_winds").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("hand_card_card_wild_mystics").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hand_card_card_harsh_winds").assertIsDisplayed()
    assertTrue(composeTestRule.onAllNodesWithText("Thầy Tu Hoang Dã").fetchSemanticsNodes().isNotEmpty())
    assertTrue(composeTestRule.onAllNodesWithText("Cuồng Phong").fetchSemanticsNodes().isNotEmpty())
  }

  @Test
  fun `lowHeight16x9And20x9BattleBoardKeepsThreeZonesAndUnitTokensUnclipped`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val guardian = CardCatalog.getCard("card_guardian_follower")
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 4,
      playerNexusHp = 35,
      playerMaxHp = 35,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 4,
      playerMaxMana = 4,
      phase = CombatPhase.COMBAT_DECLARE_BLOCK_PHASE,
      priorityHolder = com.example.engine.PriorityHolder.PLAYER,
      playerInitiatedCombat = false,
      enemyBoard = listOf(
        UnitOnBoard("e_atk_1", cithria, false, 3, 3, 3, emptySet()),
        UnitOnBoard("e_bench_1", guardian, false, 4, 5, 5, emptySet())
      ),
      playerBoard = listOf(
        UnitOnBoard("p_blk_1", guardian, true, 4, 5, 5, emptySet()),
        UnitOnBoard("p_bench_1", cithria, true, 2, 2, 2, emptySet())
      ),
      attackingUnitIds = listOf("e_atk_1"),
      blockPairings = mapOf("e_atk_1" to "p_blk_1")
    )

    val viewportSize = androidx.compose.runtime.mutableStateOf(androidx.compose.ui.unit.DpSize(569.dp, 320.dp))
    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(viewportSize.value)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }

    listOf(
      androidx.compose.ui.unit.DpSize(569.dp, 320.dp),
      androidx.compose.ui.unit.DpSize(711.dp, 320.dp)
    ).forEach { size ->
      composeTestRule.runOnUiThread { viewportSize.value = size }
      composeTestRule.waitForIdle()

      val enemyZone = composeTestRule.onNodeWithTag("enemy_bench_zone").getUnclippedBoundsInRoot()
      val fieldZone = composeTestRule.onNodeWithTag("shared_battlefield_zone").getUnclippedBoundsInRoot()
      val playerZone = composeTestRule.onNodeWithTag("player_bench_zone").getUnclippedBoundsInRoot()

      assertTrue("Expected enemy_bench_zone strictly above shared_battlefield_zone at $size", enemyZone.bottom <= fieldZone.top + 1.dp)
      assertTrue("Expected shared_battlefield_zone strictly above player_bench_zone at $size", fieldZone.bottom <= playerZone.top + 1.dp)

      val benchUnit = composeTestRule.onNodeWithTag("board_unit_p_bench_1").getUnclippedBoundsInRoot()
      val enemyBenchUnit = composeTestRule.onNodeWithTag("board_unit_e_bench_1").getUnclippedBoundsInRoot()
      val combatEnemy = composeTestRule.onNodeWithTag("board_unit_e_atk_1").getUnclippedBoundsInRoot()
      val combatBlocker = composeTestRule.onNodeWithTag("board_unit_p_blk_1").getUnclippedBoundsInRoot()

      assertTrue("Bench unit height should stay readable (>= 54.dp) at $size, got ${benchUnit.bottom - benchUnit.top}", (benchUnit.bottom - benchUnit.top) >= 54.dp)
      assertTrue("Enemy bench unit should not clip outside top/bottom at $size", enemyBenchUnit.top >= enemyZone.top - 2.dp && enemyBenchUnit.bottom <= enemyZone.bottom + 2.dp)
      assertTrue("Player bench unit should not clip outside top/bottom at $size", benchUnit.top >= playerZone.top - 2.dp && benchUnit.bottom <= playerZone.bottom + 2.dp)
      assertTrue("Combat pair should sit cleanly inside shared_battlefield_zone at $size", combatEnemy.top >= fieldZone.top - 2.dp && combatBlocker.bottom <= fieldZone.bottom + 2.dp)
    }
  }

  @Test
  fun `compactRightManaActionDialAt40PercentHasNoOverlapOnCancelOrToken`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val playerUnit = UnitOnBoard("p_1", cithria, true, 2, 2, 2, emptySet())
    val base = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 1,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      playerNexusHp = 30,
      playerMaxHp = 30,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 6,
      playerMaxMana = 10,
      enemyMana = 5,
      enemyMaxMana = 10,
      phase = CombatPhase.COMBAT_DECLARE_ATTACK_PHASE,
      playerHasAttackToken = true,
      enemyHasAttackToken = false,
      playerBoard = listOf(playerUnit),
      attackingUnitIds = listOf("p_1")
    )
    val stateHolder = androidx.compose.runtime.mutableStateOf(base)

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 569.dp, height = 320.dp)) {
          com.example.ui.components.RightManaActionDial(
            combat = stateHolder.value,
            dialWidth = 156.dp,
            dialHeight = 132.dp,
            actionDiameter = 52.8.dp,
            descFontSize = androidx.compose.ui.unit.TextUnit(10.5f, androidx.compose.ui.unit.TextUnitType.Sp),
            statFontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
            onEnterAttack = {},
            onCancelAttack = {},
            onCommitAttack = {},
            onCommitBlock = {},
            onPassTurn = {},
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd)
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    fun assertNoOverlap(a: androidx.compose.ui.unit.DpRect, b: androidx.compose.ui.unit.DpRect, msg: String) {
      val overlapsX = a.left < b.right && b.left < a.right
      val overlapsY = a.top < b.bottom && b.top < a.bottom
      assertTrue("$msg (a=$a, b=$b)", !(overlapsX && overlapsY))
    }

    val cancelBounds = composeTestRule.onNodeWithTag("cancel_attack_button").getUnclippedBoundsInRoot()
    val tokenBounds = composeTestRule.onNode(androidx.compose.ui.test.hasText("LƯỢT CÔNG")).getUnclippedBoundsInRoot()
    val enemyManaBounds = composeTestRule.onNodeWithTag("enemy_mana_text").getUnclippedBoundsInRoot()
    val playerManaBounds = composeTestRule.onNodeWithTag("player_mana_text").getUnclippedBoundsInRoot()
    val commitBounds = composeTestRule.onNodeWithTag("commit_attack_button").getUnclippedBoundsInRoot()

    assertTrue("Cancel button must stay inside screen left bound", cancelBounds.left >= 0.dp)
    assertNoOverlap(cancelBounds, tokenBounds, "Cancel X overlaps Attack Token label")
    assertNoOverlap(cancelBounds, enemyManaBounds, "Cancel X overlaps enemy mana")
    assertNoOverlap(cancelBounds, playerManaBounds, "Cancel X overlaps player mana")
    assertNoOverlap(cancelBounds, commitBounds, "Cancel X overlaps commit attack CTA")
    assertNoOverlap(tokenBounds, playerManaBounds, "Attack Token overlaps player mana")
    assertNoOverlap(tokenBounds, commitBounds, "Attack Token overlaps commit attack CTA")

    composeTestRule.runOnUiThread {
      stateHolder.value = base.copy(playerHasAttackToken = false, enemyHasAttackToken = true)
    }
    composeTestRule.waitForIdle()

    val cancelBoundsEnemy = composeTestRule.onNodeWithTag("cancel_attack_button").getUnclippedBoundsInRoot()
    val enemyTokenBounds = composeTestRule.onNode(androidx.compose.ui.test.hasText("ĐỊCH CÔNG")).getUnclippedBoundsInRoot()
    val enemyManaBounds2 = composeTestRule.onNodeWithTag("enemy_mana_text").getUnclippedBoundsInRoot()
    val playerManaBounds2 = composeTestRule.onNodeWithTag("player_mana_text").getUnclippedBoundsInRoot()
    assertNoOverlap(cancelBoundsEnemy, enemyTokenBounds, "Cancel X overlaps Enemy Attack Token")
    assertNoOverlap(cancelBoundsEnemy, playerManaBounds2, "Cancel X overlaps player mana in enemy-token case")
    assertNoOverlap(enemyTokenBounds, enemyManaBounds2, "Enemy Attack Token overlaps enemy mana")
  }

  @Test
  fun `playerHandCollapsedHidesOccludedTextAndHandZoneGuardsPreventAccidentalPlay`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val wildMystics = CardCatalog.getCard("card_wild_mystics")
    val harshWinds = CardCatalog.getCard("card_harsh_winds")
    val hand = listOf(
      com.example.model.HandCardInstance("hc_mystics", wildMystics, effectiveCost = wildMystics.cost),
      com.example.model.HandCardInstance("hc_harsh", harshWinds, effectiveCost = harshWinds.cost)
    )
    val combatState = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 2,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 6,
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 6,
      playerMaxMana = 6,
      playerSpellMana = 3,
      playerHand = hand
    )

    var playedCardId: String? = null
    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState,
            revivesRemaining = 1,
            onPlayCard = { playedCardId = it },
            onPlayTargetedCard = { cardId, _ -> playedCardId = cardId },
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Collapsed: occluded non-last card hides name/description while rightmost card keeps name visible
    assertTrue(composeTestRule.onAllNodesWithText("Thầy Tu Hoang Dã").fetchSemanticsNodes().isEmpty())
    assertTrue(composeTestRule.onAllNodesWithText("Cuồng Phong").fetchSemanticsNodes().isNotEmpty())

    // Expand hand and perform small horizontal slip inside the upper half of expanded card -> must NOT play
    composeTestRule.onNodeWithTag("hand_card_card_harsh_winds").performClick()
    composeTestRule.waitForIdle()
    assertTrue(composeTestRule.onAllNodesWithText("Thầy Tu Hoang Dã").fetchSemanticsNodes().isNotEmpty())

    composeTestRule.onNodeWithTag("hand_card_card_wild_mystics").performTouchInput {
      val topHalf = Offset(center.x, height * 0.25f)
      down(topHalf)
      moveTo(Offset(topHalf.x + 22f, topHalf.y))
      up()
    }
    composeTestRule.waitForIdle()
    assertEquals(null, playedCardId)

    // Dragging clearly upward into battlefield (>85% threshold) -> plays cleanly
    composeTestRule.onNodeWithTag("hand_card_card_wild_mystics").performTouchInput {
      val start = center
      down(start)
      moveTo(Offset(start.x - 100f, start.y - 180f))
      up()
    }
    composeTestRule.waitForIdle()
    assertEquals("hc_mystics", playedCardId)
  }

  @Test
  fun `combatScreen rightManaActionDial aligns with background orb and never overlaps or clips on 16_9 and 20_9`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val cithria = CardCatalog.getCard("card_cithria")
    val mysticShot = CardCatalog.getCard("card_mystic_shot")
    val state = CombatState(
      encounterNode = node,
      adventure = adv,
      championId = "champ_anivia",
      starLevel = 6,
      equippedRelics = emptyList(),
      activePowers = emptyList(),
      mutators = emptyList(),
      round = 1,
      playerNexusHp = 40,
      playerMaxHp = 40,
      enemyNexusHp = 20,
      enemyMaxHp = 20,
      playerMana = 3,
      playerMaxMana = 3,
      playerSpellMana = 1,
      enemyMana = 1,
      enemyMaxMana = 1,
      enemySpellMana = 0,
      playerHand = listOf(
        com.example.model.HandCardInstance("hc_1", cithria, effectiveCost = cithria.cost),
        com.example.model.HandCardInstance("hc_2", mysticShot, effectiveCost = mysticShot.cost)
      )
    )

    val viewWidth = androidx.compose.runtime.mutableStateOf(569.dp)
    val viewHeight = androidx.compose.runtime.mutableStateOf(320.dp)

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = viewWidth.value, height = viewHeight.value)) {
          com.example.ui.screens.CombatScreen(
            combat = state,
            revivesRemaining = 1,
            onPlayCard = {},
            onPlayTargetedCard = { _, _ -> },
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }

    fun assertNoOverlap(a: androidx.compose.ui.unit.DpRect, b: androidx.compose.ui.unit.DpRect, msg: String) {
      val overlapsX = a.left < b.right && b.left < a.right
      val overlapsY = a.top < b.bottom && b.top < a.bottom
      assertTrue("$msg (a=$a, b=$b)", !(overlapsX && overlapsY))
    }

    listOf(569.dp to 320.dp, 850.dp to 390.dp).forEach { (w, h) ->
      composeTestRule.runOnUiThread {
        viewWidth.value = w
        viewHeight.value = h
      }
      composeTestRule.waitForIdle()

      val ctaBounds = composeTestRule.onNodeWithTag("end_round_button").getUnclippedBoundsInRoot()
      val enemyManaBounds = composeTestRule.onNodeWithTag("enemy_mana_text").getUnclippedBoundsInRoot()
      val playerManaBounds = composeTestRule.onNodeWithTag("player_mana_text").getUnclippedBoundsInRoot()
      val spellBadgeBounds = composeTestRule.onNodeWithTag("player_spell_mana_badge").getUnclippedBoundsInRoot()
      val handCardBounds = composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").getUnclippedBoundsInRoot()

      val scaleDp = maxOf(w.value / 1076f, h.value / 614f)
      val offsetX = (w - (1076f * scaleDp).dp) / 2f
      val expectedOrbCenterX = offsetX + (927f * scaleDp).dp
      val actualCtaCenterX = (ctaBounds.left + ctaBounds.right) / 2f
      val centerDiff = kotlin.math.abs((actualCtaCenterX - expectedOrbCenterX).value)
      assertTrue("CTA center ($actualCtaCenterX) should align with painted orb ($expectedOrbCenterX) on ${w}x${h}", centerDiff <= 4f)

      assertTrue("enemyManaBounds clipped on ${w}x${h}: $enemyManaBounds", enemyManaBounds.top >= 0.dp && enemyManaBounds.right <= w)
      assertTrue("spellBadgeBounds clipped on ${w}x${h}: $spellBadgeBounds", spellBadgeBounds.bottom <= handCardBounds.top)
      assertNoOverlap(ctaBounds, enemyManaBounds, "CTA overlaps enemy mana on ${w}x${h}")
      assertNoOverlap(ctaBounds, playerManaBounds, "CTA overlaps player mana on ${w}x${h}")
      assertNoOverlap(ctaBounds, spellBadgeBounds, "CTA overlaps player spell mana badge on ${w}x${h}")
    }
  }

  @Test
  fun `manaUi displays up to 12 regular gems milestone bonus and spell vs unit cost previews cleanly`() {
    val adv = AdventureCatalog.getAdventure("adv_demacia_outskirts")
    val node = adv.mapNodes.first { it.id == "d_fight_1a" }
    val spell4 = com.example.model.HandCardInstance(
      instanceId = "hc_spell4",
      template = CardCatalog.getCard("card_mystic_shot"),
      effectiveCost = 4
    )
    val unit4 = com.example.model.HandCardInstance(
      instanceId = "hc_unit4",
      template = CardCatalog.getCard("card_cithria"),
      effectiveCost = 4
    )
    val combatState = androidx.compose.runtime.mutableStateOf(
      CombatState(
        encounterNode = node,
        adventure = adv,
        championId = "champ_anivia",
        starLevel = 6,
        equippedRelics = emptyList(),
        activePowers = emptyList(),
        mutators = emptyList(),
        round = 10,
        playerNexusHp = 40,
        playerMaxHp = 40,
        enemyNexusHp = 25,
        enemyMaxHp = 25,
        playerMana = 11,
        playerMaxMana = 12,
        playerSpellMana = 2,
        enemyMana = 10,
        enemyMaxMana = 10,
        enemySpellMana = 1,
        playerHand = listOf(spell4, unit4),
        phase = CombatPhase.MAIN_ACTION_PHASE,
        priorityHolder = PriorityHolder.PLAYER
      )
    )

    composeTestRule.setContent {
      PathOfChampionsTheme {
        Box(modifier = Modifier.size(width = 850.dp, height = 390.dp)) {
          com.example.ui.screens.CombatScreen(
            combat = combatState.value,
            revivesRemaining = 1,
            onPlayCard = {},
            onEnterAttack = {},
            onCancelAttack = {},
            onToggleAttacker = {},
            onCommitAttack = {},
            onSelectEnemyAttackerForBlock = {},
            onAssignBlocker = { _, _ -> },
            onRemoveBlocker = {},
            onCommitBlock = {},
            onResolveStack = {},
            onPassTurn = {},
            onVictoryContinue = {},
            onUseRevive = {},
            onAcceptDefeat = {},
            onAbandonRun = {}
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. Verify 12-slot regular mana text, milestone bonus badge, and all 12 gems are displayed unclipped
    composeTestRule.onNodeWithTag("player_mana_text").assertTextEquals("11/12")
    composeTestRule.onNodeWithTag("player_mana_turn_meta").assertIsDisplayed().assertTextContains("★+2", substring = true)
    (0 until 12).forEach { idx ->
      composeTestRule.onNodeWithTag("player_regular_mana_gem_$idx").assertIsDisplayed()
    }
    composeTestRule.onNodeWithTag("player_spell_mana_text").assertTextEquals("2")

    // 2. Tap mana plaque to inspect full breakdown dialog
    composeTestRule.onNodeWithTag("player_mana_plaque").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mana_detail_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Đóng").performClick()
    composeTestRule.waitForIdle()

    // 3. Switch to 3 Regular Mana & 2 Spell Mana -> test Spell 4-cost affordable vs Unit 4-cost unaffordable
    composeTestRule.runOnUiThread {
      combatState.value = combatState.value.copy(playerMana = 3, playerMaxMana = 4, playerSpellMana = 2)
    }
    composeTestRule.waitForIdle()

    // Expand hand by clicking collapsed card
    composeTestRule.onNodeWithTag("hand_card_card_cithria").performClick()
    composeTestRule.waitForIdle()

    // Tap 4-cost Spell -> uses 2 Spell + 2 Regular -> 1 Regular remains, play button ENABLED
    composeTestRule.onNodeWithTag("hand_card_card_mystic_shot").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_mana_breakdown_text")
      .assertTextContains("2 NL Phép + 2 NL Thường", substring = true)
      .assertTextContains("Còn 1 NL Thường", substring = true)
    composeTestRule.onNodeWithTag("confirm_play_card_button").assertIsEnabled()

    // Dismiss inspect overlay and tap 4-cost Unit -> cannot use Spell Mana, play button DISABLED
    composeTestRule.onNodeWithText("Đóng").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("hand_card_card_cithria").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_mana_breakdown_text")
      .assertTextContains("Không đủ NL Thường", substring = true)
    composeTestRule.onNodeWithTag("confirm_play_card_button").assertIsNotEnabled()
  }
}
