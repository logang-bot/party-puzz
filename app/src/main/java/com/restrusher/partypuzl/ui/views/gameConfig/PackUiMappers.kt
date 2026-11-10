package com.restrusher.partypuzl.ui.views.gameConfig

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.appData.appModels.QuestionPackDefinition
import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.packAccent
import com.restrusher.partypuzl.ui.common.iconRes
import com.restrusher.partypuzl.ui.views.game.gameScreen.MiniGame

/** What decides whether a premium pack is open: owning premium, or a rewarded-ad unlock. */
internal data class PremiumAccess(val sessionUnlocks: Set<String>, val isAdFree: Boolean)

/**
 * Built-in packs only, in catalog order. A DB row with no catalog entry is either a pack a later
 * release retired or a custom pack, so this pass skips it — custom rows are mapped by
 * [CustomPackSummary.toUiModel].
 */
internal fun List<QuestionPackEntity>.toCatalogUiModels(
    access: PremiumAccess,
    promptCounts: Map<String, Int>
): List<PackUiModel> {
    val byId = associateBy { it.id }
    return QuestionPackCatalog.all.mapNotNull { definition ->
        byId[definition.id]?.let { CatalogPack(definition, it).toUiModel(access, promptCounts) }
    }
}

/**
 * Always unlocked, whatever the user owns — they wrote it. The count comes from the live summary
 * rather than the one-shot prompt counts, so it refreshes when they come back from adding one.
 */
internal fun CustomPackSummary.toUiModel() = PackUiModel(
    id = packId,
    tier = PackTier.CUSTOM,
    name = PackLabel.Literal(name),
    iconRes = spice.iconRes,
    accent = spice.packAccent,
    promptCount = entryCount,
    isEnabled = isEnabled,
    isUnlocked = true
)

private data class CatalogPack(
    val definition: QuestionPackDefinition,
    val entity: QuestionPackEntity
)

/** A locked pack can never read as enabled, whatever the stored flag says. */
private fun CatalogPack.toUiModel(access: PremiumAccess, promptCounts: Map<String, Int>) =
    PackUiModel(
        id = definition.id,
        tier = definition.tier,
        name = PackLabel.Resource(definition.nameRes),
        iconRes = definition.spice.iconRes,
        accent = definition.spice.packAccent,
        promptCount = definition.promptCount(promptCounts),
        isEnabled = entity.isEnabled && isUnlocked(access),
        isUnlocked = isUnlocked(access),
        isSessionUnlocked = isOnlySessionUnlocked(access)
    )

private fun CatalogPack.isUnlocked(access: PremiumAccess): Boolean =
    definition.tier != PackTier.PREMIUM || entity.isUnlocked || access.isAdFree ||
        definition.id in access.sessionUnlocks

private fun CatalogPack.isOnlySessionUnlocked(access: PremiumAccess): Boolean =
    definition.id in access.sessionUnlocks && !entity.isUnlocked && !access.isAdFree

/**
 * Mini-games carry no question rows — they are code, not prompts — so that pack reports how many
 * mini-games ship instead of a misleading zero.
 */
private fun QuestionPackDefinition.promptCount(promptCounts: Map<String, Int>): Int =
    if (category == PackCategory.MINI_GAME) MiniGame.entries.size
    else promptCounts[id] ?: 0
