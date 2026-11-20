"""Which recording each language uses, and the clips cut from it (timings in seconds)."""

from dataclasses import dataclass

from palette import CORAL, LIME, PINK, TEAL_SOFT, VIOLET, YELLOW


@dataclass(frozen=True)
class Clip:
    source_start: float
    duration: float
    caption: str
    accent: tuple
    zoom: float = 1.0
    focus_y: float = 0.5
    boom_at: float = None


@dataclass(frozen=True)
class Cut:
    recording: str
    clips: tuple


CUTS = {
    "en": Cut("Screen_recording_20261119_214206.webm", (
        Clip(3.35, 1.8, "Wild dares", PINK, zoom=1.1, focus_y=0.4),
        Clip(10.4, 1.2, "Surprise mini-games", YELLOW, zoom=1.1, focus_y=0.4),
        Clip(66.5, 2.0, "Tap wars", CORAL),
        Clip(91.3, 1.6, "Draw the perfect circle", TEAL_SOFT),
        Clip(94.4, 1.2, "Loser drinks!", LIME, zoom=1.1, focus_y=0.45),
    )),
    "es": Cut("Screen_recording_20261119_223047.webm", (
        Clip(2.85, 2.1, "Retos locos", PINK, zoom=1.1, focus_y=0.4),
        Clip(11.0, 1.2, "¿Verdad o reto?", VIOLET, zoom=1.1, focus_y=0.4),
        Clip(63.0, 2.0, "Minijuegos sorpresa", YELLOW),
        Clip(106.9, 2.3, "¡Papa caliente!", CORAL, boom_at=1.35),
        Clip(110.4, 1.2, "¡El que pierde, bebe!", LIME, zoom=1.1, focus_y=0.45),
    )),
}
