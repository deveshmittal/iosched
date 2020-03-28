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

package app.traceindia.covid.client.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.model.Theme
import app.traceindia.covid.client.android.shared.domain.prefs.NotificationsPrefSaveActionUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetAnalyticsSettingUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetAvailableThemesUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetThemeUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetNotificationsSettingUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetTimeZoneUseCase
import app.traceindia.covid.client.android.shared.domain.settings.SetAnalyticsSettingUseCase
import app.traceindia.covid.client.android.shared.domain.settings.SetThemeUseCase
import app.traceindia.covid.client.android.shared.domain.settings.SetTimeZoneUseCase
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.result.Result.Success
import app.traceindia.covid.client.android.shared.util.map
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val setTimeZoneUseCase: SetTimeZoneUseCase,
    getTimeZoneUseCase: GetTimeZoneUseCase,
    val notificationsPrefSaveActionUseCase: NotificationsPrefSaveActionUseCase,
    getNotificationsSettingUseCase: GetNotificationsSettingUseCase,
    val setAnalyticsSettingUseCase: SetAnalyticsSettingUseCase,
    getAnalyticsSettingUseCase: GetAnalyticsSettingUseCase,
    val setThemeUseCase: SetThemeUseCase,
    getThemeUseCase: GetThemeUseCase,
    getAvailableThemesUseCase: GetAvailableThemesUseCase
) : ViewModel() {

    // Time Zone setting
    private val preferConferenceTimeZoneResult = MutableLiveData<Result<Boolean>>()
    val preferConferenceTimeZone: LiveData<Boolean>

    // Notifications setting
    private val enableNotificationsResult = MutableLiveData<Result<Boolean>>()
    val enableNotifications: LiveData<Boolean>

    // Analytics setting
    private val sendUsageStatisticsResult = MutableLiveData<Result<Boolean>>()
    val sendUsageStatistics: LiveData<Boolean>

    // Theme setting
    private val themeResult = MutableLiveData<Result<Theme>>()
    val theme: LiveData<Theme>

    // Theme setting
    private val availableThemesResult = MutableLiveData<Result<List<Theme>>>()
    val availableThemes: LiveData<List<Theme>>

    private val _navigateToThemeSelector = MutableLiveData<Event<Unit>>()
    val navigateToThemeSelector: LiveData<Event<Unit>>
        get() = _navigateToThemeSelector

    init {
        getTimeZoneUseCase(Unit, preferConferenceTimeZoneResult)
        preferConferenceTimeZone = preferConferenceTimeZoneResult.map {
            (it as? Success<Boolean>)?.data ?: true
        }

        getAnalyticsSettingUseCase(Unit, sendUsageStatisticsResult)
        sendUsageStatistics = sendUsageStatisticsResult.map {
            (it as? Success<Boolean>)?.data ?: false
        }

        getNotificationsSettingUseCase(Unit, enableNotificationsResult)
        enableNotifications = enableNotificationsResult.map {
            (it as? Success<Boolean>)?.data ?: false
        }

        getThemeUseCase(Unit, themeResult)
        theme = themeResult.map {
            (it as? Success<Theme>)?.data ?: Theme.SYSTEM
        }

        getAvailableThemesUseCase(Unit, availableThemesResult)
        availableThemes = availableThemesResult.map {
            (it as? Success<List<Theme>>)?.data ?: emptyList()
        }
    }

    fun toggleTimeZone(checked: Boolean) {
        setTimeZoneUseCase(checked, preferConferenceTimeZoneResult)
    }

    fun toggleSendUsageStatistics(checked: Boolean) {
        setAnalyticsSettingUseCase(checked, sendUsageStatisticsResult)
    }

    fun toggleEnableNotifications(checked: Boolean) {
        notificationsPrefSaveActionUseCase(checked, enableNotificationsResult)
    }

    fun setTheme(theme: Theme) {
        setThemeUseCase(theme)
    }

    fun onThemeSettingClicked() {
        _navigateToThemeSelector.value = Event(Unit)
    }
}
