/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.traceindia.covid.client.android.ui.sessiondetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.TestDataRepository
import app.traceindia.covid.client.android.shared.data.feedback.FeedbackEndpoint
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.UserEventDataSource
import app.traceindia.covid.client.android.shared.domain.sessions.LoadUserSessionUseCase
import app.traceindia.covid.client.android.shared.domain.users.FeedbackUseCase
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.NetworkUtils
import app.traceindia.covid.client.android.test.data.TestData
import app.traceindia.covid.client.android.test.util.SyncTaskExecutorRule
import app.traceindia.covid.client.android.test.util.fakes.FakeSignInViewModelDelegate
import app.traceindia.covid.client.android.test.util.time.FakeIntervalMapperRule
import app.traceindia.covid.client.android.test.util.time.FixedTimeExecutorRule
import app.traceindia.covid.client.android.ui.schedule.TestUserEventDataSource
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the [SessionFeedbackViewModel].
 */
class SessionFeedbackViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    // Allows explicit setting of "now"
    @get:Rule
    var fixedTimeExecutorRule = FixedTimeExecutorRule()

    // Allows IntervalMapper to execute immediately
    @get:Rule
    var fakeIntervalMapperRule = FakeIntervalMapperRule()

    private lateinit var viewModel: SessionFeedbackViewModel
    private val testSession = TestData.session0

    private lateinit var mockNetworkUtils: NetworkUtils

    @Before
    fun setup() {
        mockNetworkUtils = mock {
            on { hasNetworkConnection() }.doReturn(true)
        }

        viewModel = createSessionFeedbackViewModel()
        viewModel.setSessionId(testSession.id)
    }

    @Test
    fun title() {
        assertEquals(testSession.title, LiveDataTestUtil.getValue(viewModel.title))
    }

    @Test
    fun questions() {
        val questions = LiveDataTestUtil.getValue(viewModel.questions)!!
        assertEquals(4, questions.size)
        // TODO: b/124489280
        assertEquals(0, questions[0].currentRating) // TODO: This should be 1
        assertEquals(0, questions[1].currentRating) // TODO: This should be 2
        assertEquals(0, questions[2].currentRating)
        assertEquals(0, questions[3].currentRating)
    }

    private fun createSessionFeedbackViewModel(
        signInViewModelPlugin: SignInViewModelDelegate = FakeSignInViewModelDelegate(),
        loadUserSessionUseCase: LoadUserSessionUseCase = createTestLoadUserSessionUseCase(),
        feedbackUseCase: FeedbackUseCase = createTestFeedbackUseCase()
    ): SessionFeedbackViewModel {
        return SessionFeedbackViewModel(
            signInViewModelDelegate = signInViewModelPlugin,
            loadUserSessionUseCase = loadUserSessionUseCase,
            feedbackUseCase = feedbackUseCase
        )
    }

    private fun createTestLoadUserSessionUseCase(
        userEventDataSource: UserEventDataSource = TestUserEventDataSource()
    ): LoadUserSessionUseCase {
        return LoadUserSessionUseCase(
            DefaultSessionAndUserEventRepository(
                userEventDataSource,
                DefaultSessionRepository(TestDataRepository)
            )
        )
    }

    private fun createTestFeedbackUseCase(
        userEventDataSource: UserEventDataSource = TestUserEventDataSource()
    ): FeedbackUseCase {
        return FeedbackUseCase(
            object : FeedbackEndpoint {
                override fun sendFeedback(
                    sessionId: SessionId,
                    responses: Map<String, Int>
                ): LiveData<Result<Unit>> {
                    return MutableLiveData(Result.Success(Unit))
                }
            },
            DefaultSessionAndUserEventRepository(
                userEventDataSource,
                DefaultSessionRepository(TestDataRepository)
            )
        )
    }
}
