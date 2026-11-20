"""Mono audio primitives on plain float lists: oscillators, noise, filters, envelopes, mixing."""

import math
import random
import wave
from array import array

RATE = 44100
_random = random.Random(7)


def samples_for(seconds):
    return max(0, round(seconds * RATE))


def harmonic_tone(frequency, seconds, harmonics):
    """Sawtooth-like tone: partial k has amplitude 1/k, partials above Nyquist are dropped."""
    step = 2 * math.pi * frequency / RATE
    partials = [k for k in range(1, harmonics + 1) if k * frequency < RATE / 2]
    return [sum(math.sin(step * k * i) / k for k in partials) for i in range(samples_for(seconds))]


def sweep(start_frequency, end_frequency, seconds):
    """Sine gliding exponentially between two pitches; phase is accumulated so the glide is click-free."""
    count = samples_for(seconds)
    ratio = (end_frequency / start_frequency) ** (1 / count)
    output, phase, frequency = [], 0.0, start_frequency
    for _ in range(count):
        output.append(math.sin(phase))
        phase += 2 * math.pi * frequency / RATE
        frequency *= ratio
    return output


def noise(seconds):
    return [_random.uniform(-1, 1) for _ in range(samples_for(seconds))]


def lowpass(signal, cutoff):
    """One-pole low-pass, y += a·(x − y) with a = 1 − e^(−2π·fc/fs); cutoff may be a per-sample list."""
    cutoffs = cutoff if isinstance(cutoff, list) else [cutoff] * len(signal)
    output, y = [], 0.0
    for x, frequency in zip(signal, cutoffs):
        y += (1 - math.exp(-2 * math.pi * frequency / RATE)) * (x - y)
        output.append(y)
    return output


def highpass(signal, cutoff):
    return [x - y for x, y in zip(signal, lowpass(signal, cutoff))]


def decay(time_constant, seconds):
    return [math.exp(-i / (time_constant * RATE)) for i in range(samples_for(seconds))]


def attack_decay(attack, time_constant, seconds):
    rise = samples_for(attack) or 1
    return [min(1.0, i / rise) * math.exp(-max(0, i - rise) / (time_constant * RATE))
            for i in range(samples_for(seconds))]


def shaped(signal, envelope, gain=1.0):
    return [value * level * gain for value, level in zip(signal, envelope)]


def summed(*signals):
    length = max(len(signal) for signal in signals)
    return [sum(signal[i] for signal in signals if i < len(signal)) for i in range(length)]


def add_at(mix, sound, seconds):
    start = samples_for(seconds)
    for offset, value in enumerate(sound[:max(0, len(mix) - start)]):
        mix[start + offset] += value


def fade(mix, fade_in, fade_out):
    length, rise, fall = len(mix), samples_for(fade_in), samples_for(fade_out)
    for i in range(length):
        mix[i] *= min(1.0, i / rise, (length - i) / fall)


def write_wav(path, mix):
    peak = max(abs(value) for value in mix) or 1.0
    pcm = array("h", (round(value / peak * 0.89 * 32767) for value in mix))
    with wave.open(str(path), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(RATE)
        output.writeframes(pcm.tobytes())
