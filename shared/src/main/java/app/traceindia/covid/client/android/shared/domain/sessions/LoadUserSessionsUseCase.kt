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

import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.MediatorUseCase
import app.traceindia.covid.client.android.shared.domain.internal.DefaultScheduler
import app.traceindia.covid.client.android.shared.result.Result
import javax.inject.Inject

/**
 * Load [UserSession]s for a given list of sessions.
 */
open class LoadUserSessionsUseCase @Inject constructor(
    private val userEventRepository: DefaultSessionAndUserEventRepository
) : MediatorUseCase<Pair<String?, Set<SessionId>>, LoadUserSessionsUseCaseResult>() {

    override fun execute(parameters: Pair<String?, Set<String>>) {
        val (userId, eventIds) = parameters
        // Observe *all* user events
        val userSessionsObservable = userEventRepository.getObservableUserEvents(userId)

        result.removeSource(userSessionsObservable)
        result.value = null
        result.addSource(userSessionsObservable) { observableResult ->
            DefaultScheduler.execute {
                when (observableResult) {
                    is Result.Success -> {
                        // Filter down to events for sessions we're interested in
                        val relevantUserSessions = observableResult.data.userSessions
                            .filter { it.session.id in eventIds }
                            .sortedBy { it.session.startTime }
                        if (relevantUserSessions.isNotEmpty()) {
                            val useCaseResult = LoadUserSessionsUseCaseResult(relevantUserSessions)
                            result.postValue(Result.Success(useCaseResult))
                        }
                    }
                    is Result.Error -> {
                        result.postValue(observableResult)
                    }
                }
            }
        }
    }
}

data class LoadUserSessionsUseCaseResult(val userSessions: List<UserSession>)
