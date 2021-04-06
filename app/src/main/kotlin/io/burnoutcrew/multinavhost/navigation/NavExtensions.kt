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
package io.burnoutcrew.multinavhost.navigation

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

fun FragmentContainerView.setup(
    multiNavController: MultiNavHostFragmentController,
    fragment: Fragment,
    toolbar: Toolbar,
    bottomNavigationView: BottomNavigationView
) {
    val activity = fragment.requireActivity()
    activity.onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner) {
        if (multiNavController.currentNavHost(this@setup)?.findNavController()?.navigateUp() != true) {
            activity.finish()
        }
    }
    multiNavController.onDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, _ ->
            controller.setupToolbar(toolbar, destination)
            if (controller.graph.id != bottomNavigationView.selectedItemId) {
                bottomNavigationView.selectedItemId = controller.graph.id
            }
        }
    bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
        multiNavController.select(menuItem.itemId, this)
        true
    }
    bottomNavigationView.setOnNavigationItemReselectedListener {
        multiNavController.currentNavHost(this)?.findNavController()?.also { nc ->
            nc.popBackStack(nc.graph.startDestination, false)
        }
    }
    if (multiNavController.currentNavHost(this) == null && bottomNavigationView.selectedItemId > 0) {
        multiNavController.select(bottomNavigationView.selectedItemId, this)
    }
}

fun NavController.setupToolbar(toolbar: Toolbar, destination: NavDestination? = null) {
    if (previousBackStackEntry != null) {
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { navigateUp() }
    } else if (previousBackStackEntry == null) {
        toolbar.navigationIcon = null
        toolbar.setNavigationOnClickListener(null)
    }
    destination?.label?.also { toolbar.title = it }
}

fun Bundle.findDeepLinkIntent(): Pair<String, Intent>? =
    keySet()
        .asSequence()
        .map { key ->
            (get(key) as? Intent)
                ?.let { intent -> if (intent.data != null) key to intent else null }
        }
        .filterNotNull()
        .firstOrNull()