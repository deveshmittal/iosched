/*
 * Copyright 2018 Google LLC
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

package app.traceindia.covid.client.android.shared.di

import app.traceindia.covid.client.android.shared.data.ConferenceDataRepository
import app.traceindia.covid.client.android.shared.data.ConferenceDataSource
import app.traceindia.covid.client.android.shared.data.FakeAnnouncementDataSource
import app.traceindia.covid.client.android.shared.data.FakeAppConfigDataSource
import app.traceindia.covid.client.android.shared.data.FakeConferenceDataSource
import app.traceindia.covid.client.android.shared.data.FakeFeedbackEndpoint
import app.traceindia.covid.client.android.shared.data.ar.ArDebugFlagEndpoint
import app.traceindia.covid.client.android.shared.data.ar.FakeArDebugFlagEndpoint
import app.traceindia.covid.client.android.shared.data.config.AppConfigDataSource
import app.traceindia.covid.client.android.shared.data.db.AppDatabase
import app.traceindia.covid.client.android.shared.data.feed.AnnouncementDataSource
import app.traceindia.covid.client.android.shared.data.feed.DefaultFeedRepository
import app.traceindia.covid.client.android.shared.data.feed.FakeMomentDataSource
import app.traceindia.covid.client.android.shared.data.feed.FeedRepository
import app.traceindia.covid.client.android.shared.data.feed.MomentDataSource
import app.traceindia.covid.client.android.shared.data.feedback.FeedbackEndpoint
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.session.SessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.FakeUserEventDataSource
import app.traceindia.covid.client.android.shared.data.userevent.SessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.UserEventDataSource
import app.traceindia.covid.client.android.shared.fcm.StagingTopicSubscriber
import app.traceindia.covid.client.android.shared.fcm.TopicSubscriber
import app.traceindia.covid.client.android.shared.time.DefaultTimeProvider
import app.traceindia.covid.client.android.shared.time.TimeProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/**
 * Module where classes created in the shared module are created.
 */
@Module
class SharedModule {

// Define the data source implementations that should be used. All data sources are singletons.

    @Singleton
    @Provides
    @Named("remoteConfDatasource")
    fun provideConferenceDataSource(): ConferenceDataSource {
        return FakeConferenceDataSource
    }

    @Singleton
    @Provides
    @Named("bootstrapConfDataSource")
    fun provideBootstrapRemoteSessionDataSource(): ConferenceDataSource {
        return FakeConferenceDataSource
    }

    @Singleton
    @Provides
    fun provideConferenceDataRepository(
        @Named("remoteConfDatasource") remoteDataSource: ConferenceDataSource,
        @Named("bootstrapConfDataSource") boostrapDataSource: ConferenceDataSource,
        appDatabase: AppDatabase
    ): ConferenceDataRepository {
        return ConferenceDataRepository(remoteDataSource, boostrapDataSource, appDatabase)
    }

    @Singleton
    @Provides
    fun provideSessionRepository(
        conferenceDataRepository: ConferenceDataRepository
    ): SessionRepository {
        return DefaultSessionRepository(conferenceDataRepository)
    }

    @Singleton
    @Provides
    fun provideUserEventDataSource(): UserEventDataSource {
        return FakeUserEventDataSource
    }

    @Singleton
    @Provides
    fun provideFeedbackEndpoint(): FeedbackEndpoint {
        return FakeFeedbackEndpoint
    }

    @Singleton
    @Provides
    fun provideSessionAndUserEventRepository(
        userEventDataSource: UserEventDataSource,
        sessionRepository: SessionRepository
    ): SessionAndUserEventRepository {
        return DefaultSessionAndUserEventRepository(
            userEventDataSource,
            sessionRepository
        )
    }

    @Singleton
    @Provides
    fun provideTopicSubscriber(): TopicSubscriber {
        return StagingTopicSubscriber()
    }

    @Singleton
    @Provides
    fun provideAppConfigDataSource(): AppConfigDataSource {
        return FakeAppConfigDataSource()
    }

    @Singleton
    @Provides
    fun provideTimeProvider(): TimeProvider {
        // TODO: Make the time configurable
        return DefaultTimeProvider
    }

    @Singleton
    @Provides
    fun provideAnnouncementDataSource(): AnnouncementDataSource {
        return FakeAnnouncementDataSource
    }

    @Singleton
    @Provides
    fun provideMomentDataSource(): MomentDataSource {
        return FakeMomentDataSource
    }

    @Singleton
    @Provides
    fun provideFeedRepository(
        announcementDataSource: AnnouncementDataSource,
        momentDataSource: MomentDataSource
    ): FeedRepository {
        return DefaultFeedRepository(announcementDataSource, momentDataSource)
    }

    @Singleton
    @Provides
    fun provideArDebugFlagEndpoint(): ArDebugFlagEndpoint {
        return FakeArDebugFlagEndpoint
    }
}
