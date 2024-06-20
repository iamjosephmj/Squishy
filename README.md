
![Squishy_octopus](https://github.com/iamjosephmj/Squishy/assets/18631114/339314ba-95ee-4ef4-b001-55f1279a47b8)

[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat)](https://github.com/iamjosephmj/squishy/blob/main/LICENSE)
[![Public Yes](https://img.shields.io/badge/Public-yes-green.svg?style=flat)]()
[![](https://jitpack.io/v/iamjosephmj/Squishy.svg)](https://jitpack.io/#iamjosephmj/Squishy)


# Squishy

<p>
Squishy is a lightweight library that lets developers control the overscroll effect of the parent container/child composables based on user input. The library exposes all the necessary parameters for developers to control the behavior. Here is the prototype of the scroll/fling behavior in relation to overscroll.
</p>


https://github.com/iamjosephmj/Squishy/assets/18631114/abe84672-1c56-49e6-99b7-67811cf60751

## Gradle

Add the following to your project's root build.gradle.kts file

```kotlin
 repositories {
        maven {
            setUrl("https://jitpack.io")
        }
    }
```

Add the following to your project's build.gradle.kts file

```kotlin
dependencies {
    implementation("com.github.iamjosephmj:Squishy:1.0.1")
}
```


### BaseOverscrollEffect

`BaseOverscrollEffect` is an abstract class designed to handle overscroll effects in a composable's parent container or child composables. This class provides the necessary infrastructure for applying custom overscroll and fling behaviors, utilizing a coroutine scope and animation mechanisms.

### Key Features

- **Overscroll Management**: Controls the overscroll effect based on user input, ensuring smooth and visually appealing interactions.
- **Customizable Animations**: Leverages `Animatable` and `AnimationSpec` to customize the overscroll and fling animations.
- **Orientation Support**: Supports both vertical and horizontal orientations.
- **Modifiable Behavior**: Exposes modifiers for easy integration and customization in Jetpack Compose.

### Modifiers

#### `Modifier.overScroll`

Applies overscroll effects to a composable, allowing developers to control the overscroll behavior, orientation, and fling behavior.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.overScroll(
    isParentOverScrollEnabled: Boolean = true,
    overscrollEffect: BaseOverscrollEffect,
    orientation: Orientation = Orientation.Vertical,
    flingBehavior: FlingBehavior? = null
): Modifier
```

- **Parameters**:
  - `isParentOverScrollEnabled`: A boolean flag to enable or disable parent overscroll.
  - `overscrollEffect`: An instance of `BaseOverscrollEffect` to apply custom overscroll behavior.
  - `orientation`: The scroll orientation (Vertical or Horizontal).
  - `flingBehavior`: Custom fling behavior (optional).

#### `Modifier.childOverScrollSupport`

Adds support for child composables to utilize the overscroll effect from the parent container.

```kotlin
fun Modifier.childOverScrollSupport(
    overscrollEffect: BaseOverscrollEffect
): Modifier
```

- **Parameters**:
  - `overscrollEffect`: An instance of `BaseOverscrollEffect` to apply custom overscroll behavior to child composables.

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

### Getting Started

1. Add `BaseOverscrollEffect` to your project.
2. Create custom overscroll effects by extending `BaseOverscrollEffect`.
3. Apply the `overScroll` and `childOverScrollSupport` modifiers to your composables.

Enhance the user experience of your Jetpack Compose applications with custom overscroll and fling behaviors using `BaseOverscrollEffect` and its related modifiers.

### How do we do the extension of the Base class?

In this example, we are extending the `BaseOverscrollEffect` class to create a custom overscroll effect called `PushDownOverscrollEffect`. Let's break down how this works:

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

#### Explanation:

- **Class Declaration**: We define a new class `PushDownOverscrollEffect` that extends `BaseOverscrollEffect`.
- **Constructor**: It takes parameters required for the base class constructor along with additional parameters specific to this effect.
- **Initializer**: In the initializer block, we call the constructor of the superclass (`BaseOverscrollEffect`) passing the required parameters.
- **`effectModifier` Override**: We override the `effectModifier` property of the base class to customize the behavior of the overscroll effect.
  - If the orientation is vertical, we use the `super.effectModifier` to inherit the base modifier and add an offset based on the current overscroll value.
  - If the orientation is horizontal, we apply a horizontal offset.

### `rememberPushDownOverscrollEffect` Function

```kotlin
@Composable
fun rememberPushDownOverscrollEffect(
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical,
    animationSpec: AnimationSpec<Float> = tween(500),
    animatable: Animatable<Float, out AnimationVector> = Animatable(0f),
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

#### Explanation:

- **Function Declaration**: This composable function creates and remembers an instance of the `PushDownOverscrollEffect`.
- **Default Parameters**: It provides default values for optional parameters such as `maxOverscroll`, `orientation`, `animationSpec`, and `animatable`.
- **Coroutine Scope**: It creates a coroutine scope using `rememberCoroutineScope()`.
- **Remembering Effect**: It remembers the created instance of `PushDownOverscrollEffect` using the `remember` composable function to ensure that the effect is retained across recompositions.

### Usage

You can use the `rememberPushDownOverscrollEffect` function to obtain an instance of `PushDownOverscrollEffect` in your composables, allowing you to apply customized overscroll behavior to your UI elements.

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

This setup enables you to easily integrate and apply the `PushDownOverscrollEffect` to your Jetpack Compose UI components, enhancing the user experience with custom overscroll behaviors.

## Limitations
- The Library doesn't work with `LazyList`


## Contribution, Issues or Future Ideas

If part of Squishy is not working correctly be sure to file a Github issue. In the issue provide as
many details as possible. This could include example code or the exact steps that you did so that
everyone can reproduce the issue. Sample projects are always the best way :). This makes it easy for
our developers or someone from the open-source community to start working!

If you have a feature idea submit an issue with a feature request or submit a pull request and we
will work with you to merge it in!

## Contribution guidelines

Contributions are more than welcome!
- You should make sure that all the test are working properly.
- You should raise a PR to `develop` branch
- Before you raise a PR please make sure your code had no issue from Android studio lint analyzer.  

## Please Share & Star the repository to keep me motivated.
  <a href = "https://github.com/iamjosephmj/squishy/stargazers">
     <img src = "https://img.shields.io/github/stars/iamjosephmj/squishy" />
  </a>
  <a href = "https://twitter.com/iamjosephmj">
     <img src = "https://img.shields.io/twitter/url?label=follow&style=social&url=https%3A%2F%2Ftwitter.com" />
  </a>




