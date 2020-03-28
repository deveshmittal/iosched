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

package app.traceindia.covid.client.android.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import app.traceindia.covid.client.android.R
import app.traceindia.covid.client.android.shared.result.EventObserver
import app.traceindia.covid.client.android.shared.util.inTransaction
import app.traceindia.covid.client.android.shared.util.viewModelProvider
import app.traceindia.covid.client.android.ui.signin.SignInDialogFragment
import app.traceindia.covid.client.android.util.doOnApplyWindowInsets
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class OnboardingActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewModel = viewModelProvider(viewModelFactory)

        // immersive mode so images can draw behind the status bar
        val decor = window.decorView
        val flags = decor.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decor.systemUiVisibility = flags

        val container: FrameLayout = findViewById(R.id.fragment_container)
        container.doOnApplyWindowInsets { v, insets, padding ->
            v.updatePadding(top = padding.top + insets.systemWindowInsetTop)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.inTransaction {
                add(R.id.fragment_container, OnboardingFragment())
            }
        }

        viewModel.navigateToSignInDialogAction.observe(this, EventObserver {
            openSignInDialog()
        })
    }

    private fun openSignInDialog() {
        SignInDialogFragment().show(supportFragmentManager, DIALOG_SIGN_IN)
    }

    companion object {
        private const val DIALOG_SIGN_IN = "dialog_sign_in"
    }
}
