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

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.traceindia.covid.client.android.BuildConfig
import app.traceindia.covid.client.android.shared.data.prefs.SharedPreferenceStorage
import app.traceindia.covid.client.android.shared.domain.prefs.AuthCompleteActionUseCase
import app.traceindia.covid.client.android.shared.domain.prefs.AuthCompletedUseCase
import app.traceindia.covid.client.android.shared.util.PhoneNumberAuthUtils
import javax.inject.Inject

class AuthViewModel @Inject constructor() : ViewModel(), AuthListener {


    lateinit var authCompleteActionUseCase : AuthCompleteActionUseCase
    lateinit var authCompletedUseCase : AuthCompletedUseCase

    companion object {
        private const val ERROR_INVALID_PHONE_NUMBER = "ERROR_INVALID_PHONE_NUMBER"
        private const val ERROR_INVALID_VERIFICATION_CODE = "ERROR_INVALID_VERIFICATION_CODE"
    }

    var state = MutableLiveData(State.ENTERING_PHONE_NUMBER)
    private val authProvider = AuthProvider().apply { authListener = this@AuthViewModel }
    var phoneNumber: String = ""
    var smsCode: String = ""

    enum class State(state :String) {
        ENTERING_PHONE_NUMBER("Entering Phone Number"),
        INVALID_PHONE_NUMBER("Invalid Number"),
        ENTERING_VERIFICATION_CODE("Entering Verfication Code"),
        LOGGED_IN("Logged In"),
        RESEND_CODE("Resend Code"),
        INVALID_CODE("Invalid Code"),
        TIME_OUT("Timed Out"),
        UNKNOWN_ERROR_CODE("Unknown Error Occured")
    }

    fun verifyPhoneNumber(context: Context ,number: String) {
        phoneNumber = number.trim()
        authCompleteActionUseCase = AuthCompleteActionUseCase(SharedPreferenceStorage(context))
        authCompletedUseCase = AuthCompletedUseCase(SharedPreferenceStorage(context))
        if(!PhoneNumberAuthUtils.isPhoneNumberValid(number)){
            state.value = State.INVALID_PHONE_NUMBER
            return
        }
        if(!number.contains('+')){
            phoneNumber = "+${PhoneNumberAuthUtils.getDeviceCallingCode(context)}${number}"
        }
        authProvider.verifyPhoneNumber(phoneNumber)
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
        authCompletedUseCase(true)
        authCompleteActionUseCase(phoneNumber)
        state.value = State.LOGGED_IN
    }

    override fun onFailure(errorCode: String) {
        when(errorCode) {
            ERROR_INVALID_PHONE_NUMBER -> state.value = State.INVALID_PHONE_NUMBER
            ERROR_INVALID_VERIFICATION_CODE -> state.value = State.INVALID_CODE
            else -> state.value = State.UNKNOWN_ERROR_CODE
        }
    }

    override fun onResendCode() {
        state.value = State.RESEND_CODE
    }


    override fun onTimeOut() {
        state.value = State.TIME_OUT
    }
}