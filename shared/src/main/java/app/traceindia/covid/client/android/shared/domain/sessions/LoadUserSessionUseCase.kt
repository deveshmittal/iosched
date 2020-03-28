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

import androidx.lifecycle.LiveData
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.UserEventMessage
import app.traceindia.covid.client.android.shared.domain.MediatorUseCase
import app.traceindia.covid.client.android.shared.domain.internal.DefaultScheduler
import app.traceindia.covid.client.android.shared.result.Result
import javax.inject.Inject

open class LoadUserSessionUseCase @Inject constructor(
    private val userEventRepository: DefaultSessionAndUserEventRepository
) : MediatorUseCase<Pair<String?, SessionId>, LoadUserSessionUseCaseResult>() {

    private var userSession: LiveData<Result<LoadUserSessionUseCaseResult>>? = null

    override fun execute(parameters: Pair<String?, SessionId>) {
        val (userId, eventId) = parameters

        // Remove old data sources
        clearSources()

        // Fetch an observable of the data
        val newUserSession = userEventRepository.getObservableUserEvent(userId, eventId)

        // Post new values to the result object.
        result.addSource(newUserSession) {
            DefaultScheduler.execute {
                when (it) {
                    is Result.Success -> {
                        val useCaseResult = LoadUserSessionUseCaseResult(
                            userSession = it.data.userSession,
                            userMessage = it.data.userMessage
                        )
                        result.postValue(Result.Success(useCaseResult))
                    }
                    is Result.Error -> {
                        result.postValue(it)
                    }
                }
            }
        }
        // Save a reference to the observable for later cleaning of sources.
        userSession = newUserSession
    }

    fun onCleared() {
        clearSources()
    }

    private fun clearSources() {
        userSession?.let {
            result.removeSource(it)
        }
        result.value = null
    }
}

data class LoadUserSessionUseCaseResult(
    val userSession: UserSession,

    /** A message to show to the user with important changes like reservation confirmations */
    val userMessage: UserEventMessage? = null
)
