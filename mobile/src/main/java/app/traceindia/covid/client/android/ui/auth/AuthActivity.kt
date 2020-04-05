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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.traceindia.covid.client.android.BuildConfig
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.ui.MainActivity
import app.traceindia.covid.client.android.ui.dialogs.DebugDialog

class AuthActivity : AppCompatActivity() {

    companion object {
        private const val VERIFICATION_CODE_FRAGMENT = "VERIFICATION_CODE_FRAGMENT"
        private const val PHONE_NUMBER_FRAGMENT = "PHONE_NUMBER_FRAGMENT"
    }

    private val authViewModel by lazy {
        ViewModelProvider(this, AuthViewModelFactory())
                .get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        authViewModel.state.observe(this, Observer {
            val transaction = supportFragmentManager.beginTransaction()
            when(it) {
                AuthViewModel.State.ENTERING_PHONE_NUMBER -> transaction.replace(R.id.auth_fl, PhoneNumberFragment(), PHONE_NUMBER_FRAGMENT)
                        .addToBackStack(PHONE_NUMBER_FRAGMENT)
                        .commit()
                AuthViewModel.State.ENTERING_VERIFICATION_CODE -> transaction.replace(R.id.auth_fl, VerificationCodeFragment(), VERIFICATION_CODE_FRAGMENT)
                        .addToBackStack(VERIFICATION_CODE_FRAGMENT)
                        .commit()
                AuthViewModel.State.LOGGED_IN -> openMainActivity()
                AuthViewModel.State.RESEND_CODE -> authViewModel.resendVerificationCode()
                else -> {

                }
            }
        })
    }

    private fun openMainActivity() {
        if(BuildConfig.DEBUG) Toast.makeText(this, "Logged In!", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val verificationCodeFragment = supportFragmentManager.findFragmentByTag(VERIFICATION_CODE_FRAGMENT)
        if (verificationCodeFragment?.isVisible == true) authViewModel.backToEnteringPhoneNumber()
        else finishAffinity()
    }
}