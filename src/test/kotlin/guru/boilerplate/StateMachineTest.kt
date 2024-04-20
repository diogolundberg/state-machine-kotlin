package guru.boilerplate

import guru.boilerplate.Transition.Function
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

internal class StateMachineTest {

    @Test
    fun `should return result if final transition`() {
        val stateMachine = StateMachine(
            Transition(InitialState, mock { given(it(InitialState)).willReturn(FinalState) }, final = true),
        )

        stateMachine(InitialState) shouldBe FinalState
    }

    @Test
    fun `should have at least one transition`() {
        assertThrows<AssertionError> {
            StateMachine()
        }.run {
            message shouldBe NO_TRANSITIONS
        }
    }

    @Test
    fun `should have at least one final transition`() {
        assertThrows<AssertionError> {
            StateMachine(
                Transition(InitialState, mock { given(it(InitialState)).willReturn(FinalState) }),
            )
        }.run {
            message shouldBe NO_FINAL_TRANSITION
        }
    }

    @Test
    fun `should not accept duplicated states`() {
        assertThrows<AssertionError> {
            StateMachine(
                Transition(InitialState, mock { given(it(InitialState)).willReturn(FinalState) }, final = true),
                Transition(InitialState, mock { given(it(InitialState)).willReturn(FinalState) }, final = true),
            )
        }.run {
            message shouldBe DUPLICATE_STATE
        }
    }

    @ParameterizedTest
    @MethodSource("initial states")
    fun `should accept multiple initial states`(state: State) {
        val stateMachine = StateMachine(
            Transition(InitialState, mock { given(it(InitialState)).willReturn(FinalState) }, final = true),
            Transition(AlsoInitialState, mock { given(it(AlsoInitialState)).willReturn(FinalState) }, final = true),
        )

        stateMachine(state) shouldBe FinalState
    }

    @ParameterizedTest
    @MethodSource("valid states")
    fun `should return last state from any valid state`(state: State) {
        val stateMachine = StateMachine(
            Transition(InitialState, mock { given(it(InitialState)).willReturn(HalfwayState) }),
            Transition(AlsoInitialState, mock { given(it(AlsoInitialState)).willReturn(HalfwayState) }),
            Transition(HalfwayState, mock { given(it(HalfwayState)).willReturn(FinalState) }, final = true),
        )

        stateMachine(state) shouldBe FinalState
    }

    @Test
    fun `should invoke all transition functions`() {
        val initialFunction = mock<Function> { given(it(InitialState)).willReturn(HalfwayState) }
        val finalFunction = mock<Function> { given(it(HalfwayState)).willReturn(FinalState) }

        val stateMachine = StateMachine(
            Transition(InitialState, initialFunction),
            Transition(HalfwayState, finalFunction, final = true),
        )

        stateMachine(InitialState)

        verify(initialFunction)(InitialState)
        verify(finalFunction)(HalfwayState)
    }

    @Test
    fun `should not invoke transition function not on the path`() {
        val orphanFunction = mock<Function> { given(it(InitialState)).willReturn(FinalState) }
        val finalFunction = mock<Function> { given(it(HalfwayState)).willReturn(FinalState) }

        val stateMachine = StateMachine(
            Transition(InitialState, orphanFunction),
            Transition(HalfwayState, finalFunction, final = true),
        )

        stateMachine(HalfwayState)

        verifyNoInteractions(orphanFunction)
    }

    private companion object {
        object InitialState : State
        object FinalState : State
        object AlsoInitialState : State
        object HalfwayState : State

        @JvmStatic
        fun `initial states`() = listOf(InitialState, AlsoInitialState).map { Arguments.of(it) }

        @JvmStatic
        fun `valid states`() = listOf(InitialState, AlsoInitialState, HalfwayState).map { Arguments.of(it) }

        const val NO_TRANSITIONS = "No transitions"
        const val NO_FINAL_TRANSITION = "No final transition"
        const val DUPLICATE_STATE = "Duplicate state"
    }
}
