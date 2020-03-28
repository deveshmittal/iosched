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

package app.traceindia.covid.client.android.tests.ui

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.R.id
import app.traceindia.covid.client.android.tests.SetPreferencesRule
import app.traceindia.covid.client.android.tests.SyncTaskExecutorRule
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso tests for Settings screen
 */
@RunWith(AndroidJUnit4::class)
class SettingsTest {

    @get:Rule
    var activityRule = MainActivityTestRule(R.id.navigation_settings)

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    // Sets the preferences so no welcome screens are shown
    @get:Rule
    var preferencesRule = SetPreferencesRule()

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun settings_basicViewsDisplayed() {
        // Title
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(id.toolbar))))
            .check(matches(withText(R.string.settings_title)))
        // Preference toggle
        onView(withText(resources.getString(R.string.settings_enable_notifications)))
            .check(matches(isDisplayed()))
        // About label
        onView(withText(resources.getString(R.string.about_title)))
            .check(matches(isDisplayed()))
    }
}
