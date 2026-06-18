/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import dev.astatin3.thirteen.R

class PlaylistGridItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
) : MaterialCardView(context, attrs, defStyleAttr) {
    private val placeholderImageView by lazy { findViewById<ImageView>(R.id.placeholderImageView) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }
    private val headlineTextView by lazy { findViewById<TextView>(R.id.headlineTextView) }

    var headlineText: CharSequence?
        get() = headlineTextView.text
        set(value) {
            headlineTextView.text = value
            headlineTextView.isVisible = value != null
        }

    init {
        setCardBackgroundColor(
            resources.getColorStateList(R.color.list_item_background, context.theme)
        )
        cardElevation = 0f
        strokeWidth = 0

        inflate(context, R.layout.item_playlist_grid, this)
    }

    fun setHeadlineText(@StringRes resId: Int) {
        headlineText = resources.getText(resId)
    }

    fun setThumbnail(bitmap: Bitmap?, @DrawableRes placeholderResId: Int) {
        if (bitmap != null) {
            placeholderImageView.isVisible = false
            thumbnailImageView.isVisible = true
            thumbnailImageView.setImageBitmap(bitmap)
        } else {
            placeholderImageView.setImageResource(placeholderResId)
            placeholderImageView.isVisible = true
            thumbnailImageView.isVisible = false
        }
    }
}
