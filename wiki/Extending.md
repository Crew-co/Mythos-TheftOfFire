# TheftOfFire — Extending

## What it reaches into

**Zeus** gains `withhold_fire` and `chain`. **Hephaestus** gains `make_pandora`. Neither of their own jars had any reason to give them either.

## The hole it opens in itself

**`prometheus:liberation`** → contribute a `Liberation`.

**This addon cannot finish its own story.** Prometheus is freed when Heracles walks past — four chapters away, in a jar nobody had written. So it leaves an objective open, possibly for years. That isn't a gap in the architecture; that's the architecture doing what the myth does.

```kotlin
compileOnly("net.crewco:theftoffire:0.1.0")   // for the type
// addon.yml:  depends: [ TheftOfFire ]

mythos.extensions.contribute(Liberation.POINT, ...)
```

**Load order does not matter.** `consume` replays every contribution already posted and receives
every one posted afterwards — so your jar may load before or after this one, and neither cares.
