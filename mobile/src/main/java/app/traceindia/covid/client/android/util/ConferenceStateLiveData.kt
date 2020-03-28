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

package app.traceindia.covid.client.android.util

import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.shared.di.MainThreadHandler
import app.traceindia.covid.client.android.shared.domain.internal.IOSchedHandler
import app.traceindia.covid.client.android.shared.time.TimeProvider
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.util.ConferenceState.ENDED
import app.traceindia.covid.client.android.util.ConferenceState.STARTED
import app.traceindia.covid.client.android.util.ConferenceState.UPCOMING
import org.threeten.bp.Duration
import javax.inject.Inject

enum class ConferenceState { UPCOMING, STARTED, ENDED }

class ConferenceStateLiveData @Inject constructor(
    @MainThreadHandler private val handler: IOSchedHandler,
    private val timeProvider: TimeProvider
) : MutableLiveData<ConferenceState>() {

    private val updateRunnable = Runnable { checkTime() }

    override fun onActive() {
        super.onActive()
        checkTime()
    }

    override fun onInactive() {
        super.onInactive()
        handler.removeCallbacks(updateRunnable)
    }

    private fun checkTime() {
        val timeUntilStart = Duration.between(timeProvider.now(), TimeUtils.getKeynoteStartTime())
        if (timeUntilStart.isNegative) {
            val timeUntilEnd =
                Duration.between(timeProvider.now(), TimeUtils.getConferenceEndTime())
            if (timeUntilEnd.isNegative) {
                postValue(ENDED)
            } else {
                postValue(STARTED)
                // Check again after the duration, to mark ENDED in real-time
                handler.postDelayed(updateRunnable, timeUntilEnd.toMillis())
            }
        } else {
            postValue(UPCOMING)
            // Check again after the duration, to mark STARTED in real-time
            handler.postDelayed(updateRunnable, timeUntilStart.toMillis())
        }
    }
}
