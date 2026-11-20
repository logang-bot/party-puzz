"""Generated party loop: a soft intro under the hook, then the full beat drops with the phone."""

import math
from dataclasses import dataclass
from functools import lru_cache

import config
from audio_synth import (RATE, add_at, attack_decay, decay, harmonic_tone, highpass, lowpass,
                         noise, samples_for, shaped, summed, sweep)

BEAT = config.HOOK_DURATION / 8
BAR = BEAT * 4
ARP_PATTERN = (0, 2, 1, 3, 0, 2, 1, 2)


@dataclass(frozen=True)
class Chord:
    root: int
    tones: tuple


CHORDS = (
    Chord(45, (57, 60, 64, 69)),
    Chord(41, (53, 57, 60, 65)),
    Chord(48, (60, 64, 67, 72)),
    Chord(43, (55, 59, 62, 67)),
)


def track(total_seconds, drop_at):
    music = [0.0] * samples_for(total_seconds)
    drop_bar = round(drop_at / BAR)
    for bar in range(math.ceil(total_seconds / BAR)):
        _add_bar(music, bar, drop_bar)
    _sidechain(music, drop_at)
    return music


def _add_bar(music, bar, drop_bar):
    chord, start, dropped = CHORDS[bar % len(CHORDS)], bar * BAR, bar >= drop_bar
    _add_arp(music, chord, (start, dropped))
    if dropped:
        _add_groove(music, chord, start)
    else:
        add_at(music, _kick(0.55), start)


def _add_arp(music, chord, placement):
    start, bright = placement
    for step, tone_index in enumerate(ARP_PATTERN):
        add_at(music, _pluck(chord.tones[tone_index] + 12, bright), start + step * BEAT / 2)


def _add_groove(music, chord, start):
    add_at(music, _pad(chord), start)
    for beat in range(4):
        at = start + beat * BEAT
        add_at(music, _kick(1.0), at)
        add_at(music, _hat(), at + BEAT / 2)
        add_at(music, _bass(chord.root), at)
        add_at(music, _bass(chord.root + 12), at + BEAT / 2)
        if beat % 2:
            add_at(music, _clap(), at)


def _sidechain(music, drop_at):
    """Pumping duck: gain dips by 40% on each beat and recovers with an 80 ms time constant."""
    first = samples_for(drop_at)
    for i in range(first, len(music)):
        since_beat = ((i - first) / RATE) % BEAT
        music[i] *= 1 - 0.4 * math.exp(-since_beat / 0.08)


def _frequency(midi_note):
    return 440.0 * 2 ** ((midi_note - 69) / 12)


@lru_cache(maxsize=None)
def _kick(level):
    return shaped(sweep(150, 45, 0.35), decay(0.09, 0.35), 0.9 * level)


@lru_cache(maxsize=None)
def _pluck(midi_note, bright):
    tone = harmonic_tone(_frequency(midi_note), 0.32, 6 if bright else 2)
    return shaped(tone, attack_decay(0.003, 0.1, 0.32), 0.16 if bright else 0.12)


@lru_cache(maxsize=None)
def _bass(midi_note):
    tone = lowpass(harmonic_tone(_frequency(midi_note), 0.21, 4), 900)
    return shaped(tone, attack_decay(0.004, 0.09, 0.21), 0.55)


@lru_cache(maxsize=1)
def _hat():
    return shaped(highpass(noise(0.06), 7000), decay(0.015, 0.06), 0.35)


@lru_cache(maxsize=1)
def _clap():
    body = highpass(lowpass(noise(0.2), 2500), 700)
    return shaped(body, decay(0.05, 0.2), 0.9)


@lru_cache(maxsize=None)
def _pad(chord):
    voices = [harmonic_tone(_frequency(note), BAR, 2) for note in chord.tones[:3]]
    return shaped(summed(*voices), attack_decay(0.15, BAR / 2, BAR), 0.05)
