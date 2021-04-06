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
import android.os.Parcelable
import android.util.SparseArray
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

class MultiNavHostFragmentController(
    private val fm: FragmentManager,
    private val menuItemToGraph: List<Pair<Int, NavGraph>>,
    savedInstanceState: Bundle? = null
) {
    private val savedState = SparseArray<Fragment.SavedState?>()
    private var current: NavHostFragment? = null

    init {
        if (savedInstanceState != null) {
            restoreState(savedInstanceState.getParcelable(KEY_STATE), savedInstanceState.classLoader)
        }
    }

    fun select(@IdRes graphId: Int, container: ViewGroup) {
        current?.also { current ->
            if (current.arguments?.getInt("graphId") == graphId) {
                return
            }
            onDestinationChangedListener?.also { current.findNavController().removeOnDestinationChangedListener(it) }
        }
        val transaction = fm.beginTransaction()
        // Destroy current
        fm.findFragmentById(container.id)
            ?.also { fragment ->
                savedState.put(
                    fragment.requireArguments().getInt("graphId"),
                    if (fragment.isAdded) fm.saveFragmentInstanceState(fragment) else null
                )
                current = null
                transaction.remove(fragment)
            }
        // Instantiate
        val fragment: NavHostFragment =
            NavHostFragment.create(graphId.findNavigationRes()).also { it.arguments?.putInt("graphId", graphId) }
        savedState.get(graphId)?.also { fragment.setInitialSavedState(it) }
        current = fragment
        transaction.add(container.id, fragment)
        transaction.commitNowAllowingStateLoss()
        onDestinationChangedListener?.also { current?.findNavController()?.addOnDestinationChangedListener(it) }
    }

    var onDestinationChangedListener: NavController.OnDestinationChangedListener? = null
        set(value) {
            current?.findNavController()
                ?.also { nc ->
                    field?.also { nc.removeOnDestinationChangedListener(it) }
                    value?.also { value ->
                        nc.currentBackStackEntry?.also { value.onDestinationChanged(nc, it.destination, it.arguments) }
                        nc.addOnDestinationChangedListener(value)
                    }
                }
            field = value
        }

    fun handleDeepLink(intent: Intent, container: ViewGroup) {
        menuItemToGraph
            .firstOrNull { it.second.hasDeepLink(intent.data!!) }
            ?.also { target ->
                select(target.second.id, container)
                current
                    ?.findNavController()
                    ?.handleDeepLink(intent)
            }
    }

    fun navigateTo(@IdRes graphId: Int, @IdRes destination: Int, container: ViewGroup, arguments: Bundle? = null) {
        savedState.put(graphId, null)
        select(graphId, container)
        currentNavHost(container)
            ?.findNavController()
            ?.handleDeepLink(
                NavDeepLinkBuilder(container.context)
                    .setDestination(destination)
                    .setArguments(arguments)
                    .setGraph(graphId.findNavigationRes())
                    .createTaskStackBuilder().intents.first()
            )
    }

    fun currentNavHost(container: ViewGroup) =
        fm.findFragmentById(container.id) as? NavHostFragment

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_STATE, saveState())
    }

    private fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        (state as? Bundle)?.also { bundle ->
            bundle.classLoader = loader
            savedState.clear()
            current = null
            bundle.getSparseParcelableArray<Fragment.SavedState?>("states")
                ?.forEach { key, value ->
                    savedState.put(key, value)
                }
            if (bundle.containsKey("f0")) {
                current = fm.getFragment(bundle, "f0") as NavHostFragment
            }
        }
    }

    private fun saveState(): Parcelable? {
        var state: Bundle? = null
        if (savedState.isNotEmpty()) {
            state = Bundle()
            state.putSparseParcelableArray("states", savedState)
        }
        current?.also { fragment ->
            if (fragment.isAdded) {
                if (state == null) {
                    state = Bundle()
                }
                fm.putFragment(state!!, "f0", fragment)
            }
        }
        return state
    }

    private fun Int.findNavigationRes(): Int {
        return menuItemToGraph.firstOrNull { it.second.id == this }?.first
            ?: throw IllegalStateException("GraphId $this is invalid!")
    }

    companion object {
        const val KEY_STATE = "multiNavHostFragmentControllerState"
    }
}