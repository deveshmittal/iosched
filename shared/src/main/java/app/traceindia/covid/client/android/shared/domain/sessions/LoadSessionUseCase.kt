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

import app.traceindia.covid.client.android.model.Session
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.shared.data.session.SessionRepository
import app.traceindia.covid.client.android.shared.domain.MediatorUseCase
import app.traceindia.covid.client.android.shared.result.Result
import javax.inject.Inject

open class LoadSessionUseCase @Inject constructor(private val repository: SessionRepository) :
    MediatorUseCase<String, Session>() {

    override fun execute(parameters: SessionId) {
        val session = repository.getSessions().firstOrNull { it -> it.id == parameters }

        if (session == null) {
            result.postValue(Result.Error(SessionNotFoundException()))
        } else {
            session.type // Compute type from tags now so it's done in the background
            result.postValue(Result.Success(session))
        }
    }
}

class SessionNotFoundException : Exception()
