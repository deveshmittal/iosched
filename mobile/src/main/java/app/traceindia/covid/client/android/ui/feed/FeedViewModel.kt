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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.model.Announcement
import app.traceindia.covid.client.android.model.Moment
import app.traceindia.covid.client.android.model.SessionId
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.shared.analytics.AnalyticsActions
import app.traceindia.covid.client.android.shared.analytics.AnalyticsHelper
import app.traceindia.covid.client.android.shared.data.signin.AuthenticatedUserInfo
import app.traceindia.covid.client.android.shared.domain.feed.LoadAnnouncementsUseCase
import app.traceindia.covid.client.android.shared.domain.feed.LoadCurrentMomentUseCase
import app.traceindia.covid.client.android.shared.domain.sessions.LoadFilteredUserSessionsParameters
import app.traceindia.covid.client.android.shared.domain.sessions.LoadFilteredUserSessionsResult
import app.traceindia.covid.client.android.shared.domain.sessions.LoadFilteredUserSessionsUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetTimeZoneUseCase
import app.traceindia.covid.client.android.shared.result.Event
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.shared.result.Result.Loading
import app.traceindia.covid.client.android.shared.result.successOr
import app.traceindia.covid.client.android.shared.schedule.UserSessionMatcher
import app.traceindia.covid.client.android.shared.time.TimeProvider
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.shared.util.map
import app.traceindia.covid.client.android.ui.SectionHeader
import app.traceindia.covid.client.android.ui.SnackbarMessage
import app.traceindia.covid.client.android.ui.sessioncommon.EventActions
import app.traceindia.covid.client.android.ui.signin.SignInViewModelDelegate
import app.traceindia.covid.client.android.ui.theme.ThemedActivityDelegate
import app.traceindia.covid.client.android.util.ConferenceStateLiveData
import app.traceindia.covid.client.android.util.ConferenceState.ENDED
import app.traceindia.covid.client.android.util.ConferenceState.UPCOMING
import app.traceindia.covid.client.android.util.combine
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

/**
 * Loads data and exposes it to the view.
 * By annotating the constructor with [@Inject], Dagger will use that constructor when needing to
 * create the object, so defining a [@Provides] method for this class won't be needed.
 */
