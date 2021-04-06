/*
 * Copyright 2021 André Claßen
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
package io.burnoutcrew.multinavhost.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavInflater
import androidx.navigation.fragment.findNavController
import io.burnoutcrew.multinavhost.R
import io.burnoutcrew.multinavhost.databinding.FragmentBottomNavigationBinding
import io.burnoutcrew.multinavhost.navigation.MultiNavHostFragmentController
import io.burnoutcrew.multinavhost.navigation.findDeepLinkIntent
import io.burnoutcrew.multinavhost.navigation.setup


class BottomNavigationFragment : Fragment() {
    private var binding: FragmentBottomNavigationBinding? = null
    private lateinit var multiNavController: MultiNavHostFragmentController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multiNavController = MultiNavHostFragmentController(
            childFragmentManager,
            NavInflater(requireContext(), findNavController().navigatorProvider)
                .let { inflater ->
                    arrayOf(
                        R.navigation.home_navigation,
                        R.navigation.dashboard_navigation,
                        R.navigation.notifications_navigation
                    )
                        .map { it to inflater.inflate(it) }
                },
            savedInstanceState
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentBottomNavigationBinding.inflate(inflater, container, false).also {
            binding = it
            handleDeepLink()
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.also { it.container.setup(multiNavController, this, it.toolbar, it.navView) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        multiNavController.onSaveInstanceState(outState)
    }

    private fun handleDeepLink() {
        binding?.container?.also { container ->
            arguments?.findDeepLinkIntent()?.also { (key, intent) ->
                multiNavController.handleDeepLink(intent, container)
                arguments?.remove(key)
            }
        }
    }

    fun navigateTo(@IdRes graphId: Int, @IdRes destination: Int) {
        binding?.container?.also { multiNavController.navigateTo(graphId, destination, it) }
    }
}