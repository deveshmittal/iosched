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

package app.traceindia.covid.client.android.ui.sessiondetail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.databinding.ItemGenericSectionHeaderBinding
import app.traceindia.covid.client.android.databinding.ItemSessionBinding
import app.traceindia.covid.client.android.databinding.ItemSessionInfoBinding
import app.traceindia.covid.client.android.databinding.ItemSpeakerBinding
import app.traceindia.covid.client.android.model.Speaker
import app.traceindia.covid.client.android.model.userdata.UserSession
import app.traceindia.covid.client.android.ui.SectionHeader
import app.traceindia.covid.client.android.ui.sessiondetail.SessionDetailViewHolder.HeaderViewHolder
import app.traceindia.covid.client.android.ui.sessiondetail.SessionDetailViewHolder.RelatedViewHolder
import app.traceindia.covid.client.android.ui.sessiondetail.SessionDetailViewHolder.SessionInfoViewHolder
import app.traceindia.covid.client.android.ui.sessiondetail.SessionDetailViewHolder.SpeakerViewHolder
import app.traceindia.covid.client.android.util.executeAfter

/**
 * [RecyclerView.Adapter] for presenting a session details, composed of information about the
 * session, any speakers plus any related events.
 */
class SessionDetailAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val sessionDetailViewModel: SessionDetailViewModel,
    private val tagRecycledViewPool: RecycledViewPool
) : RecyclerView.Adapter<SessionDetailViewHolder>() {

    private val differ = AsyncListDiffer<Any>(this, DiffCallback)

    var speakers: List<Speaker> = emptyList()
        set(value) {
            field = value
            differ.submitList(buildMergedList(sessionSpeakers = value))
        }

    var related: List<UserSession> = emptyList()
        set(value) {
            field = value
            differ.submitList(buildMergedList(relatedSessions = value))
        }

    init {
        differ.submitList(buildMergedList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionDetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_session_info -> SessionInfoViewHolder(
                ItemSessionInfoBinding.inflate(inflater, parent, false)
            )
            R.layout.item_speaker -> SpeakerViewHolder(
                ItemSpeakerBinding.inflate(inflater, parent, false)
            )
            R.layout.item_session -> RelatedViewHolder(
                ItemSessionBinding.inflate(inflater, parent, false).apply {
                    tags.setRecycledViewPool(tagRecycledViewPool)
                }
            )
            R.layout.item_generic_section_header -> HeaderViewHolder(
                ItemGenericSectionHeaderBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: SessionDetailViewHolder, position: Int) {
        when (holder) {
            is SessionInfoViewHolder -> holder.binding.executeAfter {
                viewModel = sessionDetailViewModel
                tagViewPool = tagRecycledViewPool
                lifecycleOwner = this@SessionDetailAdapter.lifecycleOwner
            }
            is SpeakerViewHolder -> holder.binding.executeAfter {
                val presenter = differ.currentList[position] as Speaker
                speaker = presenter
                eventListener = sessionDetailViewModel
                lifecycleOwner = this@SessionDetailAdapter.lifecycleOwner
                root.setTag(R.id.tag_speaker_id, presenter.id) // Used to identify clicked view
            }
            is RelatedViewHolder -> holder.binding.executeAfter {
                userSession = differ.currentList[position] as UserSession
                eventListener = sessionDetailViewModel
                timeZoneId = sessionDetailViewModel.timeZoneId
                showTime = true
                lifecycleOwner = this@SessionDetailAdapter.lifecycleOwner
            }
            is HeaderViewHolder -> holder.binding.executeAfter {
                sectionHeader = differ.currentList[position] as SectionHeader
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is SessionItem -> R.layout.item_session_info
            is Speaker -> R.layout.item_speaker
            is UserSession -> R.layout.item_session
            is SectionHeader -> R.layout.item_generic_section_header
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun getItemCount() = differ.currentList.size

    /**
     * This adapter displays heterogeneous data types but `RecyclerView` & `AsyncListDiffer` deal in
     * a single list of items. We therefore combine them into a merged list, using marker objects
     * for static items. We still hold separate lists of [speakers] and [related] sessions so that
     * we can provide them individually, as they're loaded.
     */
    private fun buildMergedList(
        sessionSpeakers: List<Speaker> = speakers,
        relatedSessions: List<UserSession> = related
    ): List<Any> {
        val merged = mutableListOf<Any>(SessionItem)
        if (sessionSpeakers.isNotEmpty()) {
            merged += SectionHeader(R.string.session_detail_speakers_header)
            merged.addAll(sessionSpeakers)
        }
        if (relatedSessions.isNotEmpty()) {
            merged += SectionHeader(R.string.session_detail_related_header)
            merged.addAll(relatedSessions)
        }
        return merged
    }
}

// Marker object for use in our merged representation.

object SessionItem

/**
 * Diff items presented by this adapter.
 */
object DiffCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem === SessionItem && newItem === SessionItem -> true
            oldItem is SectionHeader && newItem is SectionHeader -> oldItem == newItem
            oldItem is Speaker && newItem is Speaker -> oldItem.id == newItem.id
            oldItem is UserSession && newItem is UserSession ->
                oldItem.session.id == newItem.session.id
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals")
    // Workaround of https://issuetracker.google.com/issues/122928037
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is Speaker && newItem is Speaker -> oldItem == newItem
            oldItem is UserSession && newItem is UserSession -> oldItem == newItem
            else -> true
        }
    }
}

/**
 * [RecyclerView.ViewHolder] types used by this adapter.
 */
sealed class SessionDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class SessionInfoViewHolder(
        val binding: ItemSessionInfoBinding
    ) : SessionDetailViewHolder(binding.root)

    class SpeakerViewHolder(
        val binding: ItemSpeakerBinding
    ) : SessionDetailViewHolder(binding.root)

    class RelatedViewHolder(
        val binding: ItemSessionBinding
    ) : SessionDetailViewHolder(binding.root)

    class HeaderViewHolder(
        val binding: ItemGenericSectionHeaderBinding
    ) : SessionDetailViewHolder(binding.root)
}
