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

package app.traceindia.covid.client.android.ui.agenda

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.traceindia.covid.client.android.androidtest.util.LiveDataTestUtil
import app.traceindia.covid.client.android.model.Block
import app.traceindia.covid.client.android.shared.data.agenda.AgendaRepository
import app.traceindia.covid.client.android.shared.domain.agenda.LoadAgendaUseCase
import app.traceindia.covid.client.android.shared.domain.settings.GetTimeZoneUseCase
import app.traceindia.covid.client.android.test.data.TestData
import app.traceindia.covid.client.android.test.util.SyncTaskExecutorRule
import app.traceindia.covid.client.android.test.util.fakes.FakePreferenceStorage
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.hamcrest.Matchers.equalTo as isEqualTo

/**
 * Unit tests for the [AgendaViewModel].
 */
class AgendaViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    @Test
    fun agendaDataIsLoaded() {
        val viewModel = AgendaViewModel(
            LoadAgendaUseCase(FakeAgendaRepository()),
            GetTimeZoneUseCase(FakePreferenceStorage())
        )

        val blocks = LiveDataTestUtil.getValue(viewModel.loadAgendaResult)
        assertThat(blocks, isEqualTo(TestData.agenda))
    }

    internal class FakeAgendaRepository : AgendaRepository {

        override fun getObservableAgenda(): LiveData<List<Block>> {
            val result: MutableLiveData<List<Block>> = MutableLiveData()
            result.postValue(TestData.agenda)
            return result
        }

        override fun getAgenda(): List<Block> = TestData.agenda
    }
}
