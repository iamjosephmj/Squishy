# Squishy

![Squishy_octopus](https://github.com/iamjosephmj/Squishy/assets/18631114/339314ba-95ee-4ef4-b001-55f1279a47b8)

[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](https://github.com/iamjosephmj/squishy/blob/main/LICENSE)
[![Public Yes](https://img.shields.io/badge/Public-yes-green.svg?style=flat)]()
[![](https://jitpack.io/v/iamjosephmj/Squishy.svg)](https://jitpack.io/#iamjosephmj/Squishy)

Squishy is a lightweight library for controlling the overscroll effect of parent containers or child composables based on user input. It provides parameters to customize the behavior of overscroll effects.

https://github.com/iamjosephmj/Squishy/assets/18631114/abe84672-1c56-49e6-99b7-67811cf60751

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
    implementation("com.github.iamjosephmj:Squishy:1.0.3")
}
```

## Features

### BaseOverscrollEffect

`BaseOverscrollEffect` is an abstract class for handling overscroll effects in a composable's parent container or child composables. It provides infrastructure for custom overscroll and fling behaviors using coroutines and animations.

### Key Features

- **Overscroll Management**: Control the overscroll effect for smooth interactions.
- **Customizable Animations**: Use `Animatable` and `AnimationSpec` for custom animations.
- **Orientation Support**: Supports vertical and horizontal orientations.
- **Modifiable Behavior**: Provides modifiers for easy integration and customization in Jetpack Compose.

### Modifiers

#### `Modifier.overScroll`

Applies overscroll effects to a composable.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.overScroll(
    isParentOverScrollEnabled: Boolean = true,
    overscrollEffect: BaseOverscrollEffect,
    orientation: Orientation = Orientation.Vertical,
    flingBehavior: FlingBehavior? = null
): Modifier
```

- `isParentOverScrollEnabled`: Enable or disable parent overscroll.
- `overscrollEffect`: Custom overscroll behavior.
- `orientation`: Scroll orientation (Vertical or Horizontal).
- `flingBehavior`: Custom fling behavior (optional).

#### `Modifier.childOverScrollSupport`

Allows child composables to utilize the overscroll effect from the parent container.

```kotlin
fun Modifier.childOverScrollSupport(
    overscrollEffect: BaseOverscrollEffect
): Modifier
```

- `overscrollEffect`: Custom overscroll behavior for child composables.

### Example Usage

```kotlin
val overscrollEffect = MyCustomOverscrollEffect(
    scope = rememberCoroutineScope(),
    orientation = Orientation.Vertical,
    maxOverscroll = 300f,
    animatable = remember { Animatable(0f) },
    animationSpec = tween(durationMillis = 500)
)

Column(
    modifier = Modifier
        .fillMaxSize()
        .overScroll(
            isParentOverScrollEnabled = true,
            overscrollEffect = overscrollEffect,
            orientation = Orientation.Vertical
        )
) {
    // Content here
}
```

## Adding Child Overscroll

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .overScroll(
            isParentOverScrollEnabled = false, 
            overscrollEffect = overscrollEffect,
            orientation = Orientation.Vertical
        )
) {
    for (item in 1..50) {
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .childOverScrollSupport(overScroll)
                .height(50.dp)
                .padding(1.dp)
        ) {
            Text(text = "item$item", textAlign = TextAlign.Center)
        }
    }
}
```

## Getting Started

1. Add `BaseOverscrollEffect` to your project.
2. Create custom overscroll effects by extending `BaseOverscrollEffect`.
3. Apply the `overScroll` and `childOverScrollSupport` modifiers to your composables.

## Extending the Base Class

### `PushDownOverscrollEffect` Class

```kotlin
class PushDownOverscrollEffect(
    scope: CoroutineScope,
    maxOverscroll: Float,
    orientation: Orientation,
    animatable: Animatable<Float, out AnimationVector>,
    animationSpec: AnimationSpec<Float>
) : BaseOverscrollEffect(
    scope = scope,
    maxOverscroll = maxOverscroll,
    orientation = orientation,
    animatable = animatable,
    animationSpec = animationSpec
) {
    override val effectModifier: Modifier
        get() = if (orientation == Orientation.Vertical) {
            super.effectModifier
                .offset { IntOffset(0, getOffsetValue().roundToInt()) }
        } else {
            super.effectModifier.offset { IntOffset(getOffsetValue().roundToInt(), 0) }
        }
}
```

### `rememberPushDownOverscrollEffect` Function

```kotlin
@Composable
fun rememberPushDownOverscrollEffect(
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical,
    animationSpec: AnimationSpec<Float> = tween(500),
    animatable: Animatable<Float, out AnimationVector> = Animatable(0f)
): BaseOverscrollEffect {
    val scope = rememberCoroutineScope()
    return remember {
        PushDownOverscrollEffect(
            scope,
            maxOverscroll,
            orientation,
            animatable,
            animationSpec
        )
    }
}
```

### Usage

```kotlin
val pushDownOverscrollEffect = rememberPushDownOverscrollEffect()

Column(
    modifier = Modifier
        .fillMaxSize()
        .overScroll(
            overscrollEffect = pushDownOverscrollEffect
        )
) {
    // Content here
}
```

## Limitations

- The library doesn't work with `LazyList`.

## Contributing, Issues, or Ideas

If you encounter any issues with Squishy, please file a GitHub issue with as many details as possible, including example code or steps to reproduce the issue. For feature requests, submit an issue or a pull request.

## Contribution Guidelines

- Ensure all tests pass.
- Raise a PR to the `develop` branch.
- Ensure no issues from Android Studio lint analyzer.

## Please Share & Star the Repository to Keep Me Motivated

[![GitHub Stars](https://img.shields.io/github/stars/iamjosephmj/squishy)](https://github.com/iamjosephmj/squishy/stargazers)
[![Twitter Follow](https://img.shields.io/twitter/url?label=follow&style=social&url=https%3A%2F%2Ftwitter.com%2Fiamjosephmj)](https://twitter.com/iamjosephmj)
