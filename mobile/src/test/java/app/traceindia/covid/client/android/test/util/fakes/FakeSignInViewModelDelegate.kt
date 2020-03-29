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

package app.traceindia.covid.client.android.test.util.fakes

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.shared.data.signin.AuthenticatedUserInfo
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.ui.signin.SignInEvent
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock

class FakeSignInViewModelDelegate : SignInViewModelDelegate {

    override val currentUserInfo = MutableLiveData<AuthenticatedUserInfo?>()
    override val currentUserImageUri = MutableLiveData<Uri?>()
    override val performSignInEvent = MutableLiveData<Event<SignInEvent>>()
    override val shouldShowNotificationsPrefAction = MutableLiveData<Event<Boolean>>()

    var injectIsSignedIn = true
    var injectIsRegistered = false
    var signInRequestsEmitted = 0
    var signOutRequestsEmitted = 0

    override fun isSignedIn(): Boolean = injectIsSignedIn

    override fun observeSignedInUser() = TODO("Not implemented")

    override fun observeRegisteredUser() = MutableLiveData<Boolean>().apply {
        value = injectIsSignedIn
    }

    override fun isRegistered(): Boolean = injectIsRegistered

    override fun emitSignInRequest() {
        signInRequestsEmitted++
    }

    override fun emitSignOutRequest() {
        signOutRequestsEmitted++
    }

    override fun getUserId(): String? {
        return currentUserInfo.value?.getUid()
    }

    fun loadUser(id: String) {
        val mockUser = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn(id)
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
            on { this@on.isRegistered() }.doReturn(injectIsRegistered)
            on { isRegistrationDataReady() }.doReturn(true)
        }
        currentUserInfo.postValue(mockUser)
    }
}
