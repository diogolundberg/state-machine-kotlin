package guru.boilerplate

data class Transition(
    val state: State,
    private val function: Function,
    val final: Boolean = false,
    val onTransition: (Transition, State) -> Unit = { _, _ -> },
) {
    operator fun invoke(state: State): State = function(state).also { result -> onTransition(this, result) }

    interface Function {
        operator fun invoke(state: State): State
    }
}
