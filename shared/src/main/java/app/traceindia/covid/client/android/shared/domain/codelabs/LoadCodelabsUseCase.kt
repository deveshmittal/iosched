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

package app.traceindia.covid.client.android.shared.domain.codelabs

import app.traceindia.covid.client.android.model.Codelab
import app.traceindia.covid.client.android.shared.data.codelabs.CodelabsRepository
import app.traceindia.covid.client.android.shared.domain.UseCase
import javax.inject.Inject

class LoadCodelabsUseCase @Inject constructor(private val repository: CodelabsRepository) :
    UseCase<Unit, List<Codelab>>() {

    override fun execute(parameters: Unit): List<Codelab> {
        return repository.getCodelabs()
            .sortedWith(compareByDescending<Codelab> { it.sortPriority }.thenBy { it.title })
    }
}
