package com.restrusher.partypuzl.screenshots

import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn

data class DemoPlayer(
    val nickName: String,
    val gender: Gender,
    val interestedIn: InterestedIn,
    val avatarName: String
)

data class DemoEntry(
    val type: CustomEntryType,
    val text: String,
    val optionA: String? = null,
    val optionB: String? = null
)

data class DemoPack(
    val name: String,
    val description: String,
    val entries: List<DemoEntry>
)

data class DemoContent(
    val partyName: String,
    val players: List<DemoPlayer>,
    val pack: DemoPack
) {
    companion object {
        fun forLanguage(languageTag: String): DemoContent =
            if (languageTag.startsWith("es")) Spanish else English

        private val Players = listOf(
            DemoPlayer("Alex", Gender.Male, InterestedIn.Woman, "img_dummy_avatar_1"),
            DemoPlayer("Maya", Gender.Female, InterestedIn.Man, "img_dummy_avatar_2"),
            DemoPlayer("Leo", Gender.Male, InterestedIn.Both, "img_dummy_avatar_3"),
            DemoPlayer("Nina", Gender.Female, InterestedIn.Both, "img_dummy_avatar_4"),
            DemoPlayer("Sam", Gender.Male, InterestedIn.Woman, "img_dummy_avatar_5")
        )

        private val English = DemoContent(
            partyName = "Friday game night",
            players = Players,
            pack = DemoPack(
                name = "Road trip",
                description = "Questions and dares for the long drive.",
                entries = listOf(
                    DemoEntry(CustomEntryType.TRUTH, "What's the worst song you secretly love?"),
                    DemoEntry(CustomEntryType.DARE, "Narrate the next minute like a nature documentary."),
                    DemoEntry(CustomEntryType.TRIVIA, "Which planet is closer to the Sun?", "Venus", "Mars")
                )
            )
        )

        private val Spanish = DemoContent(
            partyName = "Noche de juegos",
            players = Players,
            pack = DemoPack(
                name = "Viaje por carretera",
                description = "Preguntas y retos para el camino.",
                entries = listOf(
                    DemoEntry(CustomEntryType.TRUTH, "¿Qué canción te encanta en secreto?"),
                    DemoEntry(CustomEntryType.DARE, "Narra el próximo minuto como un documental de naturaleza."),
                    DemoEntry(CustomEntryType.TRIVIA, "¿Qué planeta está más cerca del Sol?", "Venus", "Marte")
                )
            )
        )
    }
}
