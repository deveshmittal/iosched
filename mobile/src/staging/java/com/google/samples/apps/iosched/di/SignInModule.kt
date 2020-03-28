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

package app.traceindia.covid.client.android.di

import android.content.Context
import app.traceindia.covid.client.android.shared.data.login.StagingAuthenticatedUser
import app.traceindia.covid.client.android.shared.data.login.StagingSignInHandler
import app.traceindia.covid.client.android.shared.data.login.datasources.StagingAuthStateUserDataSource
import app.traceindia.covid.client.android.shared.data.login.datasources.StagingRegisteredUserDataSource
import app.traceindia.covid.client.android.shared.data.signin.datasources.AuthIdDataSource
import app.traceindia.covid.client.android.shared.data.signin.datasources.AuthStateUserDataSource
import app.traceindia.covid.client.android.shared.data.signin.datasources.RegisteredUserDataSource
import app.traceindia.covid.client.android.shared.domain.sessions.NotificationAlarmUpdater
import app.traceindia.covid.client.android.util.signin.SignInHandler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class SignInModule {
    @Provides
    fun provideSignInHandler(context: Context): SignInHandler {
        return StagingSignInHandler(StagingAuthenticatedUser(context))
    }

    @Singleton
    @Provides
    fun provideRegisteredUserDataSource(context: Context): RegisteredUserDataSource {
        return StagingRegisteredUserDataSource(true)
    }

    @Singleton
    @Provides
    fun provideAuthStateUserDataSource(
        context: Context,
        notificationAlarmUpdater: NotificationAlarmUpdater
    ): AuthStateUserDataSource {
        return StagingAuthStateUserDataSource(
            isRegistered = true,
            isSignedIn = true,
            context = context,
            userId = "StagingTest",
            notificationAlarmUpdater = notificationAlarmUpdater
        )
    }

    @Singleton
    @Provides
    fun providesAuthIdDataSource(): AuthIdDataSource {
        return object : AuthIdDataSource {
            override fun getUserId() = "StagingTest"
        }
    }
}
