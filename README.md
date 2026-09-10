# Squishy

Overscroll effects for Jetpack Compose that you actually control.

![License MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)
[![](https://jitpack.io/v/iamjosephmj/Squishy.svg)](https://jitpack.io/#/iamjosephmj/Squishy)
![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen)

![Squishy demo](docs/demo.gif)

Compose gives you the platform stretch and, past that, a cliff: want your list to
bounce like iOS? Tilt its cards at the edges? You're in nested-scroll territory,
doing consumption math and hoping you got the contracts right.

Squishy does that math once and hands you the parts worth caring about. One
`OverScrollState` per screen drives everything — scroll position, curves, edge
behavior, visuals, per-item animation. Everything below reads from that one object.

This library is a v2, rebuilt from scratch after v1 taught us exactly how
overscroll goes wrong: state that lagged a frame behind your finger, deltas leaking
into parent scrollables, the platform stretch fighting custom effects. Those are
bugs number one, two and three in the changelog of things that don't happen anymore.

## Install

```kotlin
repositories { maven { setUrl("https://jitpack.io") } }
```

```kotlin
dependencies { implementation("com.github.iamjosephmj:Squishy:2.1.0") }
```

## The two-minute version

```kotlin
val state = rememberOverScrollState(
    config = OverScrollConfig(curve = OverscrollCurve.RubberBand()),
)

Column(Modifier.fillMaxSize().overScroll(state)) {
    // content taller than the screen
}
```

Drag past the end and the list stretches, resists harder the deeper you pull, and
springs back when you let go. Fling into the edge and the leftover velocity becomes
a bounce. That's the core. The rest is choosing where the effect lands and what it
looks like.

## Where the effect lands

On the container — the classic:

```kotlin
Column(Modifier.overScroll(state)) { /* rows */ }
```

On the items instead — the box holds perfectly still while the rows drift with the
pull:

```kotlin
Column(Modifier.overScroll(state, containerEffect = false)) {
    rows.forEach { row ->
        Text(row, Modifier.childOverScrollSupport(state))
    }
}
```

On a LazyColumn — lazy lists bring their own scroll engine, so route their edges
through `OverScrollArea` (it also silences the platform effect inside):

```kotlin
OverScrollArea(state, Modifier.fillMaxSize()) {
    LazyColumn(Modifier.fillMaxSize()) { /* items */ }
}
```

`OverScrollArea` works with anything that participates in nested scroll, not just
lazy lists.

## What the pull looks like

Seven built-in visuals. One argument swaps them, `+` stacks them:

```kotlin
val state = rememberOverScrollState(
    visual = OverscrollVisuals.zoom(minScaleX = 0.9f) + OverscrollVisuals.fade(),
    config = OverScrollConfig(maxOverscroll = 600f),
)
```

The set: `pushDown` (content follows the finger), `zoom`, `tilt`, `fade`, `rotate`,
`skew`, and `blur` — blur needs API 31; pass `minAlpha < 1f` and older devices get
the fade instead.

Visuals attach per child too:

```kotlin
Text(item, Modifier.childOverScrollSupport(state, visual = OverscrollVisuals.tilt()))
```

And writing your own is a function, not a framework:

```kotlin
val wobble = OverscrollVisual { value, bounds, _ ->
    Modifier.graphicsLayer { rotationZ = value() / bounds * 20f }
}
```

There are no contracts to implement and no way to break the scroll pipeline — a
visual is just a `Modifier` built from the current pull.

## How it feels

Physics lives in a config object, not a subclass:

```kotlin
OverScrollConfig(
    maxOverscroll = 800f,
    curve = OverscrollCurve.RubberBand(stiffness = 3f),
    settleSpec = tween(500),              // spring-home animation
    absorbVelocityFactor = 0.06f,         // fling → bounce strength
    minAbsorbVelocity = 50f,              // ignore ghost flings
    topEdge = EdgeConfig(maxOverscroll = 300f),
    bottomEdge = EdgeConfig(enabled = false),
)
```

Curves are pluggable: `Linear`, `RubberBand(stiffness)`, or `Custom` with any
`(rawDelta, current, max) -> Float` mapping — an exponential wall is two lines.
A disabled edge passes its delta through to parent scrollables instead of eating it.

## Tags

RecyclerView veterans, you already know this one. `setTag()`, but the tag carries
an animation:

```kotlin
val roles = rememberOverScrollRoles {
    transform("header") { translationY = value * 0.10f }
    transform("row") { translationY = value * (0.22f + index * 0.012f) }
}

Column(Modifier.overScroll(state, containerEffect = false)) {
    Text("Title", Modifier.overscrollRole(state, roles, "header"))
    items.forEachIndexed { i, item ->
        Text(item, Modifier.overscrollRole(state, roles, "row", index = i))
    }
}
```

The scope exposes `value` (px), `progress` (0–1), `direction`, and the item's
`index` — which is why the `"row"` transform above staggers instead of moving as
one block. Roles can carry visuals too:
`roles.visual("hero", OverscrollVisuals.tilt())`. Unknown names do nothing, so
dynamic lists can't crash on stale tags.

If you'd rather skip the registry, the child DSL gives you the same scope inline:

```kotlin
Modifier.childOverScrollSupport(state) {
    translationY = value * 0.3f
    alpha = 1f - progress
}
```

## The demo app

The `app` module in this repo is documentation you can touch. Six chambers, each
with live overscroll telemetry (px, progress, scroll, status) so you can watch the
numbers move while you feel the physics:

1. **Container** — the two-minute version, running
2. **Child mode** — box frozen, rows drifting
3. **Lazy column** — `OverScrollArea` around a real list
4. **Plugin playground** — swap all seven visuals live, mid-gesture
5. **Curves & edges** — tune the physics, apply, drag, repeat
6. **Tagged roles** — the registry pattern with a per-row depth readout

Clone, run, poke.

## Current limits

- One visual per container. Per-edge visuals are on the roadmap, not in the box.
- `blur()` renders real blur on API 31+ only.
- A state is vertical or horizontal — not both at once.

## v1 → v2

- `Modifier.overScroll(isParentOverScrollEnabled, overscrollEffect, orientation, flingBehavior)`
  → `Modifier.overScroll(state)`
- `childOverScrollSupport(overscrollEffect)` → `childOverScrollSupport(state)`
- `rememberPushDownOverscrollEffect(...)` still exists; prefer `rememberOverScrollState(...)`

## Contributing

PRs against the default branch. `./gradlew squishy:test app:lint` should pass and
the lint analyzer should stay quiet. Internally: `physics` / `scroll` / `visual` /
`child` / `state` packages, one concern per file.

## License

MIT — see [LICENSE](LICENSE).
