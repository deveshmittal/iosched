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

package app.traceindia.covid.client.android.ui.theme

import androidx.lifecycle.LiveData
import app.traceindia.covid.client.android.model.Theme
import app.traceindia.covid.client.android.shared.domain.settings.GetThemeUseCase
import app.traceindia.covid.client.android.shared.domain.settings.ObserveThemeModeUseCase
import app.traceindia.covid.client.android.shared.result.Result.Success
import app.traceindia.covid.client.android.shared.util.map
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Interface to implement activity theming via a ViewModel.
 *
 * You can inject a implementation of this via Dagger2, then use the implementation as an interface
 * delegate to add the functionality without writing any code
 *
 * Example usage:
 * ```
 * class MyViewModel @Inject constructor(
 *     themedActivityDelegate: ThemedActivityDelegate
 * ) : ViewModel(), ThemedActivityDelegate by themedActivityDelegate {
 * ```
 */
interface ThemedActivityDelegate {
    /**
     * Allows observing of the current theme
     */
    val theme: LiveData<Theme>

    /**
     * Allows querying of the current theme synchronously
     */
    val currentTheme: Theme
}

class ThemedActivityDelegateImpl @Inject constructor(
    private val observeThemeUseCase: ObserveThemeModeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ThemedActivityDelegate {
    override val theme: LiveData<Theme> by lazy(NONE) {
        observeThemeUseCase.observe().map {
            if (it is Success) it.data else Theme.SYSTEM
        }
    }

    override val currentTheme: Theme
        get() = getThemeUseCase.executeNow(Unit).let {
            if (it is Success) it.data else Theme.SYSTEM
        }

    init {
        // Observe updates in dark mode setting
        observeThemeUseCase.execute(Unit)
    }
}
