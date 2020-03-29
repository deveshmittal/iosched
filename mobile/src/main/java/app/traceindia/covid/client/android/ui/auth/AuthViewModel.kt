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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel(), AuthListener {

    companion object {
        private const val ERROR_INVALID_PHONE_NUMBER = "ERROR_INVALID_PHONE_NUMBER"
        private const val ERROR_INVALID_VERIFICATION_CODE = "ERROR_INVALID_VERIFICATION_CODE"
    }

    var state = MutableLiveData(State.ENTERING_PHONE_NUMBER)
    private val authProvider = AuthProvider().apply { authListener = this@AuthViewModel }
    var phoneNumber: String = ""
    var smsCode: String = ""

    enum class State {
        ENTERING_PHONE_NUMBER,
        INVALID_PHONE_NUMBER,
        ENTERING_VERIFICATION_CODE,
        LOGGED_IN,
        RESEND_CODE,
        INVALID_CODE,
        TIME_OUT
    }

    fun verifyPhoneNumber(number: String) {
        phoneNumber = number
        authProvider.verifyPhoneNumber(number)
    }

    fun resendVerificationCode() {
        authProvider.resendVerificationCode(phoneNumber)
    }

    suspend fun verifyCurrentUser() = authProvider.verifyCurrentUser()

    fun signOut() = authProvider.signOutUser()

    fun signIn(smsCode: String) {
        this.smsCode = smsCode
        authProvider.signInWithPhoneAuthCredential(authProvider.createCredential(smsCode))
    }

    fun backToEnteringPhoneNumber() {
        state.value = State.ENTERING_PHONE_NUMBER
    }

    fun backToEnteringVerificationCode() {
        state.value = State.ENTERING_VERIFICATION_CODE
    }

    override fun onStarted() {
        state.value = State.ENTERING_VERIFICATION_CODE
    }

    override fun onSuccess() {
        state.value = State.LOGGED_IN
    }

    override fun onFailure(errorCode: String) {
        when(errorCode) {
            ERROR_INVALID_PHONE_NUMBER -> state.value = State.INVALID_PHONE_NUMBER
            ERROR_INVALID_VERIFICATION_CODE -> state.value = State.INVALID_CODE
        }
    }

    override fun onResendCode() {
        state.value = State.RESEND_CODE
    }

    override fun onTimeOut() {
        state.value = State.TIME_OUT
    }
}