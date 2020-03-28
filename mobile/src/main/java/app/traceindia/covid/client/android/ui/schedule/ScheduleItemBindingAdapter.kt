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

package app.traceindia.covid.client.android.ui.schedule

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.databinding.BindingAdapter
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.model.Room
import app.traceindia.covid.client.android.model.userdata.UserEvent
import app.traceindia.covid.client.android.shared.util.TimeUtils
import app.traceindia.covid.client.android.ui.reservation.ReservationTextView
import app.traceindia.covid.client.android.ui.reservation.ReservationViewState
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@BindingAdapter(
    "sessionStart",
    "timeZoneId",
    "showTime",
    "sessionRoom",
    requireAll = true
)
fun sessionDateTimeLocation(
    textView: TextView,
    startTime: ZonedDateTime,
    zoneId: ZoneId?,
    showTime: Boolean,
    room: Room?
) {
    zoneId ?: return
    val roomName = room?.name ?: "-"
    val localStartTime = TimeUtils.zonedTime(startTime, zoneId)

    // For a11y, always use date, time, and location -> "May 7, 10:00 AM / Amphitheatre
    val dateTimeString = TimeUtils.dateTimeString(localStartTime)
    val contentDescription = textView.resources.getString(
        R.string.session_duration_location,
        dateTimeString,
        roomName
    )
    textView.contentDescription = contentDescription

    textView.text = if (showTime) {
        // Show date, time, and location, so just reuse the content description
        contentDescription
    } else if (!TimeUtils.isConferenceTimeZone(zoneId)) {
        // Show date and location -> "May 7 / Amphitheatre"
        val dateString = TimeUtils.dateString(localStartTime)
        textView.resources.getString(
            R.string.session_duration_location,
            dateString,
            roomName
        )
    } else {
        // Show location only
        roomName
    }
}

@BindingAdapter(
    "reservationStatus",
    "showReservations",
    "isReservable",
    requireAll = true
)
fun setReservationStatus(
    textView: ReservationTextView,
    userEvent: UserEvent?,
    showReservations: Boolean,
    isReservable: Boolean
) {
    when (isReservable && showReservations) {
        true -> {
            val reservationUnavailable = false // TODO determine this condition
            textView.status = ReservationViewState.fromUserEvent(userEvent, reservationUnavailable)
            textView.visibility = VISIBLE
        }
        false -> {
            textView.visibility = GONE
        }
    }
}
