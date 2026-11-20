"""Middle section: recording clips inside a phone frame with a caption above."""

from functools import lru_cache

from PIL import Image

import config
import graphics as g
from recording_frames import clip_frames

SLIDE_IN_SECONDS = 0.45
FLASH_SECONDS = 0.12
CAPTION_POP_SECONDS = 0.25
SCREEN_RADIUS = 52


def phone_frames():
    for clip_index, clip in enumerate(config.CLIPS):
        for frame_index, screen in enumerate(clip_frames(clip)):
            beat = (clip_index, frame_index / config.FPS)
            yield clip_index, phone_frame(clip, screen, beat)


def phone_frame(clip, screen, beat):
    clip_index, t = beat
    canvas = g.background().copy().convert("RGBA")
    slide = 0.0 if clip_index else 1 - g.ease_out_cubic(t / SLIDE_IN_SECONDS)
    offset_y = round(slide * config.HEIGHT * 0.7)
    canvas.alpha_composite(_phone(screen, t), (_phone_x(), config.PHONE_TOP + offset_y))
    _draw_caption(canvas, clip, t)
    return canvas.convert("RGB")


def _phone(screen, t):
    phone = _bezel().copy()
    flash = 1 - g.clamp01(t / FLASH_SECONDS)
    lit = Image.blend(screen, Image.new("RGB", screen.size, config.INK), 0.3 * flash)
    phone.paste(lit, (config.BEZEL_PAD, config.BEZEL_PAD), _screen_mask())
    return phone


@lru_cache(maxsize=1)
def _bezel():
    size = (config.SCREEN_W + config.BEZEL_PAD * 2, config.SCREEN_H + config.BEZEL_PAD * 2)
    radius = SCREEN_RADIUS + config.BEZEL_PAD
    phone = Image.new("RGBA", size)
    phone.paste(Image.new("RGBA", size, config.TEAL + (110,)), (0, 0), g.rounded_mask(size, radius))
    inner = (size[0] - 6, size[1] - 6)
    phone.paste(Image.new("RGBA", inner, config.BEZEL + (255,)), (3, 3), g.rounded_mask(inner, radius - 3))
    return phone


@lru_cache(maxsize=1)
def _screen_mask():
    return g.rounded_mask((config.SCREEN_W, config.SCREEN_H), SCREEN_RADIUS)


def _phone_x():
    return (config.WIDTH - config.SCREEN_W) // 2 - config.BEZEL_PAD


def _draw_caption(canvas, clip, t):
    progress = t / CAPTION_POP_SECONDS
    text = g.scaled(g.fitted_text_image(clip.caption, 96, clip.accent), 1.25 - 0.25 * g.ease_out_back(progress))
    center = (config.WIDTH / 2, config.CAPTION_CENTER_Y)
    g.paste_centered(canvas, g.with_opacity(text, g.clamp01(progress * 1.5)), center)
