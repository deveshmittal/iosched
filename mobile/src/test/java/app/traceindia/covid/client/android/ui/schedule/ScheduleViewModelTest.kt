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

@file:Suppress("FunctionName")

package app.traceindia.covid.client.android.ui.schedule

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.ConferenceData
import app.traceindia.covid.client.android.model.MobileTestData
import app.traceindia.covid.client.android.model.TestDataRepository
import app.traceindia.covid.client.android.model.TestDataSource
import app.traceindia.covid.client.android.shared.analytics.AnalyticsHelper
import app.traceindia.covid.client.android.shared.data.ConferenceDataRepository
import app.traceindia.covid.client.android.shared.data.ConferenceDataSource
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.signin.AuthenticatedUserInfoBasic
import app.traceindia.covid.client.android.shared.data.signin.datasources.AuthStateUserDataSource
import app.traceindia.covid.client.android.shared.data.signin.datasources.RegisteredUserDataSource
import app.traceindia.covid.client.android.shared.data.tag.TagRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.UserEventDataSource
import app.traceindia.covid.client.android.shared.data.userevent.UserEventMessage
import app.traceindia.covid.client.android.shared.data.userevent.UserEventMessageChangeType
import app.traceindia.covid.client.android.shared.data.userevent.UserEventsResult
import app.traceindia.covid.client.android.shared.domain.RefreshConferenceDataUseCase
import app.traceindia.covid.client.android.shared.domain.auth.ObserveUserAuthStateUseCase
import app.traceindia.covid.client.android.shared.domain.prefs.LoadSelectedFiltersUseCase
import app.traceindia.covid.client.android.shared.domain.prefs.SaveSelectedFiltersUseCase
import app.traceindia.covid.client.android.shared.domain.prefs.ScheduleUiHintsShownUseCase
import app.traceindia.covid.client.android.shared.domain.sessions.LoadFilteredUserSessionsUseCase
import app.traceindia.covid.client.android.shared.domain.sessions.ObserveConferenceDataUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetTimeZoneUseCase
import app.traceindia.covid.client.android.shared.domain.users.StarEventAndNotifyUseCase
import app.traceindia.covid.client.android.shared.fcm.TopicSubscriber
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.schedule.UserSessionMatcher
import app.traceindia.covid.client.android.test.data.TestData
import app.traceindia.covid.client.android.test.util.SyncTaskExecutorRule
import app.traceindia.covid.client.android.test.util.fakes.FakeAnalyticsHelper
import app.traceindia.covid.client.android.test.util.fakes.FakeAppDatabase
import app.traceindia.covid.client.android.test.util.fakes.FakePreferenceStorage
import app.traceindia.covid.client.android.test.util.fakes.FakeSignInViewModelDelegate
import app.traceindia.covid.client.android.test.util.fakes.FakeStarEventUseCase
import app.traceindia.covid.client.android.ui.SnackbarMessage
import app.traceindia.covid.client.android.ui.messages.SnackbarMessageManager
import app.traceindia.covid.client.android.ui.schedule.filters.EventFilter
import app.traceindia.covid.client.android.ui.schedule.filters.LoadEventFiltersUseCase
import app.traceindia.covid.client.android.ui.signin.FirebaseSignInViewModelDelegate
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit tests for the [ScheduleViewModel].
 */
class ScheduleViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    @Test
    fun testDataIsLoaded_ObservablesUpdated() { // TODO: Very slow test (1s)
        // Create test use cases with test data
        val loadSessionsUseCase = LoadFilteredUserSessionsUseCase(
            DefaultSessionAndUserEventRepository(
                TestUserEventDataSource(), DefaultSessionRepository(TestDataRepository)
            )
        )
        val loadTagsUseCase = LoadEventFiltersUseCase(TagRepository(TestDataRepository))
        val signInDelegate = FakeSignInViewModelDelegate()

        // Create ViewModel with the use cases
        val viewModel = createScheduleViewModel(
            loadFilteredSessionsUseCase = loadSessionsUseCase,
            loadTagsUseCase = loadTagsUseCase,
            signInViewModelDelegate = signInDelegate
        )

        // Kick off the viewmodel by loading a user.
        signInDelegate.loadUser("test")

        // Observe viewmodel to load sessions
        viewModel.scheduleUiData.observeForever {}

        // Check that data were loaded correctly
        // Sessions
        assertEquals(
            TestData.userSessionList,
            LiveDataTestUtil.getValue(viewModel.scheduleUiData)?.list
        )
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLoading)!!)
        // Tags
        val loadedFilters = LiveDataTestUtil.getValue(viewModel.eventFilters)
        assertTrue(loadedFilters?.containsAll(MobileTestData.tagFiltersList) ?: false)
    }

    @Test
    fun testDataIsLoaded_Fails() {
        // Create ViewModel
        val viewModel = createScheduleViewModel()
        val errorMsg = LiveDataTestUtil.getValue(viewModel.errorMessage)
        assertTrue(errorMsg?.peekContent()?.isNotEmpty() ?: false)
    }

    /** Starring **/

    @Test
    fun testStarEvent() {
        // Create test use cases with test data
        val snackbarMessageManager = SnackbarMessageManager(FakePreferenceStorage())
        val viewModel = createScheduleViewModel(snackbarMessageManager = snackbarMessageManager)

        viewModel.onStarClicked(TestData.userSession0)

        val nextMessageEvent: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(snackbarMessageManager.observeNextMessage())
        val message = nextMessageEvent?.getContentIfNotHandled()
        assertThat(message?.messageId, `is`(equalTo(R.string.event_starred)))
        assertThat(message?.actionId, `is`(equalTo(R.string.dont_show)))

        // TODO: check changes in data source
    }

    @Test
    fun testUnstarEvent() {
        // Create test use cases with test data
        val snackbarMessageManager = SnackbarMessageManager(FakePreferenceStorage())
        val viewModel = createScheduleViewModel(snackbarMessageManager = snackbarMessageManager)

        viewModel.onStarClicked(TestData.userSession1)

        val nextMessageEvent: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(snackbarMessageManager.observeNextMessage())
        val message = nextMessageEvent?.getContentIfNotHandled()
        assertThat(message?.messageId, `is`(equalTo(R.string.event_unstarred)))
        assertThat(message?.actionId, `is`(equalTo(R.string.dont_show)))
    }

    @Test
    fun testStar_notLoggedInUser() {
        // Create test use cases with test data
        val signInDelegate = FakeSignInViewModelDelegate()
        signInDelegate.injectIsSignedIn = false

        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInDelegate)

        viewModel.onStarClicked(TestData.userSession1)

        val starEvent: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(viewModel.snackBarMessage)
        // TODO change with actual resource used
        assertThat(
            starEvent?.getContentIfNotHandled()?.messageId,
            `is`(not(equalTo(R.string.reservation_request_succeeded)))
        )

        // Verify that the sign in dialog was triggered
        val signInEvent = LiveDataTestUtil.getValue(viewModel.navigateToSignInDialogAction)
        assertNotNull(signInEvent?.getContentIfNotHandled())
    }

    /** New reservation / waitlist **/

    @Test
    fun reservationReceived() {
        // Create test use cases with test data
        val userEventsResult: MutableLiveData<UserEventsResult> = MutableLiveData()
        val source = TestUserEventDataSource(userEventsResult)
        val loadSessionsUseCase = createTestLoadUserSessionsByDayUseCase(source)
        val signInDelegate = FakeSignInViewModelDelegate()
        val snackbarMessageManager = SnackbarMessageManager(FakePreferenceStorage())
        val viewModel = createScheduleViewModel(
            loadFilteredSessionsUseCase = loadSessionsUseCase,
            signInViewModelDelegate = signInDelegate,
            snackbarMessageManager = snackbarMessageManager
        )

        // Kick off the viewmodel by loading a user.
        signInDelegate.loadUser("test")

        // Observe viewmodel to load sessions
        viewModel.scheduleUiData.observeForever {}

        // Observe snackbar so messages are received
        viewModel.snackBarMessage.observeForever { }

        // A session goes from not-reserved to reserved
        val oldValue = LiveDataTestUtil.getValue(userEventsResult)
        val newValue = oldValue!!.copy(
            userEventsMessage = UserEventMessage(
                UserEventMessageChangeType.CHANGES_IN_RESERVATIONS
            )
        )

        userEventsResult.postValue(newValue)

        val reservationMessage: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(snackbarMessageManager.observeNextMessage())
        assertThat(
            reservationMessage?.getContentIfNotHandled()?.messageId,
            `is`(equalTo(R.string.reservation_new))
        )
    }

    @Test
    fun waitlistReceived() {
        // Create test use cases with test data
        val userEventsResult = MutableLiveData<UserEventsResult>()
        val source = TestUserEventDataSource(userEventsResult)
        val loadSessionsUseCase = createTestLoadUserSessionsByDayUseCase(source)
        val signInDelegate = FakeSignInViewModelDelegate()
        val snackbarMessageManager = SnackbarMessageManager(FakePreferenceStorage())
        val viewModel = createScheduleViewModel(
            loadFilteredSessionsUseCase = loadSessionsUseCase,
            signInViewModelDelegate = signInDelegate,
            snackbarMessageManager = snackbarMessageManager
        )

        // Kick off the viewmodel by loading a user.
        signInDelegate.loadUser("test")

        // Observe viewmodel to load sessions
        viewModel.scheduleUiData.observeForever {}

        // Observe snackbar so messages are received
        viewModel.snackBarMessage.observeForever { }

        // A session goes from not-reserved to reserved
        val oldValue = LiveDataTestUtil.getValue(userEventsResult)
        val newValue = oldValue!!.copy(
            userEventsMessage = UserEventMessage(UserEventMessageChangeType.CHANGES_IN_WAITLIST)
        )

        userEventsResult.postValue(newValue)

        val waitlistMessage: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(snackbarMessageManager.observeNextMessage())
        assertThat(
            waitlistMessage?.getContentIfNotHandled()?.messageId,
            `is`(equalTo(R.string.waitlist_new))
        )
    }

    @Test
    fun noLoggedInUser_showsReservationButton() {
        // Given no logged in user
        val noFirebaseUser = null

        // Create ViewModel
        val observableFirebaseUserUseCase =
            FakeObserveUserAuthStateUseCase(
                user = Result.Success(noFirebaseUser),
                isRegistered = Result.Success(false)
            )
        val signInViewModelComponent = FirebaseSignInViewModelDelegate(
            observableFirebaseUserUseCase,
            mock {})

        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelComponent)

        // Check that reservation buttons are shown
        assertEquals(true, LiveDataTestUtil.getValue(viewModel.showReservations))
    }

    @Test
    fun loggedInUser_registered_showsReservationButton() {
        // Given a logged in user
        val mockUser = mock<AuthenticatedUserInfoBasic> {
            on { isSignedIn() }.doReturn(true)
        }

        // Who is registered
        val observableFirebaseUserUseCase =
            FakeObserveUserAuthStateUseCase(
                user = Result.Success(mockUser),
                isRegistered = Result.Success(true)
            )
        val signInViewModelComponent = FirebaseSignInViewModelDelegate(
            observableFirebaseUserUseCase,
            mock {})

        // Create ViewModel
        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelComponent)

        // Check that reservation buttons are shown
        assertEquals(true, LiveDataTestUtil.getValue(viewModel.showReservations))
    }

    @Test
    fun loggedInUser_notRegistered_hidesReservationButton() {
        // Given a logged in user
        val mockUser = mock<AuthenticatedUserInfoBasic> {
            on { isSignedIn() }.doReturn(true)
        }

        // Who isn't registered
        val observableFirebaseUserUseCase =
            FakeObserveUserAuthStateUseCase(
                user = Result.Success(mockUser),
                isRegistered = Result.Success(false)
            )
        val signInViewModelComponent = FirebaseSignInViewModelDelegate(
            observableFirebaseUserUseCase,
            mock {})

        // Create ViewModel
        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelComponent)

        // Check that *no* reservation buttons are shown
        assertEquals(false, LiveDataTestUtil.getValue(viewModel.showReservations))
    }

    @Test
    fun scheduleHints_notShown_on_launch() {
        val viewModel = createScheduleViewModel()

        val event = LiveDataTestUtil.getValue(viewModel.scheduleUiHintsShown)
        assertEquals(event?.getContentIfNotHandled(), false)
    }

    @Test
    fun swipeRefresh_refreshesRemoteConfData() {
        // Given a view model with a mocked remote data source
        val remoteDataSource = mock<ConferenceDataSource> {}
        val viewModel = createScheduleViewModel(
            refreshConferenceDataUseCase = RefreshConferenceDataUseCase(
                ConferenceDataRepository(
                    remoteDataSource = remoteDataSource,
                    boostrapDataSource = TestDataSource,
                    appDatabase = FakeAppDatabase()
                )
            )
        )

        // When swipe refresh is called
        viewModel.onSwipeRefresh()

        // Then the remote data source attempts to fetch new data
        verify(remoteDataSource).getRemoteConferenceData()

        // And the swipe refreshing status is set to false
        assertEquals(false, LiveDataTestUtil.getValue(viewModel.swipeRefreshing))
    }

    @Test
    fun newDataFromConfRepo_scheduleUpdated() {
        val repo = ConferenceDataRepository(
            remoteDataSource = TestConfDataSourceSession0(),
            boostrapDataSource = BootstrapDataSourceSession3(),
            appDatabase = FakeAppDatabase()
        )

        val loadUserSessionsByDayUseCase = createTestLoadUserSessionsByDayUseCase(
            conferenceDataRepo = repo
        )
        val viewModel = createScheduleViewModel(
            loadFilteredSessionsUseCase = loadUserSessionsByDayUseCase,
            observeConferenceDataUseCase = ObserveConferenceDataUseCase(repo)
        )

        // Observe viewmodel to load sessions
        viewModel.scheduleUiData.observeForever {}

        // Trigger a refresh on the repo
        repo.refreshCacheWithRemoteConferenceData()

        // The new value should be present
        val newValue = LiveDataTestUtil.getValue(viewModel.scheduleUiData)

        assertThat(
            newValue?.list?.first()?.session,
            `is`(IsEqual.equalTo(TestData.session0))
        )
    }

    private fun createScheduleViewModel(
        loadFilteredSessionsUseCase: LoadFilteredUserSessionsUseCase =
            createTestLoadUserSessionsByDayUseCase(),
        loadTagsUseCase: LoadEventFiltersUseCase = createEventFiltersExceptionUseCase(),
        signInViewModelDelegate: SignInViewModelDelegate = createSignInViewModelDelegate(),
        starEventUseCase: StarEventAndNotifyUseCase = createStarEventUseCase(),
        snackbarMessageManager: SnackbarMessageManager = SnackbarMessageManager(
            FakePreferenceStorage()
        ),
        scheduleUiHintsShownUseCase: ScheduleUiHintsShownUseCase =
            FakeScheduleUiHintsShownUseCase(),
        getTimeZoneUseCase: GetTimeZoneUseCase = createGetTimeZoneUseCase(),
        topicSubscriber: TopicSubscriber = mock {},
        refreshConferenceDataUseCase: RefreshConferenceDataUseCase =
            RefreshConferenceDataUseCase(TestDataRepository),
        observeConferenceDataUseCase: ObserveConferenceDataUseCase =
            ObserveConferenceDataUseCase(TestDataRepository),
        loadSelectedFiltersUseCase: LoadSelectedFiltersUseCase =
            LoadSelectedFiltersUseCase(FakePreferenceStorage()),
        saveSelectedFiltersUseCase: SaveSelectedFiltersUseCase =
            SaveSelectedFiltersUseCase(FakePreferenceStorage()),
        analyticsHelper: AnalyticsHelper = FakeAnalyticsHelper()
    ): ScheduleViewModel {
        return ScheduleViewModel(
            loadFilteredUserSessionsUseCase = loadFilteredSessionsUseCase,
            loadEventFiltersUseCase = loadTagsUseCase,
            signInViewModelDelegate = signInViewModelDelegate,
            starEventUseCase = starEventUseCase,
            scheduleUiHintsShownUseCase = scheduleUiHintsShownUseCase,
            topicSubscriber = topicSubscriber,
            snackbarMessageManager = snackbarMessageManager,
            getTimeZoneUseCase = getTimeZoneUseCase,
            refreshConferenceDataUseCase = refreshConferenceDataUseCase,
            observeConferenceDataUseCase = observeConferenceDataUseCase,
            loadSelectedFiltersUseCase = loadSelectedFiltersUseCase,
            saveSelectedFiltersUseCase = saveSelectedFiltersUseCase,
            analyticsHelper = analyticsHelper
        )
    }

    /**
     * Creates a test [LoadFilteredUserSessionsUseCase].
     */
    private fun createTestLoadUserSessionsByDayUseCase(
        userEventDataSource: UserEventDataSource = TestUserEventDataSource(),
        conferenceDataRepo: ConferenceDataRepository = TestDataRepository
    ): LoadFilteredUserSessionsUseCase {
        val sessionRepository = DefaultSessionRepository(conferenceDataRepo)
        val userEventRepository = DefaultSessionAndUserEventRepository(
            userEventDataSource, sessionRepository
        )

        return LoadFilteredUserSessionsUseCase(userEventRepository)
    }

    /**
     * Creates a use case that throws an exception.
     */
    private fun createEventFiltersExceptionUseCase(): LoadEventFiltersUseCase {
        return object : LoadEventFiltersUseCase(TagRepository(TestDataRepository)) {
            override fun execute(parameters: UserSessionMatcher): List<EventFilter> {
                throw Exception("Testing exception")
            }
        }
    }

    private fun createSignInViewModelDelegate() = FakeSignInViewModelDelegate()

    private fun createStarEventUseCase() = FakeStarEventUseCase()

    private fun createGetTimeZoneUseCase() =
        object : GetTimeZoneUseCase(FakePreferenceStorage()) {}
}

