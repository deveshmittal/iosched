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

package app.traceindia.covid.client.android.shared.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import app.traceindia.covid.client.android.shared.BuildConfig
import app.traceindia.covid.client.android.shared.R
import app.traceindia.covid.client.android.shared.data.BootstrapConferenceDataSource
import app.traceindia.covid.client.android.shared.data.ConferenceDataRepository
import app.traceindia.covid.client.android.shared.data.ConferenceDataSource
import app.traceindia.covid.client.android.shared.data.NetworkConferenceDataSource
import app.traceindia.covid.client.android.shared.data.ar.ArDebugFlagEndpoint
import app.traceindia.covid.client.android.shared.data.ar.DefaultArDebugFlagEndpoint
import app.traceindia.covid.client.android.shared.data.config.AppConfigDataSource
import app.traceindia.covid.client.android.shared.data.config.RemoteAppConfigDataSource
import app.traceindia.covid.client.android.shared.data.db.AppDatabase
import app.traceindia.covid.client.android.shared.data.feed.AnnouncementDataSource
import app.traceindia.covid.client.android.shared.data.feed.DefaultFeedRepository
import app.traceindia.covid.client.android.shared.data.feed.FeedRepository
import app.traceindia.covid.client.android.shared.data.feed.FirestoreAnnouncementDataSource
import app.traceindia.covid.client.android.shared.data.feedback.DefaultFeedbackEndpoint
import app.traceindia.covid.client.android.shared.data.feedback.FeedbackEndpoint
import app.traceindia.covid.client.android.shared.data.feed.FirestoreMomentDataSource
import app.traceindia.covid.client.android.shared.data.feed.MomentDataSource
import app.traceindia.covid.client.android.shared.data.session.DefaultSessionRepository
import app.traceindia.covid.client.android.shared.data.session.SessionRepository
import app.traceindia.covid.client.android.shared.data.userevent.DefaultSessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.FirestoreUserEventDataSource
import app.traceindia.covid.client.android.shared.data.userevent.SessionAndUserEventRepository
import app.traceindia.covid.client.android.shared.data.userevent.UserEventDataSource
import app.traceindia.covid.client.android.shared.fcm.FcmTopicSubscriber
import app.traceindia.covid.client.android.shared.fcm.TopicSubscriber
import app.traceindia.covid.client.android.shared.time.DefaultTimeProvider
import app.traceindia.covid.client.android.shared.time.TimeProvider
import app.traceindia.covid.client.android.shared.util.NetworkUtils
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
    fun provideConferenceDataSource(
        context: Context,
        networkUtils: NetworkUtils
    ): ConferenceDataSource {
        return NetworkConferenceDataSource(context, networkUtils)
    }

    @Singleton
    @Provides
    @Named("bootstrapConfDataSource")
    fun provideBootstrapRemoteSessionDataSource(): ConferenceDataSource {
        return BootstrapConferenceDataSource
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
    fun provideAnnouncementDataSource(firestore: FirebaseFirestore): AnnouncementDataSource {
        return FirestoreAnnouncementDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideMomentsDataSource(firestore: FirebaseFirestore): MomentDataSource {
        return FirestoreMomentDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideFeedRepository(
        dataSource: AnnouncementDataSource,
        momentsDataSource: MomentDataSource
    ): FeedRepository {
        return DefaultFeedRepository(dataSource, momentsDataSource)
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
    fun provideUserEventDataSource(firestore: FirebaseFirestore): UserEventDataSource {
        return FirestoreUserEventDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideFeedbackEndpoint(functions: FirebaseFunctions): FeedbackEndpoint {
        return DefaultFeedbackEndpoint(functions)
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
    fun provideFirebaseFireStore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            // This is to enable the offline data
            // https://firebase.google.com/docs/firestore/manage-data/enable-offline
            .setPersistenceEnabled(true)
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        return firestore
    }

    @Singleton
    @Provides
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }

    @Singleton
    @Provides
    fun provideArDebugFlagEndpoint(functions: FirebaseFunctions): ArDebugFlagEndpoint {
        return DefaultArDebugFlagEndpoint(functions)
    }

    @Singleton
    @Provides
    fun provideTopicSubscriber(): TopicSubscriber {
        return FcmTopicSubscriber()
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfigSettings(): FirebaseRemoteConfigSettings {
        return FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfig(
        configSettings: FirebaseRemoteConfigSettings
    ): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.remote_config_defaults)
        return remoteConfig
    }

    @Singleton
    @Provides
    fun provideAppConfigDataSource(
        remoteConfig: FirebaseRemoteConfig,
        configSettings: FirebaseRemoteConfigSettings
    ): AppConfigDataSource {
        return RemoteAppConfigDataSource(remoteConfig, configSettings)
    }

    @Singleton
    @Provides
    fun provideTimeProvider(): TimeProvider {
        return DefaultTimeProvider
    }
}
