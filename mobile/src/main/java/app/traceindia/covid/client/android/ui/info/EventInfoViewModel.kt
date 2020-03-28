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

package app.traceindia.covid.client.android.ui.info

import android.net.wifi.WifiConfiguration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.model.ConferenceWifiInfo
import app.traceindia.covid.client.android.shared.analytics.AnalyticsActions
import app.traceindia.covid.client.android.shared.analytics.AnalyticsHelper
import app.traceindia.covid.client.android.shared.domain.logistics.LoadWifiInfoUseCase
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.result.successOr
import app.traceindia.covid.client.android.shared.util.map
import app.traceindia.covid.client.android.ui.SnackbarMessage
import app.traceindia.covid.client.android.util.wifi.WifiInstaller
import javax.inject.Inject

class EventInfoViewModel @Inject constructor(
    loadWifiInfoUseCase: LoadWifiInfoUseCase,
    private val wifiInstaller: WifiInstaller,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    companion object {
        private const val ASSISTANT_APP_URL =
            "https://assistant.google.com/services/invoke/uid/0000009fca77b068"
    }

    private val _wifiConfig = MutableLiveData<Result<ConferenceWifiInfo>>()
    val wifiConfig: LiveData<ConferenceWifiInfo?>
    val showWifi: LiveData<Boolean>

    private val _snackbarMessage = MutableLiveData<Event<SnackbarMessage>>()
    val snackBarMessage: LiveData<Event<SnackbarMessage>>
        get() = _snackbarMessage

    private val _openUrlEvent = MutableLiveData<Event<String>>()
    val openUrlEvent: LiveData<Event<String>>
        get() = _openUrlEvent

    init {
        loadWifiInfoUseCase(Unit, _wifiConfig)
        wifiConfig = _wifiConfig.map {
            it.successOr(null)
        }
        showWifi = wifiConfig.map {
            it != null && it.ssid.isNotBlank() && it.password.isNotBlank()
        }
    }

    fun onWifiConnect() {
        val config = wifiConfig.value ?: return
        val success = wifiInstaller.installConferenceWifi(WifiConfiguration().apply {
            SSID = config.ssid
            preSharedKey = config.password
        })
        val snackbarMessage = if (success) {
            SnackbarMessage(R.string.wifi_install_success)
        } else {
            SnackbarMessage(
                messageId = R.string.wifi_install_clipboard_message, longDuration = true
            )
        }

        _snackbarMessage.postValue(Event(snackbarMessage))
        analyticsHelper.logUiEvent("Events", AnalyticsActions.WIFI_CONNECT)
    }

    fun onClickAssistantApp() {
        _openUrlEvent.value = Event(ASSISTANT_APP_URL)
        analyticsHelper.logUiEvent("Assistant App", AnalyticsActions.CLICK)
    }
}
