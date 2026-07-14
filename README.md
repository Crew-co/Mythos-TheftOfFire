# TheftOfFire

Prometheus tricks Zeus, steals fire, and is nailed to a rock for it. The eagle does not
stop. **Story #4 — the era `theft-of-fire`.**

```kotlin
compileOnly("net.crewco:mythos-addon-api:0.1.0")
```

## The story, as mechanics

1. **`/power mecone`** — Prometheus divides the sacrifice and lets Zeus *choose*. This opens
   a two-slot GUI on Zeus's screen: a glistening pile of fat, and an ugly grey stomach. He
   takes the shiny one. He is a god; he chooses with his eyes. Under the fat: bones.
   *(A myth that turns entirely on one player picking between two things wants to be a
   two-slot menu. It's thirty lines, because the host owns the GUI framework.)*
2. **`/power withhold_fire`** — Zeus, humiliated, takes fire away. Mortals — and *only*
   mortals — can no longer light anything. That asymmetry is the injustice the rest of the
   chapter is about.
3. **`/power steal_fire`** — Prometheus goes up above y=150 and comes back with a coal in a
   hollow stalk. He right-clicks a mortal, and every hearth in the world lights at once.
4. **`/power make_pandora`** — Hephaestus builds her out of clay. Her role is *sealed* until
   this runs, then unsealed — so she's offered to the front of the spirit queue like any
   other vacancy. Whoever has waited longest gets to be the woman everybody blames.
5. **`/power open_jar`** — she was given a sealed jar and told not to open it, which is not a
   warning, it's an instruction. World difficulty goes to HARD, permanently. Hope stays in.
6. **`/power chain`** — Zeus nails him to a rock. The eagle comes every three minutes and
   takes twelve hearts. It *cannot kill him*: the engine already refuses lethal damage to a
   Titan from nothing at all, so the eagle takes him to the edge and leaves him there. That
   required no code.

## The button he doesn't press

Prometheus knows a secret Zeus would trade everything for — which goddess will bear a son
greater than his father. `/power endure speak` ends the punishment **right now**, any time,
and the Chronicle records that he talked.

The power exists purely so that *not using it* is a decision a player makes, every day, on
purpose. Without the button there's no defiance — just a man stuck to a rock.

## The ending it can't write

Prometheus is freed when Heracles walks past. Heracles is four chapters away, in a jar
nobody has written. So this addon **doesn't resolve its own last beat**:

```kotlin
// In LaboursOfHeracles, whenever that happens:
mythos.extensions.contribute(
    Liberation.POINT,
    Liberation("heracles", "a man in a lion's skin, on his way to somewhere else") { player ->
        mythos.roles.roleOf(player.uniqueId)?.id == "heracles"
    },
)
```

Until then the eagle comes every dawn and `/era` shows one objective outstanding — forever,
if need be. That's not a gap in the architecture. That's the architecture doing what the
myth does.
