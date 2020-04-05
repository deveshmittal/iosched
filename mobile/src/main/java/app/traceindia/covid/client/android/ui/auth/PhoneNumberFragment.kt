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

package app.traceindia.covid.client.android.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import app.traceindia.covid.client.android.R
import kotlinx.android.synthetic.main.fragment_phone_number.*

class PhoneNumberFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_phone_number, container, false)
    }

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phone_number_et.setText(authViewModel.phoneNumber)

        phone_number_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(phoneNumber: Editable?) {
                authViewModel.phoneNumber = phoneNumber.toString()
                continue_btn.isEnabled = phone_number_et.text?.isNotEmpty() ?: false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        continue_btn.setOnClickListener(this)
        continue_btn.isEnabled = phone_number_et.text?.isNotEmpty() ?: false

        authViewModel.state.observe(viewLifecycleOwner, Observer {
            when (it) {
                AuthViewModel.State.INVALID_PHONE_NUMBER -> phone_number_et.error = getString(R.string.invalid_phone_number_error)
                else -> {
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view) {
            continue_btn -> {
                context.let {
                    authViewModel.verifyPhoneNumber(context = requireContext(), number = phone_number_et.text.toString())
                }

            }
        }
    }
}