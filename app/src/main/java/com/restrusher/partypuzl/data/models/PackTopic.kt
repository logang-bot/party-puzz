package com.restrusher.partypuzl.data.models

/**
 * What a user-written pack is *about* — the shelf it sits on, not the deal it feeds.
 *
 * Deliberately not [PackCategory]: a pack is a themed collection, and the content loader pools by
 * each *entry's* type, so one pack may hold truths, dares, sticky dares and trivia at once.
 * Labelling it "Truth or Dare" would promise a filter that does not exist. The topic is only ever
 * read to draw a chip and to help the author find the pack again.
 */
enum class PackTopic {
    FRIENDS_INSIDE_JOKES,
    BIG_NIGHT_OUT,
    FOOD_AND_DRINK,
    COUPLES,
    FAMILY,
    WORK_CROWD,
    POP_CULTURE,
    TRAVEL
}
