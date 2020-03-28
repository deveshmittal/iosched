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

package app.traceindia.covid.client.android.ui.feed

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.Announcement
import app.traceindia.covid.client.android.model.Moment
import app.traceindia.covid.client.android.model.TestDataRepository
import app.traceindia.covid.client.android.shared.data.feed.DefaultFeedRepository
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.domain.feed.LoadAnnouncementsUseCase
import app.traceindia.covid.client.android.shared.domain.feed.LoadCurrentMomentUseCase
import app.traceindia.covid.client.android.shared.domain.internal.IOSchedHandler
import app.traceindia.covid.client.android.shared.domain.sessions.LoadFilteredUserSessionsUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetTimeZoneUseCase
import app.traceindia.covid.client.android.shared.time.TimeProvider
import app.traceindia.covid.client.android.test.data.TestData
import app.traceindia.covid.client.android.test.util.SyncTaskExecutorRule
import app.traceindia.covid.client.android.test.util.fakes.FakeAnalyticsHelper
import app.traceindia.covid.client.android.test.util.fakes.FakePreferenceStorage
import app.traceindia.covid.client.android.test.util.fakes.FakeSignInViewModelDelegate
import app.traceindia.covid.client.android.test.util.fakes.FakeThemedActivityDelegate
import app.traceindia.covid.client.android.test.util.time.FixedTimeProvider
import app.traceindia.covid.client.android.ui.SectionHeader
import app.traceindia.covid.client.android.ui.schedule.TestUserEventDataSource
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import app.traceindia.covid.client.android.ui.theme.ThemedActivityDelegate
import app.traceindia.covid.client.android.util.ConferenceStateLiveData
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.Instant

/**
 * Unit tests for the [FeedViewModel]
 */
class FeedViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    private val fakeHandler = object : IOSchedHandler {
        override fun post(runnable: Runnable) = true

        override fun postDelayed(runnable: Runnable, millis: Long) = true

        override fun removeCallbacks(runnable: Runnable) {}
    }

    private val defaultFeedRepository =
        DefaultFeedRepository(TestAnnouncementDataSource, TestMomentDataSource)

    // Loads feed roughly during the Keynote time
    private val defaultTimeProvider =
        FixedTimeProvider(TestData.TestConferenceDays[0].start.plusHours(4).toInstant())

    @Test
    fun testDataIsLoaded_ObservablesUpdated() {
        // Create ViewModel with the use case and load the feed.
        val viewModel = createFeedViewModel()
        val feedObservable = LiveDataTestUtil.getValue(viewModel.feed)

        // Check that data was loaded correctly.
        // At the specified time, the Moment is relevant and there is one Announcement.
        // Add two more for the Sessions carousel and the "Announcements' heading.
        assertThat(feedObservable?.size, `is`(equalTo(4)))
        assertThat(feedObservable?.get(0) as? Moment, `is`(equalTo(TestData.moment1)))
        assertThat(feedObservable?.get(1), `is`(instanceOf(FeedSessions::class.java)))
        assertThat(feedObservable?.get(2), `is`(instanceOf(SectionHeader::class.java)))
        assertThat(feedObservable?.get(3) as? Announcement, `is`(equalTo(TestData.feedItem1)))
    }

    @Test
    fun testDataIsLoaded_Fails() {
        // Create ViewModel with a use case that returns an error
        val viewModel = createFeedViewModel(loadAnnouncementUseCase = FailingUseCase)

        // Verify that an error was caught
        val errorMessage = LiveDataTestUtil.getValue(viewModel.errorMessage)
        assertTrue(errorMessage?.peekContent()?.isNotEmpty() ?: false)
    }

    /**
     * Use case that always returns an error when executed.
     */
    object FailingUseCase : LoadAnnouncementsUseCase(
        DefaultFeedRepository(TestAnnouncementDataSource, TestMomentDataSource)
    ) {
        override fun execute(parameters: Instant): List<Announcement> {
            throw Exception("Error!")
        }
    }

    private fun createFeedViewModel(
        loadCurrentMomentUseCase: LoadCurrentMomentUseCase =
            LoadCurrentMomentUseCase(defaultFeedRepository),
        loadAnnouncementUseCase: LoadAnnouncementsUseCase =
            LoadAnnouncementsUseCase(defaultFeedRepository),
        loadFilteredSessionsUseCase: LoadFilteredUserSessionsUseCase =
            LoadFilteredUserSessionsUseCase(
                DefaultSessionAndUserEventRepository(
                    TestUserEventDataSource(), DefaultSessionRepository(TestDataRepository)
                )
            ),
        getTimeZoneUseCase: GetTimeZoneUseCase = GetTimeZoneUseCase(FakePreferenceStorage()),
        conferenceStateLiveData: ConferenceStateLiveData =
            ConferenceStateLiveData(fakeHandler, defaultTimeProvider),
        timeProvider: TimeProvider = defaultTimeProvider,
        signInViewModelDelegate: SignInViewModelDelegate = FakeSignInViewModelDelegate().apply {
            loadUser("123")
        },
        themedActivityDelegate: ThemedActivityDelegate = FakeThemedActivityDelegate()
    ): FeedViewModel {
        return FeedViewModel(
            loadCurrentMomentUseCase = loadCurrentMomentUseCase,
            loadAnnouncementsUseCase = loadAnnouncementUseCase,
            loadFilteredUserSessionsUseCase = loadFilteredSessionsUseCase,
            getTimeZoneUseCase = getTimeZoneUseCase,
            conferenceStateLiveData = conferenceStateLiveData,
            timeProvider = timeProvider,
            analyticsHelper = FakeAnalyticsHelper(),
            signInViewModelDelegate = signInViewModelDelegate,
            themedActivityDelegate = themedActivityDelegate
        )
    }
}
