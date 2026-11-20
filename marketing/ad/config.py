import os
from dataclasses import dataclass, replace
from pathlib import Path

from ad_copy import COPY
from cuts import CUTS
from palette import BEZEL, BG_END, BG_MID, BG_TOP, INK, PINK, TEAL, TEAL_SOFT, YELLOW

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
RES = ROOT / "app/src/main/res"

FONT = RES / "font/abel_regular.ttf"
LOGO = RES / "drawable/img_partypuzl_logo.png"
ICON_BACKGROUND = RES / "mipmap-xxxhdpi/ic_launcher_background.webp"
ICON_FOREGROUND = RES / "mipmap-xxxhdpi/ic_launcher_foreground.webp"

WIDTH, HEIGHT, FPS = 1080, 1920, 30

SOURCE_CROP_TOP, SOURCE_CROP_HEIGHT = 110, 2220
BEZEL_PAD = 22
PHONE_TOP = 340
CAPTION_CENTER_Y = 200
SCREEN_W = 840
SCREEN_H = HEIGHT - PHONE_TOP - BEZEL_PAD + 80


@dataclass(frozen=True)
class HookLine:
    text: str
    color: tuple
    start: float
    size: int


HOOK_STYLES = (
    HookLine("", INK, 0.0, 230),
    HookLine("", PINK, 0.9, 210),
    HookLine("", YELLOW, 1.8, 200),
    HookLine("", TEAL_SOFT, 2.7, 200),
)
HOOK_DURATION = 3.6
MAX_TEXT_WIDTH = 980
END_CARD_DURATION = 3.2

HOOK_LINES = CLIPS = END_TAGLINE = BADGE = OUTPUT = RECORDING = None


def apply_language(language):
    global HOOK_LINES, CLIPS, END_TAGLINE, BADGE, OUTPUT, RECORDING
    copy, cut = COPY[language], CUTS[language]
    HOOK_LINES = tuple(replace(style, text=text) for style, text in zip(HOOK_STYLES, copy.hook))
    CLIPS, END_TAGLINE = cut.clips, copy.tagline
    RECORDING = Path(os.environ.get(f"AD_RECORDING_{language.upper()}", ROOT / cut.recording))
    BADGE = HERE / f"assets/google_play_badge_{language}.png"
    OUTPUT = HERE / "out" / f"partypuzl_ad_{language}.mp4"
