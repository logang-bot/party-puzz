package com.restrusher.partypuzl.domain.customPacks

/**
 * How much custom content the user may still add. Free users get [MAX_FREE_PACKS] packs of up to
 * [MAX_FREE_ENTRIES] entries each; premium removes both caps.
 *
 * The caps only block adding. Content a free user already has above them stays playable and
 * editable.
 */
data class CustomPackQuota(val isPremium: Boolean, val packCount: Int) {

    val canCreatePack: Boolean get() = isPremium || packCount < MAX_FREE_PACKS

    fun canAddEntry(entryCount: Int): Boolean = isPremium || entryCount < MAX_FREE_ENTRIES

    companion object {
        const val MAX_FREE_PACKS = 1
        const val MAX_FREE_ENTRIES = 5
    }
}