class FeedViewModel @Inject constructor(
    private val loadCurrentMomentUseCase: LoadCurrentMomentUseCase,
    loadAnnouncementsUseCase: LoadAnnouncementsUseCase,
    private val loadFilteredUserSessionsUseCase: LoadFilteredUserSessionsUseCase,
    getTimeZoneUseCase: GetTimeZoneUseCase,
    conferenceStateLiveData: ConferenceStateLiveData,
    private val timeProvider: TimeProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val signInViewModelDelegate: SignInViewModelDelegate,
    themedActivityDelegate: ThemedActivityDelegate
) : ViewModel(),
    FeedEventListener,
    ThemedActivityDelegate by themedActivityDelegate,
    SignInViewModelDelegate by signInViewModelDelegate {

    companion object {
        // Show at max 10 sessions in the horizontal sessions list as user can click on
        // View All sessions and go to schedule to view the full list
        private const val MAX_SESSIONS = 10

        // Indicates there is no header to show at the current time. We need this sentinel value
        // because our LiveData.combine extension functions interpret null values to mean the
        // LiveData has not returned a result, but null for the current Moment is valid.
        private object NoHeader

        // Indicates there is no sessions related display on the home screen as the conference is
        // over.
        private object NoSessionsContainer
    }

    val errorMessage: LiveData<Event<String>>

    val feed: LiveData<List<Any>>

    val snackBarMessage: LiveData<Event<SnackbarMessage>>

    private val loadSessionsResult: MediatorLiveData<Result<LoadFilteredUserSessionsResult>>

    private val loadAnnouncementsResult = MutableLiveData<Result<List<Announcement>>>()

    private val currentMomentResult = MediatorLiveData<Result<Moment?>>()

    private val _navigateToSessionAction = MutableLiveData<Event<String>>()
    val navigateToSessionAction: LiveData<Event<String>>
        get() = _navigateToSessionAction

    private val _navigateToMapAction = MutableLiveData<Event<Moment>>()
    val navigateToMapAction: LiveData<Event<Moment>>
        get() = _navigateToMapAction

    private val _openSignInDialogAction = MutableLiveData<Event<Unit>>()
    val openSignInDialogAction: LiveData<Event<Unit>>
        get() = _openSignInDialogAction

    private val _openLiveStreamAction = MutableLiveData<Event<String>>()
    val openLiveStreamAction: LiveData<Event<String>>
        get() = _openLiveStreamAction

    private val _navigateToScheduleAction = MutableLiveData<Event<Boolean>>()
    val navigateToScheduleAction: LiveData<Event<Boolean>>
        get() = _navigateToScheduleAction

    private val preferConferenceTimeZoneResult = MutableLiveData<Result<Boolean>>()
    val timeZoneId: LiveData<ZoneId>

    init {
        timeZoneId = preferConferenceTimeZoneResult.map {
            if (it.successOr(true)) TimeUtils.CONFERENCE_TIMEZONE else ZoneId.systemDefault()
        }

        loadSessionsResult = loadFilteredUserSessionsUseCase.observe()
        loadSessionsResult.addSource(signInViewModelDelegate.currentUserInfo) {
            refreshSessions()
        }

        val sessionContainerLiveData =
            signInViewModelDelegate.currentUserInfo
                .combine(
                    loadSessionsResult,
                    timeZoneId
                ) { userInfo, sessions, timeZone ->
                    createFeedSessionsContainer(userInfo, sessions, timeZone)
                }
                // Further combine with conferenceState and decide if the
                // feedSessionsContainer should be shown or not.
                .combine(conferenceStateLiveData) { sessionsContainer, conferenceState ->
                    if (conferenceState == ENDED)
                        NoSessionsContainer
                    else
                        sessionsContainer
                }

        loadAnnouncementsUseCase(timeProvider.now(), loadAnnouncementsResult)
        val announcements: LiveData<List<Any>> = loadAnnouncementsResult.map {
            if (it is Loading) {
                listOf(LoadingIndicator)
            } else {
                val items = it.successOr(emptyList())
                if (items.isNotEmpty()) items else listOf(AnnouncementsEmpty)
            }
        }

        currentMomentResult.addSource(conferenceStateLiveData) {
            // This will change to the first moment when the Keynote starts
            loadCurrentMomentUseCase(timeProvider.now(), currentMomentResult)
        }

        val currentFeedHeader = conferenceStateLiveData.combine(
            currentMomentResult
        ) { conferenceStarted, momentResult ->
            if (conferenceStarted == UPCOMING) {
                CountdownItem
            } else {
                // Use case can return null even on success, so replace nulls with a sentinel
                momentResult.successOr(null) ?: NoHeader
            }
        }

        // Compose feed
        feed = currentFeedHeader.combine(
            sessionContainerLiveData,
            announcements
        ) { feedHeader, sessionContainer, announcementItems ->
            val feedItems = mutableListOf<Any>()
            if (feedHeader != NoHeader) {
                feedItems.add(feedHeader)
            }
            if (sessionContainer != NoSessionsContainer) {
                feedItems.add(sessionContainer)
            }
            feedItems.plus(SectionHeader(R.string.feed_announcement_title))
                .plus(announcementItems)
        }

        errorMessage = loadAnnouncementsResult.map {
            Event(content = (it as? Result.Error)?.exception?.message ?: "")
        }

        // Show an error message if the feed could not be loaded.
        snackBarMessage = MediatorLiveData()
        snackBarMessage.addSource(loadAnnouncementsResult) {
            if (it is Result.Error) {
                snackBarMessage.value =
                    Event(
                        SnackbarMessage(
                            messageId = R.string.feed_loading_error,
                            longDuration = true
                        )
                    )
            }
        }

        getTimeZoneUseCase(Unit, preferConferenceTimeZoneResult)
    }

    private fun createFeedSessionsContainer(
        userInfo: AuthenticatedUserInfo?,
        sessionsResult: Result<LoadFilteredUserSessionsResult>,
        timeZoneId: ZoneId
    ): FeedSessions {
        val isSignedIn = userInfo?.isSignedIn() ?: false
        val isRegistered = userInfo?.isRegistered() ?: false
        val sessions = sessionsResult.successOr(null)?.userSessions ?: emptyList()
        val hasSessions = sessions.isEmpty()
        val now = ZonedDateTime.ofInstant(timeProvider.now(), timeZoneId)
        val upcomingSessions = sessions
            .filter { it.session.endTime.isAfter(now) }
            .take(MAX_SESSIONS)

        val username = userInfo?.getDisplayName()?.split(" ")?.get(0)
        val titleId = when {
            isSignedIn && !hasSessions -> R.string.feed_no_saved_events
            isSignedIn && isRegistered -> R.string.feed_upcoming_events
            isSignedIn -> R.string.feed_saved_events
            else -> R.string.title_schedule
        }
        val actionId = when {
            isSignedIn && hasSessions -> R.string.feed_view_your_schedule
            else -> R.string.feed_view_all_events
        }

        return FeedSessions(
            username = username,
            titleId = titleId,
            actionTextId = actionId,
            userSessions = upcomingSessions,
            timeZoneId = timeZoneId,
            isLoading = sessionsResult is Loading
        )
    }

    private fun refreshSessions() {
        val sessionMatcher = UserSessionMatcher()
        if (signInViewModelDelegate.isSignedIn()) {
            sessionMatcher.setShowPinnedEventsOnly(true)
        }
        loadFilteredUserSessionsUseCase.execute(
            LoadFilteredUserSessionsParameters(
                sessionMatcher,
                signInViewModelDelegate.getUserId()
            )
        )
    }

    override fun openEventDetail(id: SessionId) {
        analyticsHelper.logUiEvent("Home to event detail",
            AnalyticsActions.HOME_TO_SESSION_DETAIL)
        _navigateToSessionAction.value = Event(id)
    }

    override fun openSchedule(showOnlyPinnedSessions: Boolean) {
        analyticsHelper.logUiEvent("Home to Schedule", AnalyticsActions.HOME_TO_SCHEDULE)
        _navigateToScheduleAction.value = Event(showOnlyPinnedSessions)
    }

    override fun onStarClicked(userSession: UserSession) {
        TODO("not implemented")
    }

    override fun signIn() {
        analyticsHelper.logUiEvent("Home to Sign In", AnalyticsActions.HOME_TO_SIGN_IN)
        _openSignInDialogAction.value = Event(Unit)
    }

    override fun openMap(moment: Moment) {
        analyticsHelper.logUiEvent(moment.title.toString(), AnalyticsActions.HOME_TO_MAP)
        _navigateToMapAction.value = Event(moment)
    }

    override fun openLiveStream(liveStreamUrl: String) {
        analyticsHelper.logUiEvent(liveStreamUrl, AnalyticsActions.HOME_TO_LIVESTREAM)
        _openLiveStreamAction.value = Event(liveStreamUrl)
    }
}

interface FeedEventListener : EventActions {
    fun openSchedule(showOnlyPinnedSessions: Boolean)
    fun signIn()
    fun openMap(moment: Moment)
    fun openLiveStream(liveStreamUrl: String)
}
