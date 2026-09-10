# Squishy

> Plugin-driven overscroll for Jetpack Compose.

![License MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)
![Public](https://img.shields.io/badge/Public-yes-green.svg?style=flat)
[![](https://jitpack.io/v/iamjosephmj/Squishy.svg)](https://jitpack.io/#/iamjosephmj/Squishy)
![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen)
![Tests](https://img.shields.io/badge/tests-94%20passing-brightgreen)

![Squishy demo](docs/demo.gif)

One state object drives everything: scroll physics, edge behavior, visual plugins,
and per-item animations — for plain columns, LazyLists, and anything that scrolls.

## Quick start

```kotlin
repositories { maven { setUrl("https://jitpack.io") } }

dependencies { implementation("com.github.iamjosephmj:Squishy:2.1.0") }
```

```kotlin
val state = rememberOverScrollState(
    config = OverScrollConfig(curve = OverscrollCurve.RubberBand()),
)

Column(Modifier.fillMaxSize().overScroll(state)) {
    // content taller than the screen
}
```

*Drag past the end — the whole list stretches like taffy, fights you a little
more the deeper you pull, and springs home when you let go. Fling into the edge
and the leftover velocity becomes a bounce.*

That's the entire core API. Everything below is the same state object, wearing
different clothes.

## Recipes

### Make the whole list squish

*One modifier and the container itself becomes the spring — no fighting with
the platform stretch effect.*

```kotlin
Column(Modifier.overScroll(state)) { /* rows */ }
```

### Keep the box still — let the items breathe

*The container never moves an inch; every row drifts with the pull like a
loose stack of cards.*

```kotlin
Column(Modifier.overScroll(state, containerEffect = false)) {
    rows.forEach { row ->
        Text(row, Modifier.childOverScrollSupport(state))
    }
}
```

### Squish a LazyColumn

*Lazy lists bring their own scroll engine. `OverScrollArea` routes their edge
leftovers into yours and silences the platform effect — one wrapper, any
nested-scroll-aware scrollable.*

```kotlin
OverScrollArea(state, Modifier.fillMaxSize()) {
    LazyColumn(Modifier.fillMaxSize()) { /* items */ }
}
```

### Change the vibe, not the code

*One argument swaps the entire personality of the pull. Built-ins compose with `+`.*

```kotlin
val state = rememberOverScrollState(
    visual = OverscrollVisuals.zoom(minScaleX = 0.9f) + OverscrollVisuals.fade(),
    config = OverScrollConfig(maxOverscroll = 600f, curve = OverscrollCurve.RubberBand()),
)
```

| Visual | What it feels like |
|---|---|
| `pushDown()` | The classic — content follows your finger |
| `zoom()` | The list shrinks toward the edge, like compressing a sponge |
| `tilt()` | Cards lean into the pull, pivot at the edge |
| `blur()` | Depth-of-field right at the boundary (API 31+; `minAlpha < 1f` also fades on older devices) |
| `fade()` | The edge dissolves into the background |
| `rotate()` / `skew()` | Playful card-deck angles |

Per-child instead of per-container? Pair with `containerEffect = false`:

```kotlin
Text(item, Modifier.childOverScrollSupport(state, visual = OverscrollVisuals.tilt()))
```

### Write your own plugin in three lines

*A visual is a pure function of the pull — `(value, bounds, orientation) -> Modifier`.
No contracts to implement, no way to break the scroll pipeline.*

```kotlin
val wobble = OverscrollVisual { value, bounds, _ ->
    Modifier.graphicsLayer { rotationZ = value() / bounds * 20f }
}
```

### Tune the physics like a designer

*A config object, not a subclass. Change a number, drag, feel it.*

```kotlin
OverScrollConfig(
    maxOverscroll = 800f,
    curve = OverscrollCurve.RubberBand(stiffness = 3f),  // Linear / RubberBand / Custom
    settleSpec = tween(500),
    absorbVelocityFactor = 0.06f,     // fling-to-bounce strength
    absorbDurationMillis = 120,
    minAbsorbVelocity = 50f,
    topEdge = EdgeConfig(enabled = true, maxOverscroll = 300f),
    bottomEdge = EdgeConfig(enabled = false),  // that edge passes through to parents
)
```

`OverscrollCurve.Custom` accepts any `(rawDelta, current, max) -> Float` — want
an exponential wall? Two lines.

### Tag elements like `setTag()`

*RecyclerView muscle memory, Compose power: register animations by name once,
then tag items. Unknown names are a safe no-op.*

```kotlin
val state = rememberOverScrollState(
    config = OverScrollConfig(curve = OverscrollCurve.RubberBand()),
)
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

*The `index` in the transform is the item's position — the rows above fan out
with a stagger instead of moving as one block.* Roles can also carry plugins:
`roles.visual("hero", OverscrollVisuals.tilt())`.

Prefer lambdas per item? The child DSL gives you the same scope:
`value` (px), `progress` (0–1), `direction`, `index`:

```kotlin
Modifier.childOverScrollSupport(state) {
    translationY = value * 0.3f
    alpha = 1f - progress
}
```

## The demo app

The `app` module is a full showcase — six chambers, live telemetry on every
screen, Navigation 3 with predictive back, an Aperture-styled dark theme:

`01 Container` · `02 Child mode` · `03 Lazy column` · `04 Plugin playground` ·
`05 Curves & edges` (live physics tuning) · `06 Tagged roles`

Clone the repo and run it to feel every recipe above.

## Compatibility

- Min SDK 21, Compose Foundation 1.7+
- Library pinned against `foundation-android:1.7.8`; works with newer Compose at consumption time
- 94 tests: JVM contract tests (consumption accounting, curves, edges, absorb, settle) + Robolectric gesture integration

## v1 → v2 migration

- `Modifier.overScroll(isParentOverScrollEnabled, overscrollEffect, orientation, flingBehavior)` →
  `Modifier.overScroll(state: OverScrollState)`
- `Modifier.childOverScrollSupport(overscrollEffect)` → `Modifier.childOverScrollSupport(state)`
- `rememberPushDownOverscrollEffect(...)` remains; prefer `rememberOverScrollState(...)`
- `isParentOverScrollEnabled` is replaced by `containerEffect` + `OverScrollArea`

## Contributing

Issues and PRs welcome — raise PRs against the default branch, ensure
`./gradlew squishy:test app:lint` passes, and keep the lint analyzer clean.
Internally the library is organized into `physics` / `scroll` / `visual` /
`child` / `state` packages — one concern per file — so fixes land where
you'd expect.

## License

MIT — see [LICENSE](LICENSE).
