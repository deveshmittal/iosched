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

package app.traceindia.covid.client.android.ui.agenda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.model.Block
import app.traceindia.covid.client.android.shared.domain.agenda.LoadAgendaUseCase
import app.traceindia.covid.client.android.shared.domain.invoke
import app.traceindia.covid.client.android.shared.domain.settings.GetTimeZoneUseCase
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.shared.util.map
import org.threeten.bp.ZoneId
import javax.inject.Inject

class AgendaViewModel @Inject constructor(
    loadAgendaUseCase: LoadAgendaUseCase,
    getTimeZoneUseCase: GetTimeZoneUseCase
) : ViewModel() {

    val loadAgendaResult: LiveData<List<Block>>

    private val preferConferenceTimeZoneResult = MutableLiveData<Result<Boolean>>()
    val timeZoneId: LiveData<ZoneId>

    init {
        val showInConferenceTimeZone = preferConferenceTimeZoneResult.map {
            (it as? Result.Success<Boolean>)?.data ?: true
        }
        timeZoneId = showInConferenceTimeZone.map { inConferenceTimeZone ->
            if (inConferenceTimeZone) {
                TimeUtils.CONFERENCE_TIMEZONE
            } else {
                ZoneId.systemDefault()
            }
        }

        // Load agenda blocks.
        getTimeZoneUseCase(preferConferenceTimeZoneResult)
        val observableAgenda = loadAgendaUseCase.observe()
        loadAgendaUseCase.execute(Unit)
        loadAgendaResult = observableAgenda.map {
            (it as? Result.Success)?.data ?: emptyList()
        }
    }
}
