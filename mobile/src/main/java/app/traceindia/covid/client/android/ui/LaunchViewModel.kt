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

package app.traceindia.covid.client.android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.shared.domain.prefs.AuthCompletedUseCase
import app.traceindia.covid.client.android.shared.domain.prefs.OnboardingCompletedUseCase
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.util.map
import javax.inject.Inject

/**
 * Logic for determining which screen to send users to on app launch.
 */
class LaunchViewModel @Inject constructor(
    onboardingCompletedUseCase: OnboardingCompletedUseCase,
    authCompletedUseCase: AuthCompletedUseCase
) : ViewModel() {

    private val onboardingCompletedResult = MutableLiveData<Result<Boolean>>()
    private val authCompletedResult = MutableLiveData<Result<Boolean>>()
    val launchDestination: LiveData<Event<LaunchDestination>>

    init {
        // Check if onboarding has already been completed and then navigate the user accordingly
        onboardingCompletedUseCase(Unit, onboardingCompletedResult)
        authCompletedUseCase(false, authCompletedResult)
        launchDestination = onboardingCompletedResult.map {
            // If this check fails, prefer to launch main activity than show onboarding too often
            if ((it as? Result.Success)?.data == false) {
                Event(LaunchDestination.ONBOARDING)
            } else {

                Event(LaunchDestination.AUTH_ACTIVITY)
            }
        }
    }
}

enum class LaunchDestination {
    ONBOARDING,
    MAIN_ACTIVITY,
    AUTH_ACTIVITY
}
