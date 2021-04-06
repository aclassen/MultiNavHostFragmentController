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
package io.burnoutcrew.multinavhost.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.burnoutcrew.multinavhost.databinding.FragmentDetailBinding
import io.burnoutcrew.multinavhost.navigation.setupToolbar
import io.burnoutcrew.multinavhost.ui.shared.SimpleItemAdapter

class DetailsFragment : Fragment() {
    private var binding: FragmentDetailBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentDetailBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.also {
            findNavController().setupToolbar(it.toolbar)
            it.rcv.layoutManager = LinearLayoutManager(requireContext())
            it.rcv.adapter = SimpleItemAdapter(List(50) { idx -> "${javaClass.simpleName} $idx" }
            )
        }
    }
}