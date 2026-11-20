"""Builds the ad's audio: generated music faded in and out, with effects on each transition."""

from dataclasses import dataclass, field

import config
import end_card
import music
import sfx
from audio_synth import add_at, fade, write_wav

MUSIC_FADE_IN = 0.6
MUSIC_FADE_OUT = 1.8
MUSIC_LEVEL = 0.55


@dataclass
class Timeline:
    frames: int = 0
    clip_starts: list = field(default_factory=list)
    end_card_start: float = 0.0

    def seconds(self):
        return self.frames / config.FPS


def write_soundtrack(timeline, path):
    mix = music.track(timeline.seconds(), config.HOOK_DURATION)
    fade(mix, MUSIC_FADE_IN, MUSIC_FADE_OUT)
    mix = [value * MUSIC_LEVEL for value in mix]
    for at, sound in _effects(timeline):
        add_at(mix, sound, at)
    write_wav(path, mix)


def _effects(timeline):
    hook = [(line.start, sfx.pop()) for line in config.HOOK_LINES]
    arrival = [(config.HOOK_DURATION - sfx.SLIDE_SECONDS * sfx.SLIDE_PEAK + 0.15, sfx.slide())]
    cuts = [(at, sfx.swish()) for at in timeline.clip_starts[1:] + [timeline.end_card_start]]
    return hook + arrival + cuts + _booms(timeline) + _end_card_effects(timeline.end_card_start)


def _booms(timeline):
    pairs = zip(timeline.clip_starts, config.CLIPS)
    return [(at + clip.boom_at, sfx.boom()) for at, clip in pairs if clip.boom_at is not None]


def _end_card_effects(start):
    return [(start + end_card.ICON_DELAY, sfx.chime()), (start + end_card.BADGE_DELAY, sfx.pop())]
