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

package app.traceindia.covid.client.android.shared.domain.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.shared.data.feedback.FeedbackEndpoint
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.repository.TestUserEventDataSource
import app.traceindia.covid.client.android.shared.model.TestDataRepository
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.SyncExecutorRule
import app.traceindia.covid.client.android.test.data.TestData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [FeedbackUseCase]
 */
class FeedbackUseCaseTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncExecutorRule = SyncExecutorRule()

    @Test
    fun submit() {
        val testFeedbackEndpoint = object : FeedbackEndpoint {
            override fun sendFeedback(
                sessionId: SessionId,
                responses: Map<String, Int>
            ): LiveData<Result<Unit>> {
                return MutableLiveData(Result.Success(Unit))
            }
        }
        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(), DefaultSessionRepository(TestDataRepository)
        )
        val useCase = FeedbackUseCase(testFeedbackEndpoint, testUserEventRepository)

        val resultLiveData = useCase.observe()
        useCase.execute(FeedbackParameter(
            "userIdTest",
            TestData.userEvents[0],
            TestData.userEvents[0].id,
            mapOf("q1" to 1)
        ))

        val result = LiveDataTestUtil.getValue(resultLiveData)
        assertEquals(Result.Success(Unit), result)
    }
}
