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
import app.traceindia.covid.client.android.model.ConferenceDay
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.userdata.UserEvent
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.data.userevent.ObservableUserEvents
import app.traceindia.covid.client.android.shared.data.userevent.SessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.sessions.LoadUserSessionUseCaseResult
import app.traceindia.covid.client.android.shared.domain.sessions.StarReserveNotificationAlarmUpdater
import app.traceindia.covid.client.android.shared.domain.users.ReservationRequestAction.CancelAction
import app.traceindia.covid.client.android.shared.domain.users.ReservationRequestAction.RequestAction
import app.traceindia.covid.client.android.shared.notifications.SessionAlarmManager
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.SyncExecutorRule
import app.traceindia.covid.client.android.test.data.TestData
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ReservationActionUseCase].
 */
class ReservationActionUseCaseTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncExecutorRule = SyncExecutorRule()

    @Test
    fun sessionIsRequestedSuccessfully() {
        val useCase = ReservationActionUseCase(TestUserEventRepository, createFakeUpdater())

        val resultLiveData = useCase.observe()

        useCase.execute(
            ReservationRequestParameters(
                "userTest",
                TestData.session0.id,
                RequestAction(),
                null
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
        Assert.assertEquals(result, Result.Success(RequestAction()))
    }

    @Test
    fun sessionIsCanceledSuccessfully() {
        val useCase = ReservationActionUseCase(TestUserEventRepository, createFakeUpdater())

        val resultLiveData = useCase.observe()

        useCase.execute(
            ReservationRequestParameters(
                "userTest", TestData.session0.id,
                CancelAction(),
                null
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
        Assert.assertEquals(result, Result.Success(CancelAction()))
    }

    @Test
    fun requestFails() {
        val useCase = ReservationActionUseCase(FailingUserEventRepository, createFakeUpdater())

        val resultLiveData = useCase.observe()

        useCase.execute(
            ReservationRequestParameters(
                "userTest", TestData.session0.id,
                CancelAction(),
                null
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
        assertTrue(result is Result.Error)
    }
}

object TestUserEventRepository : SessionAndUserEventRepository {
    override fun getObservableUserEvents(
        userId: String?
    ): LiveData<Result<ObservableUserEvents>> {
        TODO("not implemented")
    }

    override fun getObservableUserEvent(
        userId: String?,
        eventId: SessionId
    ): LiveData<Result<LoadUserSessionUseCaseResult>> {
        TODO("not implemented")
    }

    override fun starEvent(
        userId: String,
        userEvent: UserEvent
    ): LiveData<Result<StarUpdatedStatus>> {
        TODO("not implemented")
    }

    override fun recordFeedbackSent(userId: String, userEvent: UserEvent): LiveData<Result<Unit>> {
        TODO("not implemented")
    }

    override fun changeReservation(
        userId: String,
        sessionId: SessionId,
        action: ReservationRequestAction
    ): LiveData<Result<ReservationRequestAction>> {
        val result = MutableLiveData<Result<ReservationRequestAction>>()
        result.postValue(
            Result.Success(
                if (action is RequestAction) RequestAction() else CancelAction()
            )
        )
        return result
    }

    override fun getUserEvents(userId: String?): List<UserEvent> {
        TODO("not implemented")
    }

    override fun swapReservation(
        userId: String,
        fromId: SessionId,
        toId: SessionId
    ): LiveData<Result<SwapRequestAction>> {
        TODO("not implemented")
    }

    override fun clearSingleEventSubscriptions() {}

    override fun getConferenceDays(): List<ConferenceDay> {
        TODO("not implemented")
    }

    override fun getUserSession(userId: String, sessionId: SessionId): UserSession {
        TODO("not implemented")
    }
}

object FailingUserEventRepository : SessionAndUserEventRepository {
    override fun getObservableUserEvents(
        userId: String?
    ): LiveData<Result<ObservableUserEvents>> {
        TODO("not implemented")
    }

    override fun getObservableUserEvent(
        userId: String?,
        eventId: SessionId
    ): LiveData<Result<LoadUserSessionUseCaseResult>> {
        TODO("not implemented")
    }

    override fun starEvent(
        userId: String,
        userEvent: UserEvent
    ): LiveData<Result<StarUpdatedStatus>> {
        TODO("not implemented")
    }

    override fun recordFeedbackSent(userId: String, userEvent: UserEvent): LiveData<Result<Unit>> {
        TODO("not implemented")
    }

    override fun changeReservation(
        userId: String,
        sessionId: SessionId,
        action: ReservationRequestAction
    ): LiveData<Result<ReservationRequestAction>> {
        throw Exception("Test")
    }

    override fun getUserEvents(userId: String?): List<UserEvent> {
        TODO("not implemented")
    }

    override fun swapReservation(
        userId: String,
        fromId: SessionId,
        toId: SessionId
    ): LiveData<Result<SwapRequestAction>> {
        TODO("not implemented")
    }

    override fun clearSingleEventSubscriptions() {}

    override fun getConferenceDays(): List<ConferenceDay> {
        TODO("not implemented")
    }

    override fun getUserSession(userId: String, sessionId: SessionId): UserSession {
        TODO("not implemented")
    }
}

private fun createFakeUpdater(): StarReserveNotificationAlarmUpdater {
    val alarmManager: SessionAlarmManager = mock()
    doNothing().whenever(alarmManager).cancelAlarmForSession(any())
    return StarReserveNotificationAlarmUpdater(alarmManager)
}
