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

package app.traceindia.covid.client.android.shared.domain.tags

import app.traceindia.covid.client.android.model.Tag
import app.traceindia.covid.client.android.shared.data.tag.TagRepository
import app.traceindia.covid.client.android.shared.model.TestDataRepository
import app.traceindia.covid.client.android.shared.result.Result
import app.traceindia.covid.client.android.test.data.TestData.advancedTag
import app.traceindia.covid.client.android.test.data.TestData.androidTag
import app.traceindia.covid.client.android.test.data.TestData.beginnerTag
import app.traceindia.covid.client.android.test.data.TestData.cloudTag
import app.traceindia.covid.client.android.test.data.TestData.codelabsTag
import app.traceindia.covid.client.android.test.data.TestData.intermediateTag
import app.traceindia.covid.client.android.test.data.TestData.sessionsTag
import app.traceindia.covid.client.android.test.data.TestData.webTag
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [LoadTagsByCategoryUseCase]
 */
class LoadTagsByCategoryUseCaseTest {

    @Test
    fun returnsOrderedTags() {
        val useCase = LoadTagsByCategoryUseCase(TagRepository(TestDataRepository))
        val tags = useCase.executeNow(Unit) as Result.Success<List<Tag>>

        // Expected values to assert
        val expected = listOf(
            // category = LEVEL
            beginnerTag, intermediateTag, advancedTag,
            // category = TRACK
            androidTag, cloudTag, webTag,
            // category = TYPE
            sessionsTag, codelabsTag
        )

        assertEquals(expected, tags.data)
    }
}
