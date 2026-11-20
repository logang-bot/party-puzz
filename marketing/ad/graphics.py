"""Reusable drawing primitives: easing, text, gradients, masks and pasting."""

from functools import lru_cache

from PIL import Image, ImageDraw, ImageFilter, ImageFont

import config


def clamp01(value):
    return max(0.0, min(1.0, value))


def ease_out_cubic(value):
    return 1 - (1 - clamp01(value)) ** 3


def ease_out_back(value):
    """Overshoots by ~10% before settling, using the standard c1 = 1.70158 curve."""
    x, c1 = clamp01(value), 1.70158
    return 1 + (c1 + 1) * (x - 1) ** 3 + c1 * (x - 1) ** 2


@lru_cache(maxsize=None)
def font(size):
    return ImageFont.truetype(str(config.FONT), size)


@lru_cache(maxsize=64)
def text_image(text, size, color):
    stroke = max(2, size // 40)
    probe = ImageDraw.Draw(Image.new("RGBA", (1, 1)))
    box = probe.multiline_textbbox((0, 0), text, font=font(size), align="center",
                                   stroke_width=stroke, spacing=size // 8)
    box, pad = [round(edge) for edge in box], size // 3
    image = Image.new("RGBA", (box[2] - box[0] + pad * 2, box[3] - box[1] + pad * 2))
    _draw_text_with_shadow(image, text, (size, color, stroke, pad - box[0], pad - box[1]))
    return image


def fitted_text_image(text, size, color):
    image = text_image(text, size, color)
    shadow_pad = 2 * (size // 3)
    return scaled(image, min(1.0, config.MAX_TEXT_WIDTH / (image.width - shadow_pad)))


def _draw_text_with_shadow(image, text, style):
    size, color, stroke, x, y = style
    shadow = Image.new("RGBA", image.size)
    kwargs = dict(font=font(size), align="center", stroke_width=stroke, spacing=size // 8)
    ImageDraw.Draw(shadow).multiline_text((x, y + size // 20), text, fill=(0, 0, 0, 170),
                                          stroke_fill=(0, 0, 0, 170), **kwargs)
    image.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(size // 18)))
    ImageDraw.Draw(image).multiline_text((x, y), text, fill=color, stroke_fill=color, **kwargs)


@lru_cache(maxsize=1)
def background():
    image = Image.new("RGB", (config.WIDTH, config.HEIGHT))
    draw = ImageDraw.Draw(image)
    for y in range(config.HEIGHT):
        draw.line([(0, y), (config.WIDTH, y)], fill=_gradient_color(y / config.HEIGHT))
    return image


def _gradient_color(position):
    if position < 0.5:
        return _mix(config.BG_TOP, config.BG_MID, position * 2)
    return _mix(config.BG_MID, config.BG_END, (position - 0.5) * 2)


def _mix(start, end, amount):
    return tuple(round(a + (b - a) * amount) for a, b in zip(start, end))


@lru_cache(maxsize=16)
def glow(color, diameter):
    mask = Image.new("L", (diameter, diameter))
    inset = diameter // 4
    ImageDraw.Draw(mask).ellipse((inset, inset, diameter - inset, diameter - inset), fill=255)
    layer = Image.new("RGBA", (diameter, diameter), color + (0,))
    layer.putalpha(mask.filter(ImageFilter.GaussianBlur(diameter // 8)))
    return layer


def rounded_mask(size, radius):
    mask = Image.new("L", size)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius, fill=255)
    return mask


def with_opacity(image, opacity):
    if opacity >= 1:
        return image
    faded = image.copy()
    faded.putalpha(faded.getchannel("A").point(lambda a: round(a * clamp01(opacity))))
    return faded


def scaled(image, scale):
    if abs(scale - 1) < 0.002:
        return image
    size = (max(1, round(image.width * scale)), max(1, round(image.height * scale)))
    return image.resize(size, Image.LANCZOS)


def paste_centered(canvas, image, center):
    x = round(center[0] - image.width / 2)
    y = round(center[1] - image.height / 2)
    canvas.paste(image, (x, y), image)
