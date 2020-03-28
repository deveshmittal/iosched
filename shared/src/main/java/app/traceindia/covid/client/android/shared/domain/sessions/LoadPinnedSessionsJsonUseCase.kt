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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import app.traceindia.covid.client.android.model.schedule.PinnedSession
import app.traceindia.covid.client.android.model.schedule.PinnedSessionsSchedule
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.MediatorUseCase
import app.traceindia.covid.client.android.shared.domain.internal.DefaultScheduler
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.shared.util.toEpochMilli
import javax.inject.Inject

/**
 * Load a list of pinned (starred or reserved) [UserSession]s for a given user as a json format.
 *
 * Example JSON is structured as
 *
 * { schedule : [
 *   {
 *     "name": "session1",
 *     "location": "Room 1",
 *     "day": "5/07",
 *     "time": "13:30",
 *     "timestamp": 82547983,
 *     "description": "Session description1"
 *   },
 *   {
 *     "name": "session2",
 *     "location": "Room 2",
 *     "day": "5/08",
 *     "time": "13:30",
 *     "timestamp": 19238489,
 *     "description": "Session description2"
 *   }, .....
 *   ]
 * }
 */
open class LoadPinnedSessionsJsonUseCase @Inject constructor(
    private val userEventRepository: DefaultSessionAndUserEventRepository
) : MediatorUseCase<String, String>() {

    val gson: Gson = GsonBuilder().create()

    override fun execute(parameters: String) {
        val userSessionsObservable = userEventRepository.getObservableUserEvents(parameters)

        result.removeSource(userSessionsObservable)
        result.value = null
        result.addSource(userSessionsObservable) { observableResult ->
            DefaultScheduler.execute {
                when (observableResult) {
                    is Result.Success -> {
                        val useCaseResult = observableResult.data.userSessions.filter {
                            it.userEvent.isPinned()
                        }.map {
                            val session = it.session
                            // We assume the conference time zone because only on-site attendees are
                            // going to use the feature
                            val zonedTime =
                                TimeUtils.zonedTime(session.startTime,
                                    TimeUtils.CONFERENCE_TIMEZONE)
                            PinnedSession(name = session.title,
                                location = session.room?.name ?: "",
                                day = TimeUtils.abbreviatedDayForAr(zonedTime),
                                time = TimeUtils.abbreviatedTimeForAr(zonedTime),
                                timestamp = session.startTime.toEpochMilli(),
                                description = session.description)
                        }
                        val jsonResult = gson.toJson(PinnedSessionsSchedule(useCaseResult))
                        result.postValue(Result.Success(jsonResult))
                    }
                    is Result.Error -> {
                        result.postValue(observableResult)
                    }
                }
            }
        }
    }
}
