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
package io.burnoutcrew.multinavhost.ui.notifications

import androidx.navigation.fragment.findNavController
import io.burnoutcrew.multinavhost.R
import io.burnoutcrew.multinavhost.ui.shared.BaseListFragment

class NotificationsFragment : BaseListFragment() {
    override val onClick: (() -> Unit)
        get() = { findNavController().navigate(R.id.navigation_notificationsSecondLevel) }
}

class NotificationsSecondLevelFragment : BaseListFragment() {
    override val onClick: (() -> Unit)
        get() = { findNavController().navigate(R.id.notificationsThirdLevel) }
}

class NotificationsThirdLevelFragment : BaseListFragment() {
    override val onClick: (() -> Unit)
        get() = { parentFragment?.parentFragment?.findNavController()?.navigate(R.id.settings) }
}