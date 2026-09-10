package io.iamjosephmj.squishy.navigation

/**
 * One entry per demo chamber; feeds the home cards, chamber scaffolds and
 * intros from one place.
 */
enum class DemoScreen(val num: String, val title: String, val blurb: String, val detail: String) {
    Container("01", "Container", "Path 1 basics — a plain column that squishes past its edges, container visual on.", "A plain column that scrolls and then squishes past its edges — the container itself carries the overscroll visual. Watch the telemetry while dragging past the top or bottom, and fling into an edge to see the velocity bounce settle back."),
    ChildMode("02", "Child mode", "containerEffect off — every item carries the overscroll instead of the box.", "The container stays completely still — every item carries the overscroll instead. All rows share one state, so the whole list responds to the same pull while the box never moves."),
    LazyColumn("03", "Lazy column", "OverScrollArea wrapping LazyColumn — edge leftovers feed the effect.", "LazyColumn owns its scroll pipeline internally, so it runs through OverScrollArea: edge leftovers are routed into the effect and the platform stretch is suppressed. Works with any nested-scroll-aware list."),
    Playground("04", "Plugin playground", "Swap overscroll visuals per child — built-ins and a custom one, live.", "Pick a visual — every built-in plus combinations, applied per child on a rubber band. Switch the chips to swap the effect live while the same state drives the list."),
    Curves("05", "Curves & edges", "Tune the drag curve, per-edge limits and max overscroll in real time.", "Tune the physics live: the drag curve (linear, rubber band, heavy band, half gain), per-edge enable, and the max overscroll limit. Change something, then drag the list to feel the difference."),
    Roles("06", "Tagged roles", "setTag()-style routing — name an item, register its animation, done.", "Every element is tagged with a role name, and the registry maps each name to an animation. Rows also carry their index, so one registered transform varies per position — pull past an edge to see the layers fan out."),
}
