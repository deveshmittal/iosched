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
import kotlinx.android.synthetic.main.fragment_verification_code.*

class VerificationCodeFragment : Fragment(), View.OnClickListener {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_verification_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sms_code_et.setText(authViewModel.smsCode)

        sms_code_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(sms_code: Editable?) {
                log_in_btn.isEnabled = sms_code?.isNotEmpty() ?: false
                authViewModel.smsCode = sms_code.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        log_in_btn.setOnClickListener(this)
        resend_code_btn.setOnClickListener(this)
        toolbar.setNavigationOnClickListener { _ ->
            if (this.isAdded) requireActivity().onBackPressed()
        }

        log_in_btn.isEnabled = sms_code_et.text?.isNotEmpty() ?: false
        resend_code_btn.isEnabled = false

        authViewModel.state.observe(viewLifecycleOwner, Observer {
            when (it) {
                AuthViewModel.State.TIME_OUT -> resend_code_btn.isEnabled = true
                AuthViewModel.State.INVALID_CODE -> sms_code_et.error = getString(R.string.invalid_code_number_error)
                else -> {
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view) {
            log_in_btn -> authViewModel.signIn(sms_code_et.text.toString())
            resend_code_btn -> {
                authViewModel.verifyPhoneNumber(requireContext(), authViewModel.phoneNumber)
                resend_code_btn.isEnabled = false
            }
        }
    }
}
