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

package app.traceindia.covid.client.android.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.DaggerAppCompatDialogFragment

class DebugDialog: DaggerAppCompatDialogFragment() {
    companion object{
        private const val MESSAGE = "message"
        fun newInstance(message: String): DebugDialog{
            val bundle = Bundle().apply {
                putString(MESSAGE, message)
            }
            return DebugDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val args = requireNotNull(arguments)
        val message = args.getString(MESSAGE)
        return MaterialAlertDialogBuilder(context)
                .setTitle("Debug Info")
                .setMessage(message)
                .create()
    }
}