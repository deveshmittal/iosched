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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import app.traceindia.covid.client.android.databinding.FragmentAgendaBinding
import app.traceindia.covid.client.android.model.Block
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.shared.util.activityViewModelProvider
import app.traceindia.covid.client.android.shared.util.viewModelProvider
import app.traceindia.covid.client.android.ui.MainNavigationFragment
import app.traceindia.covid.client.android.ui.signin.setupProfileMenuItem
import app.traceindia.covid.client.android.util.clearDecorations
import app.traceindia.covid.client.android.util.doOnApplyWindowInsets
import org.threeten.bp.ZoneId
import javax.inject.Inject

class AgendaFragment : MainNavigationFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: AgendaViewModel
    private lateinit var binding: FragmentAgendaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAgendaBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        // Pad the bottom of the RecyclerView so that the content scrolls up above the nav bar
        binding.recyclerView.doOnApplyWindowInsets { v, insets, padding ->
            v.updatePaddingRelative(bottom = padding.bottom + insets.systemWindowInsetBottom)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)

        binding.viewModel = viewModel
        binding.toolbar.setupProfileMenuItem(
            activityViewModelProvider(viewModelFactory), this@AgendaFragment
        )
    }
}

@BindingAdapter(value = ["agendaItems", "timeZoneId"])
fun agendaItems(recyclerView: RecyclerView, list: List<Block>?, zoneId: ZoneId?) {
    list ?: return
    zoneId ?: return
    val isInConferenceTimeZone = TimeUtils.isConferenceTimeZone(zoneId)
    if (recyclerView.adapter == null) {
        recyclerView.adapter = AgendaAdapter()
    }
    (recyclerView.adapter as AgendaAdapter).apply {
        submitList(list)
        timeZoneId = zoneId
    }
    // Recreate the decoration used for the sticky date headers
    recyclerView.clearDecorations()
    if (list.isNotEmpty()) {
        recyclerView.addItemDecoration(
            AgendaHeadersDecoration(recyclerView.context, list, isInConferenceTimeZone)
        )
    }
}
