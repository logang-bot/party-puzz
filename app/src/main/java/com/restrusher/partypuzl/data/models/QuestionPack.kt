package com.restrusher.partypuzl.data.models

/**
 * Tier decides how a pack is presented and whether it needs unlocking.
 *
 * - [OFFICIAL] — curated by PartyPuzz, free, always available.
 * - [PREMIUM] — curated by PartyPuzz, needs a rewarded ad (session) or the purchase (permanent).
 * - [CUSTOM] — written by the user. No authoring flow ships yet; the tier exists so the
 *   catalog and the Prepare screen already have a place for them.
 */
enum class PackTier { OFFICIAL, PREMIUM, CUSTOM }

/**
 * Which deal a pack feeds. Mirrors `GameDealType` in the game screen, but lives in the data
 * layer so entities and the catalog don't have to reach up into the UI package.
 */
enum class PackCategory { TRUTH_OR_DARE, GENERAL_KNOWLEDGE, STICKY_DARE, MINI_GAME }
