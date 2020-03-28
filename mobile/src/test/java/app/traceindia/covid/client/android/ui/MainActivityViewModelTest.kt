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

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.functions.FirebaseFunctions
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.TestDataRepository
import app.traceindia.covid.client.android.shared.data.ar.DefaultArDebugFlagEndpoint
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.ar.LoadArDebugFlagUseCase
import app.traceindia.covid.client.android.shared.domain.sessions.LoadPinnedSessionsJsonUseCase
import app.traceindia.covid.client.android.test.util.SyncTaskExecutorRule
import app.traceindia.covid.client.android.test.util.fakes.FakeSignInViewModelDelegate
import app.traceindia.covid.client.android.test.util.fakes.FakeThemedActivityDelegate
import app.traceindia.covid.client.android.ui.schedule.TestUserEventDataSource
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import app.traceindia.covid.client.android.ui.theme.ThemedActivityDelegate
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class MainActivityViewModelTest {
    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    private fun createMainActivityViewModel(
        signInViewModelDelegate: SignInViewModelDelegate = FakeSignInViewModelDelegate(),
        themedActivityDelegate: ThemedActivityDelegate = FakeThemedActivityDelegate()
    ): MainActivityViewModel {
        return MainActivityViewModel(
            signInViewModelDelegate = signInViewModelDelegate,
            themedActivityDelegate = themedActivityDelegate,
            loadPinnedSessionsUseCase = LoadPinnedSessionsJsonUseCase(
                DefaultSessionAndUserEventRepository(
                    TestUserEventDataSource(), DefaultSessionRepository(TestDataRepository)
                )
            ),
            loadArDebugFlagUseCase = LoadArDebugFlagUseCase(
                DefaultArDebugFlagEndpoint(
                    mock(FirebaseFunctions::class.java))),
            context = mock(Context::class.java)
        )
    }

    @Test
    fun notLoggedIn_profileClicked_showsSignInDialog() {
        // Given a ViewModel with a signed out user
        val signInViewModelDelegate = FakeSignInViewModelDelegate().apply {
            injectIsSignedIn = false
        }
        val viewModel =
            createMainActivityViewModel(signInViewModelDelegate = signInViewModelDelegate)

        // When profile is clicked
        viewModel.onProfileClicked()

        // Then the sign in dialog should be shown
        val signOutEvent = LiveDataTestUtil.getValue(viewModel.navigateToSignInDialogAction)
        assertThat(signOutEvent?.getContentIfNotHandled(), `is`(notNullValue()))
    }

    @Test
    fun loggedIn_profileClicked_showsSignOutDialog() {
        // Given a ViewModel with a signed in user
        val signInViewModelDelegate = FakeSignInViewModelDelegate().apply {
            injectIsSignedIn = true
        }
        val viewModel =
            createMainActivityViewModel(signInViewModelDelegate = signInViewModelDelegate)

        // When profile is clicked
        viewModel.onProfileClicked()

        // Then the sign out dialog should be shown
        val signOutEvent = LiveDataTestUtil.getValue(viewModel.navigateToSignOutDialogAction)
        assertThat(signOutEvent?.getContentIfNotHandled(), `is`(notNullValue()))
    }
}
