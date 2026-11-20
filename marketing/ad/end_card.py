"""Closing card: app icon, PartyPuzl wordmark, tagline and the Google Play badge."""

from functools import lru_cache

from PIL import Image

import config
import graphics as g

CROSSFADE_SECONDS = 0.3
ICON_SIZE = 300
LOGO_SCALE = 1.9
ICON_DELAY = 0.1
BADGE_DELAY = 0.65


def end_card_frames(previous_frame):
    for index in range(round(config.END_CARD_DURATION * config.FPS)):
        t = index / config.FPS
        card = end_card_frame(t)
        yield Image.blend(previous_frame, card, g.clamp01(t / CROSSFADE_SECONDS))


def end_card_frame(t):
    canvas = g.background().copy().convert("RGBA")
    g.paste_centered(canvas, g.with_opacity(g.glow(config.TEAL, 1500), 0.3), (540, 820))
    _pop(canvas, _icon(), (t - ICON_DELAY, 470))
    _rise(canvas, _logo(), (t - 0.3, 820))
    _rise(canvas, g.fitted_text_image(config.END_TAGLINE, 74, config.INK), (t - 0.45, 1080))
    _pop(canvas, _badge(config.BADGE), (t - BADGE_DELAY, 1300))
    return canvas.convert("RGB")


def _pop(canvas, image, timing):
    local_t, center_y = timing
    progress = local_t / 0.4
    popped = g.scaled(image, max(0.01, g.ease_out_back(progress)))
    g.paste_centered(canvas, g.with_opacity(popped, progress * 3), (config.WIDTH / 2, center_y))


def _rise(canvas, image, timing):
    local_t, center_y = timing
    progress = g.ease_out_cubic(local_t / 0.4)
    center = (config.WIDTH / 2, center_y + (1 - progress) * 60)
    g.paste_centered(canvas, g.with_opacity(image, progress), center)


@lru_cache(maxsize=1)
def _icon():
    background = Image.open(config.ICON_BACKGROUND).convert("RGBA")
    background.alpha_composite(Image.open(config.ICON_FOREGROUND).convert("RGBA"))
    icon = background.resize((ICON_SIZE, ICON_SIZE), Image.LANCZOS)
    icon.putalpha(g.rounded_mask(icon.size, ICON_SIZE * 22 // 100))
    return icon


@lru_cache(maxsize=1)
def _logo():
    return g.scaled(Image.open(config.LOGO).convert("RGBA"), LOGO_SCALE)


@lru_cache(maxsize=4)
def _badge(path):
    return Image.open(path).convert("RGBA")
