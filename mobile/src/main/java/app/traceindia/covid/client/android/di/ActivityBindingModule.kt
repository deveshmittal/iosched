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

package app.traceindia.covid.client.android.di

import app.traceindia.covid.client.android.shared.di.ActivityScoped
import app.traceindia.covid.client.android.ui.LaunchModule
import app.traceindia.covid.client.android.ui.LauncherActivity
import app.traceindia.covid.client.android.ui.MainActivity
import app.traceindia.covid.client.android.ui.MainActivityModule
import app.traceindia.covid.client.android.ui.agenda.AgendaModule
import app.traceindia.covid.client.android.ui.codelabs.CodelabsModule
import app.traceindia.covid.client.android.ui.feed.FeedModule
import app.traceindia.covid.client.android.ui.info.InfoModule
import app.traceindia.covid.client.android.ui.map.MapModule
import app.traceindia.covid.client.android.ui.onboarding.OnboardingActivity
import app.traceindia.covid.client.android.ui.onboarding.OnboardingModule
import app.traceindia.covid.client.android.ui.prefs.PreferenceModule
import app.traceindia.covid.client.android.ui.reservation.ReservationModule
import app.traceindia.covid.client.android.ui.schedule.ScheduleModule
import app.traceindia.covid.client.android.ui.search.SearchModule
import app.traceindia.covid.client.android.ui.sessioncommon.EventActionsViewModelDelegateModule
import app.traceindia.covid.client.android.ui.sessiondetail.SessionDetailActivity
import app.traceindia.covid.client.android.ui.sessiondetail.SessionDetailModule
import app.traceindia.covid.client.android.ui.sessiondetail.SessionFeedbackModule
import app.traceindia.covid.client.android.ui.settings.SettingsModule
import app.traceindia.covid.client.android.ui.signin.SignInDialogModule
import app.traceindia.covid.client.android.ui.speaker.SpeakerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * We want Dagger.Android to create a Subcomponent which has a parent Component of whichever module
 * ActivityBindingModule is on, in our case that will be [AppComponent]. You never
 * need to tell [AppComponent] that it is going to have all these subcomponents
 * nor do you need to tell these subcomponents that [AppComponent] exists.
 * We are also telling Dagger.Android that this generated SubComponent needs to include the
 * specified modules and be aware of a scope annotation [@ActivityScoped].
 * When Dagger.Android annotation processor runs it will create 2 subcomponents for us.
 */
@Module
@Suppress("UNUSED")
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LaunchModule::class])
    internal abstract fun launcherActivity(): LauncherActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            OnboardingModule::class,
            SignInDialogModule::class
        ]
    )
    internal abstract fun onboardingActivity(): OnboardingActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            // activity
            MainActivityModule::class,
            // fragments
            AgendaModule::class,
            CodelabsModule::class,
            FeedModule::class,
            InfoModule::class,
            MapModule::class,
            ScheduleModule::class,
            SearchModule::class,
            SessionDetailModule::class,
            SettingsModule::class,
            SpeakerModule::class,
            // other
            PreferenceModule::class,
            ReservationModule::class,
            PreferenceModule::class,
            SessionFeedbackModule::class,
            SignInDialogModule::class,
            EventActionsViewModelDelegateModule::class
        ]
    )
    internal abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SessionDetailModule::class,
            SessionFeedbackModule::class,
            SignInDialogModule::class,
            ReservationModule::class,
            PreferenceModule::class
        ]
    )
    internal abstract fun sessionDetailActivity(): SessionDetailActivity
}
