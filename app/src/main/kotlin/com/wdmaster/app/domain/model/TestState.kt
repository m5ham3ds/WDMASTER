package com.wdmaster.app.domain.model

sealed class TestState {
    object Idle : TestState()
    object LoadingPage : TestState()
    object WaitingDom : TestState()
    object InjectingCard : TestState()
    object SubmittingLogin : TestState()
    object CheckingResult : TestState()
    object Logout : TestState()
    data class Success(val message: String = "") : TestState()
    data class Failure(val reason: String = "") : TestState()
    object Retry : TestState()
}