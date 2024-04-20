package guru.boilerplate


class StateMachine(
    vararg transitions: Transition,
) {
    init {
        assert(transitions.isNotEmpty()) { NO_TRANSITIONS }
        assert(transitions.any { it.final }) { NO_FINAL_TRANSITION }
        assert(transitions.groupingBy { it.state.name }.eachCount().none { it.value > 1 }) { DUPLICATE_STATE }
    }

    private val transitions: Map<String, Transition> = transitions.associateBy { it.state.name }

    operator fun invoke(state: State): State =
        transitions[state.name]
            ?.run { this to invoke(state) }
            ?.let { (transition, result) -> if (transition.final) result else this(result) }
            ?: throw RuntimeException()

    private val State.name get() = this::class.simpleName!!

    private companion object {
        const val NO_TRANSITIONS = "No transitions"
        const val NO_FINAL_TRANSITION = "No final transition"
        const val DUPLICATE_STATE = "Duplicate state"
    }
}