class TestRegisteredUserDataSource(private val isRegistered: Result<Boolean?>) :
    RegisteredUserDataSource {
    override fun listenToUserChanges(userId: String) {}

    override fun observeResult(): LiveData<Result<Boolean?>?> {
        return MutableLiveData<Result<Boolean?>?>().apply { value = isRegistered }
    }

    override fun setAnonymousValue() {}
}

class TestAuthStateUserDataSource(
    private val user: Result<AuthenticatedUserInfoBasic?>?
) : AuthStateUserDataSource {
    override fun startListening() {}

    override fun getBasicUserInfo(): LiveData<Result<AuthenticatedUserInfoBasic?>> =
        MutableLiveData<Result<AuthenticatedUserInfoBasic?>>().apply { value = user }

    override fun clearListener() {}
}

class FakeObserveUserAuthStateUseCase(
    user: Result<AuthenticatedUserInfoBasic?>?,
    isRegistered: Result<Boolean?>
) : ObserveUserAuthStateUseCase(
    TestRegisteredUserDataSource(isRegistered),
    TestAuthStateUserDataSource(user),
    mock {}
)

class FakeScheduleUiHintsShownUseCase : ScheduleUiHintsShownUseCase(
    preferenceStorage = FakePreferenceStorage()
)

class TestConfDataSourceSession0 : ConferenceDataSource {
    override fun getRemoteConferenceData(): ConferenceData? {
        return conferenceData
    }

    override fun getOfflineConferenceData(): ConferenceData? {
        return conferenceData
    }

    private val conferenceData = ConferenceData(
        sessions = listOf(TestData.session0),
        speakers = listOf(TestData.speaker1),
        rooms = emptyList(),
        codelabs = emptyList(),
        tags = listOf(TestData.androidTag, TestData.webTag),
        version = 42
    )
}

class BootstrapDataSourceSession3 : ConferenceDataSource {
    override fun getRemoteConferenceData(): ConferenceData? {
        throw NotImplementedError() // Not used
    }

    override fun getOfflineConferenceData(): ConferenceData? {
        return ConferenceData(
            sessions = listOf(TestData.session3),
            speakers = listOf(TestData.speaker1),
            rooms = emptyList(),
            codelabs = emptyList(),
            tags = listOf(TestData.androidTag, TestData.webTag),
            version = 42
        )
    }
}
