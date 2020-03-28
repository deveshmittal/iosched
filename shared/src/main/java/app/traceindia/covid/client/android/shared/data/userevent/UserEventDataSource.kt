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

package app.traceindia.covid.client.android.shared.data.userevent

import androidx.lifecycle.LiveData
import app.traceindia.covid.client.android.model.Session
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.userdata.UserEvent
import app.traceindia.covid.client.android.shared.domain.users.ReservationRequestAction
import app.traceindia.covid.client.android.shared.domain.users.StarUpdatedStatus
import app.traceindia.covid.client.android.shared.domain.users.SwapRequestAction
import app.traceindia.covid.client.android.shared.result.Result

interface UserEventDataSource {

    fun getObservableUserEvents(userId: String): LiveData<UserEventsResult>

    fun getObservableUserEvent(userId: String, eventId: SessionId): LiveData<UserEventResult>

    fun getUserEvents(userId: String): List<UserEvent>

    fun getUserEvent(userId: String, eventId: SessionId): UserEvent?

    /**
     * Toggle the isStarred status for an event.
     *
     * @param userId the userId ([FirebaseUser#uid]) of the current logged in user
     * @param userEvent the [UserEvent], which isStarred is going to be the updated status
     * @return the LiveData that represents the status of the star operation.
     */
    fun starEvent(userId: String, userEvent: UserEvent): LiveData<Result<StarUpdatedStatus>>

    fun recordFeedbackSent(
        userId: String,
        userEvent: UserEvent
    ): LiveData<Result<Unit>>

    fun requestReservation(
        userId: String,
        session: Session,
        action: ReservationRequestAction
    ): LiveData<Result<ReservationRequestAction>>

    fun swapReservation(
        userId: String,
        fromSession: Session,
        toSession: Session
    ): LiveData<Result<SwapRequestAction>>

    fun clearSingleEventSubscriptions()
}

data class UserEventsResult(
    val userEvents: List<UserEvent>,
    val userEventsMessage: UserEventMessage? = null
)

data class UserEventResult(
    val userEvent: UserEvent?,
    val userEventMessage: UserEventMessage? = null
)
