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

package app.traceindia.covid.client.android.shared.domain.sessions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.schedule.PinnedSession
import app.traceindia.covid.client.android.model.schedule.PinnedSessionsSchedule
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.UserEventsResult
import app.traceindia.covid.client.android.shared.domain.repository.TestUserEventDataSource
import app.traceindia.covid.client.android.shared.model.TestDataRepository
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.SyncExecutorRule
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.shared.util.toEpochMilli
import app.traceindia.covid.client.android.test.data.TestData
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [LoadPinnedSessionsJsonUseCase]
 */
class LoadPinnedSessionsJsonUseCaseTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule var syncExecutorRule = SyncExecutorRule()

    @Test
    fun returnedUserSessions_areStarredOrReserved() {
        // Arrange
        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()
        val testUserEventRepository = DefaultSessionAndUserEventRepository(
            TestUserEventDataSource(userEventsResult),
            DefaultSessionRepository(TestDataRepository)
        )
        val useCase = LoadPinnedSessionsJsonUseCase(testUserEventRepository)
        val resultLiveData = useCase.observe()

        // Act
        useCase.execute("user1")

        // Assert
        val result = LiveDataTestUtil.getValue(resultLiveData)
                as Result.Success<String>
        val expected = PinnedSessionsSchedule(
            listOf(TestData.session0, TestData.session1, TestData.session2)
                .map {
                    PinnedSession(
                        name = it.title,
                        location = it.room?.name ?: "",
                        day = TimeUtils.abbreviatedDayForAr(it.startTime),
                        time = TimeUtils.abbreviatedTimeForAr(it.startTime),
                        timestamp = it.startTime.toEpochMilli(),
                        description = it.description
                    )
                }
        )

        val gson = GsonBuilder().create()
        assertThat(result.data, `is`(equalTo(gson.toJson(expected))))
    }
}
