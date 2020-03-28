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

import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.data.userevent.SessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.MediatorUseCase
import app.traceindia.covid.client.android.shared.domain.sessions.StarReserveNotificationAlarmUpdater
import app.traceindia.covid.client.android.shared.result.Result
import javax.inject.Inject

open class StarEventAndNotifyUseCase @Inject constructor(
    private val repository: SessionAndUserEventRepository,
    private val alarmUpdater: StarReserveNotificationAlarmUpdater
) : MediatorUseCase<StarEventParameter, StarUpdatedStatus>() {

    override fun execute(parameters: StarEventParameter) {
        val updateResult = try {
            repository.starEvent(parameters.userId, parameters.userSession.userEvent)
        } catch (e: Exception) {
            result.postValue(Result.Error(e))
            return
        }
        // Avoid duplicating sources and trigger an update on the LiveData from the base class.
        result.removeSource(updateResult)
        result.addSource(updateResult) {
            alarmUpdater.updateSession(
                parameters.userSession,
                parameters.userSession.userEvent.isPreSessionNotificationRequired()
            )
            result.postValue(updateResult.value)
        }
    }
}

data class StarEventParameter(
    val userId: String,
    val userSession: UserSession
)

enum class StarUpdatedStatus {
    STARRED,
    UNSTARRED
}
