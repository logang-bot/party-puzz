"""Generated transition sounds: pop, slide, swish, chime and boom."""

import math
from functools import lru_cache

from audio_synth import (attack_decay, decay, highpass, lowpass, noise, samples_for, shaped,
                         summed, sweep)

SLIDE_SECONDS = 0.42
SLIDE_PEAK = 0.7


@lru_cache(maxsize=1)
def pop():
    return shaped(sweep(480, 1250, 0.09), attack_decay(0.002, 0.025, 0.09), 0.7)


@lru_cache(maxsize=1)
def slide():
    """Soft rising glide: two sines an octave apart sweep up one octave, swelling to SLIDE_PEAK (sin² curve)."""
    count = samples_for(SLIDE_SECONDS)
    swell = [_slide_curve(i / count) for i in range(count)]
    low = shaped(sweep(330, 660, SLIDE_SECONDS), swell, 0.22)
    high = shaped(sweep(660, 1320, SLIDE_SECONDS), swell, 0.08)
    return summed(low, high)


def _slide_curve(position):
    warped = position / SLIDE_PEAK if position < SLIDE_PEAK else (
        1 + (position - SLIDE_PEAK) / (1 - SLIDE_PEAK))
    return math.sin(math.pi * warped / 2) ** 2


@lru_cache(maxsize=1)
def swish():
    body = lowpass(highpass(noise(0.22), 1800), 9000)
    return shaped(body, attack_decay(0.04, 0.045, 0.22), 0.55)


@lru_cache(maxsize=1)
def chime():
    bells = [[0.0] * samples_for(0.08 * index) + _bell(note) for index, note in enumerate((79, 84, 88))]
    return summed(*bells)


def _bell(midi_note):
    """Bell timbre: fundamental plus an inharmonic 2.76× partial that dies away faster."""
    frequency = 440.0 * 2 ** ((midi_note - 69) / 12)
    body = shaped(sweep(frequency, frequency, 0.9), decay(0.3, 0.9), 0.32)
    shimmer = shaped(sweep(frequency * 2.76, frequency * 2.76, 0.9), decay(0.08, 0.9), 0.12)
    return summed(body, shimmer)


@lru_cache(maxsize=1)
def boom():
    thump = shaped(sweep(110, 32, 1.0), decay(0.28, 1.0), 1.1)
    rumble = shaped(lowpass(lowpass(lowpass(noise(1.0), 300), 300), 300), decay(0.16, 1.0), 9.0)
    return summed(thump, rumble)
