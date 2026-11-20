"""Renders the vertical PartyPuzl ad to MP4s.

Run: python3 marketing/ad/render.py [en] [es]   (no arguments renders every language)
"""

import subprocess
import sys

import config
from ad_copy import COPY
from end_card import end_card_frames
from hook_scene import hook_frames
from phone_scene import phone_frames
from soundtrack import Timeline, write_soundtrack

LOUDNESS_FILTER = ("acompressor=threshold=-20dB:ratio=4:attack=3:release=120:makeup=2,"
                   "alimiter=limit=0.9:level=false,loudnorm=I=-14:TP=-1.5:LRA=11")


def main(languages):
    for language in languages or COPY:
        config.apply_language(language)
        if not config.RECORDING.is_file():
            sys.exit(f"Recording not found: {config.RECORDING}\n"
                     f"Set AD_RECORDING_{language.upper()}=/path/to/recording")
        render_ad()
    return 0


def render_ad():
    config.OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    video, audio = config.OUTPUT.with_suffix(".video.mp4"), config.OUTPUT.with_suffix(".wav")
    timeline = _encode_video(video)
    write_soundtrack(timeline, audio)
    partial = config.OUTPUT.with_suffix(".partial.mp4")
    _run(_mux_command(video, audio, partial))
    partial.replace(config.OUTPUT)
    video.unlink()
    audio.unlink()
    print(f"Wrote {config.OUTPUT}")


def _encode_video(destination):
    timeline = Timeline()
    with subprocess.Popen(_encode_command(destination), stdin=subprocess.PIPE) as encoder:
        for frame in _all_frames(timeline):
            encoder.stdin.write(frame.tobytes())
            timeline.frames += 1
        encoder.stdin.close()
    if encoder.returncode:
        sys.exit(f"ffmpeg failed while encoding {destination}")
    return timeline


def _all_frames(timeline):
    yield from hook_frames()
    for clip_index, last in phone_frames():
        if clip_index == len(timeline.clip_starts):
            timeline.clip_starts.append(timeline.seconds())
        yield last
    timeline.end_card_start = timeline.seconds()
    yield from end_card_frames(last)


def _encode_command(destination):
    size = f"{config.WIDTH}x{config.HEIGHT}"
    return ["ffmpeg", "-v", "error", "-y",
            "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", size, "-r", str(config.FPS), "-i", "-",
            "-c:v", "libx264", "-preset", "slow", "-crf", "18", "-pix_fmt", "yuv420p",
            str(destination)]


def _mux_command(video, audio, destination):
    return ["ffmpeg", "-v", "error", "-y", "-i", str(video), "-i", str(audio),
            "-map", "0:v", "-map", "1:a", "-af", LOUDNESS_FILTER, "-ar", "48000",
            "-c:v", "copy", "-c:a", "aac", "-b:a", "192k",
            "-ac", "2", "-shortest", "-movflags", "+faststart", str(destination)]


def _run(command):
    if subprocess.run(command).returncode:
        sys.exit(f"ffmpeg failed: {' '.join(command)}")


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
