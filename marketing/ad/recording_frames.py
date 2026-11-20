"""Streams cropped, scaled frames of one clip out of the screen recording."""

import subprocess

from PIL import Image

import config


def clip_frames(clip):
    frame_bytes = config.SCREEN_W * config.SCREEN_H * 3
    with subprocess.Popen(_decode_command(clip), stdout=subprocess.PIPE) as process:
        while len(chunk := process.stdout.read(frame_bytes)) == frame_bytes:
            yield Image.frombytes("RGB", (config.SCREEN_W, config.SCREEN_H), chunk)


def _decode_command(clip):
    filters = f"{_crop_filter(clip)},scale={config.SCREEN_W}:{config.SCREEN_H}:flags=lanczos"
    return ["ffmpeg", "-v", "error", "-ss", str(clip.source_start), "-t", str(clip.duration),
            "-i", str(config.RECORDING), "-vf", f"{filters},fps={config.FPS}",
            "-f", "rawvideo", "-pix_fmt", "rgb24", "-"]


def _crop_filter(clip):
    width = round(1080 / clip.zoom)
    height = round(config.SCREEN_H * width / config.SCREEN_W)
    top = config.SOURCE_CROP_TOP + round(config.SOURCE_CROP_HEIGHT * clip.focus_y - height / 2)
    top = max(config.SOURCE_CROP_TOP, min(top, config.SOURCE_CROP_TOP + config.SOURCE_CROP_HEIGHT - height))
    return f"crop={width}:{height}:{(1080 - width) // 2}:{top}"
