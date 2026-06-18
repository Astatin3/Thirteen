/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dev.astatin3.thirteen.ext.updateBarsVisibility
import dev.astatin3.thirteen.viewmodels.FullscreenViewModel

abstract class ThirteenBottomSheetDialogFragment(
    @LayoutRes contentLayoutId: Int,
) : BottomSheetDialogFragment(contentLayoutId) {
    // View models
    private val fullscreenViewModel by activityViewModels<FullscreenViewModel>()

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().window?.updateBarsVisibility(
            systemBars = !fullscreenViewModel.fullscreenMode.value
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                fullscreenViewModel.fullscreenMode.collectLatest {
                    requireDialog().window?.updateBarsVisibility(systemBars = !it)
                }
            }
        }
    }
}
