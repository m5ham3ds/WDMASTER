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
        // التحقق من صحة الانتقالات
        when (newState) {
            State.LOADING_PAGE -> check(currentState == State.IDLE || currentState == State.RETRY)
            State.WAITING_DOM -> check(currentState == State.LOADING_PAGE)
            State.INJECTING_CARD -> check(currentState == State.WAITING_DOM)
            State.SUBMITTING_LOGIN -> check(currentState == State.INJECTING_CARD)
            State.CHECKING_RESULT -> check(currentState == State.SUBMITTING_LOGIN)
            State.SUCCESS -> check(currentState == State.CHECKING_RESULT)
            State.FAILURE -> check(
                currentState == State.CHECKING_RESULT ||
                currentState == State.LOADING_PAGE ||
                currentState == State.WAITING_DOM ||
                currentState == State.INJECTING_CARD
            )
            State.RETRY -> check(currentState == State.FAILURE)
            State.LOGOUT -> check(
                currentState == State.SUCCESS || currentState == State.FAILURE
            )
            State.IDLE -> {} // يمكن العودة إلى IDLE من أي حالة
        }
        currentState = newState
    }

    fun reset() {
        currentState = State.IDLE
    }
}