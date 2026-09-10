# Squishy

![Squishy_octopus](https://github.com/iamjosephmj/Squishy/assets/18631114/339314ba-95ee-4ef4-b001-55f1279a47b8)

[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](https://github.com/iamjosephmj/squishy/blob/main/LICENSE)
[![Public Yes](https://img.shields.io/badge/Public-yes-green.svg?style=flat)]()
[![](https://jitpack.io/v/iamjosephmj/Squishy.svg)](https://jitpack.io/#iamjosephmj/Squishy)

Squishy is a lightweight library for controlling the overscroll effect of containers or
child composables based on user input. v2 is a ground-up rewrite of the overscroll
pipeline: one scroll pipeline per container, no fighting with the platform stretch effect,
correct nested-scroll consumption, and LazyList support.

## Gradle Setup

Add this to your root `build.gradle.kts`:

```kotlin
repositories {
    maven { setUrl("https://jitpack.io") }
}
```

Add this to your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.iamjosephmj:Squishy:2.1.0")
}
```

## Quick start (plain container)

```kotlin
val state = rememberOverScrollState()

Column(
    modifier = Modifier
        .fillMaxSize()
        .overScroll(state)
) {
    // content taller than the screen
}
```

Drag past the edge of the content and the whole container squishes, then settles back.

## Child-content mode

Children can participate in the parent's overscroll (each child moves with the squish):

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .overScroll(state, containerEffect = false)
) {
    for (item in 1..50) {
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .childOverScrollSupport(state)
                .height(50.dp)
        ) {
            Text(text = "item$item")
        }
    }
}
```

`containerEffect = false` suppresses the container's own visual so the children move instead
of double-moving.

## LazyColumn / LazyRow / external scroll containers

Lazy lists own their scroll pipeline internally, so use `OverScrollArea` — it feeds edge
leftovers into Squishy and suppresses the platform stretch effect inside:

```kotlin
val state = rememberOverScrollState()

OverScrollArea(state, Modifier.fillMaxSize()) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(50) { item ->
            Text("item $item", Modifier.fillMaxWidth().height(50.dp))
        }
    }
}
```

`OverScrollArea` works with any scrollable that participates in nested scroll.

## Plugins & custom effects (v2.1)

Pick a built-in visual, tune behavior with a config, or write your own in three lines:

```kotlin
val state = rememberOverScrollState(
    visual = OverscrollVisuals.zoom(minScaleX = 0.9f) + OverscrollVisuals.fade(),
    config = OverScrollConfig(
        maxOverscroll = 600f,
        curve = OverscrollCurve.RubberBand(),
        topEdge = EdgeConfig(enabled = true, maxOverscroll = 300f),
    ),
)
```

Built-ins: `pushDown()`, `zoom()`, `rotate()`, `skew()`, `tilt()`, `blur()` (blur
requires API 31+; pass `minAlpha < 1f` to also fade on older devices), `fade()` — combine with `+`.

Custom visual:

```kotlin
val wobble = OverscrollVisual { value, bounds, _ ->
    Modifier.graphicsLayer { rotationZ = value() / bounds * 20f }
}
```

Per-item animations with the child DSL — `value` (signed px), `progress` (0..1),
`direction` (Top/Bottom/None) inside a `graphicsLayer` block:

```kotlin
Text(
    "row",
    Modifier.childOverScrollSupport(state) {
        translationY = value * (1f - index * 0.03f)
        rotationZ = progress * 6f
    },
)
```

Apply a plugin visual to each child instead of the container — pair it with
`overScroll(state, containerEffect = false)` so the container stays static. Let the visual
drive the item's response; add a transform lambda only for effects the visual doesn't
already provide (translation on top of a visual double-applies):

```kotlin
Column(Modifier.overScroll(state, containerEffect = false)) {
    items.forEachIndexed { index, item ->
        Text(item, Modifier.childOverScrollSupport(state, visual = OverscrollVisuals.zoom()))
    }
}
```

`OverScrollConfig`: `maxOverscroll`, `curve` (Linear / RubberBand(stiffness) / Custom),
`settleSpec`, `absorbVelocityFactor`, `absorbDurationMillis`, `minAbsorbVelocity`,
`topEdge`/`bottomEdge` (`EdgeConfig(enabled, maxOverscroll)`; a disabled edge lets the
delta pass through to ancestors and skips fling absorb). `OverscrollDirection.Top` maps to
the leading edge (top for vertical, start for horizontal).

- Hoist or `remember` custom `OverscrollVisual` instances and `Custom` curve lambdas — they are
  compared by identity when keying internal state.
- Built-in factories are `@Composable` and already remembered, so inline usage is safe.

## Custom effects

Extend `BaseOverscrollEffect` and override `effectModifier` to draw your own effect.
`value` is the current overscroll offset (in pixels, clamped to `maxOverscroll`):

```kotlin
class MyEffect(
    orientation: Orientation,
    maxOverscroll: Float,
    animationSpec: AnimationSpec<Float>,
) : BaseOverscrollEffect(orientation, maxOverscroll, animationSpec) {
    override val effectModifier: Modifier = Modifier.graphicsLayer {
        val fade = 1f - (abs(value) / maxOverscroll)
        alpha = fade.coerceIn(0f, 1f)
    }
}

val state = rememberOverScrollState(MyEffect(Orientation.Vertical, 300f, tween(400)))
```

## API notes

- `rememberOverScrollState(orientation, maxOverscroll, animationSpec)` — state is recreated
  if parameters change; in-flight overscroll resets.
- `OverScrollState` exposes `overscrollOffset`, `isOverscrolling`, `scrollValue`,
  `maxScrollValue`, `scrollTo`, `animateScrollTo` for programmatic control.
- Overscroll only applies to user-driven drags; programmatic scrolls never squish. A fling
  that reaches an edge automatically triggers a velocity-proportional squish and settles
  back.
- The effect consumes overscroll delta and velocity, so outer scrollables do not react to
  a child's overscroll.
- `overScroll` requires bounded constraints along the scrolling axis from its parent (like
  `verticalScroll`); content that fits does not squish.
- `containerEffect = false` on `overScroll`/`OverScrollArea` disables the container's own
  visual so `childOverScrollSupport` children move instead (combining both double-applies
  the offset).
- Use one `OverScrollState` per container. Sharing a state between containers makes them
  fight (shared settle cancellation, last-measured scroll bounds).

## v1 → v2 migration

- `Modifier.overScroll(isParentOverScrollEnabled, overscrollEffect, orientation, flingBehavior)` →
  `Modifier.overScroll(state: OverScrollState)`.
- `Modifier.childOverScrollSupport(overscrollEffect)` → `Modifier.childOverScrollSupport(state)`.
- `rememberPushDownOverscrollEffect(...)` still exists; prefer `rememberOverScrollState(...)`.
- The `isParentOverScrollEnabled` flag is gone — use `OverScrollArea` for LazyList or
  external scroll containers.

## Contributing, Issues, or Ideas

If you encounter any issues with Squishy, please file a GitHub issue with as many details as
possible, including example code or steps to reproduce the issue. For feature requests,
submit an issue or a pull request.

## Contribution Guidelines

- Ensure all tests pass (`./gradlew squishy:test`).
- Raise a PR to the `develop` branch.
- Ensure no issues from Android Studio lint analyzer.
