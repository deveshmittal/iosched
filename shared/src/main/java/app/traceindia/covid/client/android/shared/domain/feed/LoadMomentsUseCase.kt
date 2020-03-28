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

package app.traceindia.covid.client.android.shared.domain.feed

import app.traceindia.covid.client.android.model.Moment
import app.traceindia.covid.client.android.shared.data.feed.FeedRepository
import app.traceindia.covid.client.android.shared.domain.UseCase
import javax.inject.Inject

/**
 * Loads all moments into a list.
 */
open class LoadMomentsUseCase @Inject constructor(
    private val repository: FeedRepository
) : UseCase<Unit, List<Moment>>() {

    override fun execute(parameters: Unit): List<Moment> {
        return repository.getMoments()
    }
}
