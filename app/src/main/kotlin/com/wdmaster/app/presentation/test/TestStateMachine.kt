package com.wdmaster.app.presentation.test

class TestStateMachine {

    enum class State {
        IDLE,
        LOADING_PAGE,
        WAITING_DOM,
        INJECTING_CARD,
        SUBMITTING_LOGIN,
        CHECKING_RESULT,
        SUCCESS,
        FAILURE,
        RETRY,
        LOGOUT
    }

    var currentState: State = State.IDLE
        private set

    @Synchronized
    fun transition(newState: State) {
        // الحالات المسموح بها دائمًا (للأمان)
        when (newState) {
            State.IDLE,
            State.LOADING_PAGE,
            State.SUCCESS,
            State.FAILURE -> {
                currentState = newState
                return
            }
            else -> {}
        }

        // التحقق من صحة الانتقالات للحالات الأخرى
        when (newState) {
            State.WAITING_DOM -> check(currentState == State.LOADING_PAGE)
            State.INJECTING_CARD -> check(currentState == State.WAITING_DOM)
            State.SUBMITTING_LOGIN -> check(currentState == State.INJECTING_CARD)
            State.CHECKING_RESULT -> check(currentState == State.SUBMITTING_LOGIN)
            State.RETRY -> check(currentState == State.FAILURE)
            State.LOGOUT -> check(
                currentState == State.SUCCESS || currentState == State.FAILURE
            )
            else -> {}
        }
        currentState = newState
    }

    fun reset() {
        currentState = State.IDLE
    }
}
