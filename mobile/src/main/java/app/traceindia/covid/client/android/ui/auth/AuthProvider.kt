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

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface AuthListener {
    fun onStarted()
    fun onSuccess()
    fun onFailure(errorCode: String)
    fun onResendCode()
    fun onTimeOut()
}

class AuthProvider {
    private val phoneAuthProvider = PhoneAuthProvider.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var verificationId: String
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    var authListener: AuthListener? = null
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val verificationStateChangedCallbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(id, token)
                    authListener?.onStarted()
                    verificationId = id
                    resendingToken = token
                }

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    if(exception is FirebaseAuthInvalidCredentialsException){
                        authListener?.onFailure(exception.errorCode)
                    }else{
                        authListener?.onFailure("Something went wrong!Please try again later.")
                    }

                }

                override fun onCodeAutoRetrievalTimeOut(p0: String) {
                    super.onCodeAutoRetrievalTimeOut(p0)
                    authListener?.onTimeOut()
                }
            }

    fun isUserSignedIn() = firebaseAuth.currentUser != null

    suspend fun verifyCurrentUser(): Boolean {
        return suspendCoroutine {
            firebaseAuth.currentUser?.reload()
                    ?.addOnCompleteListener { task -> it.resume(task.isSuccessful) }
                    ?: it.resume(false)
        }
    }

    fun signOutUser() = auth.signOut()

    fun createCredential(smsCode: String) = PhoneAuthProvider.getCredential(verificationId, smsCode)

    fun resendVerificationCode(phoneNumber: String) {
        phoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                verificationStateChangedCallbacks,
                resendingToken
        )
    }

    fun verifyPhoneNumber(number: String) {
        phoneAuthProvider.verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                verificationStateChangedCallbacks
        )
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(TaskExecutors.MAIN_THREAD, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        authListener?.onSuccess()
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            authListener?.onFailure((task.exception as FirebaseAuthInvalidCredentialsException).errorCode)
                        }
                    }
                })
    }
}