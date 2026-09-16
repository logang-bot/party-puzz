package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.annotation.StringRes
import com.restrusher.partypuzl.R

/**
 * Reward / punishment lines that carry no mechanics, one list per mode and category.
 *
 * Each list is the source of truth for its deck's length, so a line is added in one place.
 */
@StringRes
internal val barRewardFlavours: List<Int> = listOf(
    R.string.bar_reward_skip_next_drink,
    R.string.bar_reward_steal_a_sip,
    R.string.bar_reward_new_house_rule,
    R.string.bar_reward_crown_yourself,
    R.string.bar_reward_send_for_a_round,
    R.string.bar_reward_swap_drinks
)

@StringRes
internal val barPunishmentFlavours: List<Int> = listOf(
    R.string.bar_punishment_tequila_shot,
    R.string.bar_punishment_finish_your_drink,
    R.string.bar_punishment_group_picks_shot,
    R.string.bar_punishment_table_mixed_shot,
    R.string.bar_punishment_shame_sash,
    R.string.bar_punishment_five_second_chug,
    R.string.bar_punishment_wrong_hand_shot
)

@StringRes
internal val couplesRewardFlavours: List<Int> = listOf(
    R.string.couples_reward_whisper_something_sweet,
    R.string.couples_reward_cuddle_break,
    R.string.couples_reward_feed_each_other,
    R.string.couples_reward_slow_dance,
    R.string.couples_reward_pick_the_song,
    R.string.couples_reward_shoulder_massage
)

@StringRes
internal val couplesPunishmentFlavours: List<Int> = listOf(
    R.string.couples_punishment_kiss_from_the_left,
    R.string.couples_punishment_worst_pickup_line,
    R.string.couples_punishment_last_person_texted,
    R.string.couples_punishment_partner_posts_status,
    R.string.couples_punishment_last_selfie,
    R.string.couples_punishment_most_used_emoji,
    R.string.couples_punishment_partner_impression
)
