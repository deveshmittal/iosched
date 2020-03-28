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

import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.ui.SnackbarMessage
import app.traceindia.covid.client.android.ui.sessioncommon.EventActionsViewModelDelegate

class FakeEventActionsViewModelDelegate : EventActionsViewModelDelegate {

    override val navigateToEventAction = MutableLiveData<Event<SessionId>>()
    override val navigateToSignInDialogAction = MutableLiveData<Event<Unit>>()
    override val snackBarMessage = MutableLiveData<Event<SnackbarMessage>>()

    override fun openEventDetail(id: SessionId) = TODO("Not implemented")

    override fun onStarClicked(userSession: UserSession) = TODO("Not implemented")
}
