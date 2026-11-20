"""Opening hook: one punchy line at a time, popping in over a colour glow."""

import config
import graphics as g

POP_SECONDS = 0.22
EXIT_SECONDS = 0.18


def hook_frames():
    for index in range(round(config.HOOK_DURATION * config.FPS)):
        yield hook_frame(index / config.FPS)


def hook_frame(t):
    canvas = g.background().copy().convert("RGBA")
    line = _current_line(t)
    progress = (t - line.start) / POP_SECONDS
    opacity = g.clamp01(progress) * _exit_opacity(t)
    _draw_glow(canvas, line, opacity)
    text = g.fitted_text_image(line.text, line.size, line.color)
    scale = 1.35 - 0.35 * g.ease_out_back(progress)
    g.paste_centered(canvas, g.with_opacity(g.scaled(text, scale), opacity), _center())
    return canvas.convert("RGB")


def _current_line(t):
    started = [line for line in config.HOOK_LINES if line.start <= t]
    return started[-1]


def _exit_opacity(t):
    return g.clamp01((config.HOOK_DURATION - t) / EXIT_SECONDS)


def _draw_glow(canvas, line, opacity):
    halo = g.glow(line.color, 1300)
    g.paste_centered(canvas, g.with_opacity(halo, 0.35 * opacity), _center())


def _center():
    return config.WIDTH / 2, config.HEIGHT / 2
