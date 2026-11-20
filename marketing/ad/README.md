# Vertical social ad

Renders a ~15 s, 1080×1920 (9:16) MP4 for TikTok / Reels / Shorts:
hook text → app recording in a phone frame → logo + Google Play badge.

```sh
python3 marketing/ad/render.py          # every language → out/partypuzl_ad_<lang>.mp4
python3 marketing/ad/render.py es       # just Spanish
AD_RECORDING_EN=~/Desktop/rec.webm python3 marketing/ad/render.py en   # recording elsewhere
```

Needs `ffmpeg` (with libx264) and Pillow. Each language has its own screen
recording (app set to that language), looked up by file name in the project root
unless `AD_RECORDING_<LANG>` points elsewhere. Recordings are not committed, and
the clip timings in `cuts.py` are tied to those specific recordings.

## Editing

- `ad_copy.py` — hook lines and end-card tagline per language.
- `cuts.py` — per language: the recording file and its clips (start, duration,
  caption, caption colour, optional `zoom` / `focus_y` crop).
- `config.py` — layout and timing: `HOOK_STYLES` (colour, start, size),
  `END_CARD_DURATION`. Text wider than `MAX_TEXT_WIDTH` shrinks to fit.
- `palette.py` — brand colours from the app theme.

A new language is an entry in `ad_copy.py` and `cuts.py`, plus
`assets/google_play_badge_<lang>.png`.

## Sound

All audio is generated in code, so it is original and safe for paid ads:

- `music.py` — an A-minor party loop at 133 BPM (`BEAT = HOOK_DURATION / 8`), so the
  hook lines land on beats and the full beat drops when the phone slides in.
- `sfx.py` — pop (hook lines, badge), slide (soft rising glide as the phone arrives), swish (each cut),
  chime (end card), boom (any clip with `boom_at` set in `cuts.py`).
- `soundtrack.py` — places the effects at the real cut times from the render,
  fades the music in (0.6 s) and out (1.8 s), and sets their relative levels.

`render.py` normalises the final mix to about −14 LUFS, the usual level for
TikTok / Reels. Silence the music by setting `MUSIC_LEVEL = 0` in `soundtrack.py`.

`assets/google_play_badge_en.png` is Google's official badge, downloaded from
the Play badge page. Google's brand rules forbid altering it (colour, shape, text);
scaling it is fine.
