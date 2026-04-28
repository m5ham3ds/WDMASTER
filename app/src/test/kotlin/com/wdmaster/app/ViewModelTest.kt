package com.wdmaster.app

import com.wdmaster.app.presentation.home.HomeViewModel
import com.wdmaster.app.domain.usecase.*
import com.wdmaster.app.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.*
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class ViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel(
            mock(GenerateCardsUseCase::class.java),
            mock(TestBatchUseCase::class.java),
            mock(CardRepository::class.java),
            mock(SessionRepository::class.java),
            mock(PatternLearningUseCase::class.java)
        )
    }

    @Test
    fun `initial state is idle`() {
        assert(viewModel.uiState.value is HomeViewModel.UiState.Idle)
    }
}