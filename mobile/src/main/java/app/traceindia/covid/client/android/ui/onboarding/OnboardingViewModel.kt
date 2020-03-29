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

package app.traceindia.covid.client.android.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.shared.domain.prefs.OnboardingCompleteActionUseCase
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import javax.inject.Inject

/**
 * Records that onboarding has been completed and navigates user onward.
 */
class OnboardingViewModel @Inject constructor(
    private val onboardingCompleteActionUseCase: OnboardingCompleteActionUseCase,
    signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

    private val _navigateToAuthActivity = MutableLiveData<Event<Unit>>()
    val navigateToAuthActivity: LiveData<Event<Unit>> = _navigateToAuthActivity

    private val _navigateToSignInDialogAction = MutableLiveData<Event<Unit>>()
    val navigateToSignInDialogAction: LiveData<Event<Unit>> = _navigateToSignInDialogAction

    fun getStartedClick() {
        onboardingCompleteActionUseCase(true)
        _navigateToAuthActivity.postValue(Event(Unit))
    }

    fun onSigninClicked() {
        _navigateToSignInDialogAction.value = Event(Unit)
    }
}
