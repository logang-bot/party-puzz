"""Hook lines and end-card tagline per language. Clip captions live in cuts.py."""

from dataclasses import dataclass


@dataclass(frozen=True)
class AdCopy:
    hook: tuple
    tagline: str


COPY = {
    "en": AdCopy(
        hook=("Party?", "No party?", "Doesn't\nmatter.", "Just try\nthis app."),
        tagline="Party or not, just play.",
    ),
    "es": AdCopy(
        hook=("¿Fiesta?", "¿Sin fiesta?", "No\nimporta.", "Solo prueba\nesta app."),
        tagline="Con fiesta o sin ella, a jugar.",
    ),
}
