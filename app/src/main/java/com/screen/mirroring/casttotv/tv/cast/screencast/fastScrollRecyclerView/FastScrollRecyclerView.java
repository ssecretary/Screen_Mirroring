/*
 * Copyright 2018 L4 Digital. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.screen.mirroring.casttotv.tv.cast.screencast.fastScrollRecyclerView;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.screen.mirroring.casttotv.tv.cast.screencast.R;

public class FastScrollRecyclerView extends RecyclerView {

    private FastScroller fastScroller;

    public FastScrollRecyclerView(@NonNull Context context) {
        super(context);
        layout(context, null);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public FastScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        layout(context, attrs);
    }


    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter instanceof FastScroller.SectionIndexer) {
            fastScroller.setSectionIndexer((FastScroller.SectionIndexer) adapter);
        } else if (adapter == null) {
            fastScroller.setSectionIndexer(null);
        }
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        fastScroller.setVisibility(visibility);
    }

    public void setFastScrollListener(@Nullable FastScroller.FastScrollListener fastScrollListener) {
        fastScroller.setFastScrollListener(fastScrollListener);
    }

    public void setSectionIndexer(@Nullable FastScroller.SectionIndexer sectionIndexer) {
        fastScroller.setSectionIndexer(sectionIndexer);
    }

    public void setFastScrollEnabled(boolean enabled) {
        fastScroller.setEnabled(enabled);
    }


    public void setHideScrollbar(boolean hideScrollbar) {
        fastScroller.setHideScrollbar(hideScrollbar);
    }

    public void setTrackVisible(boolean visible) {
        fastScroller.setTrackVisible(visible);
    }

    public void setTrackColor(@ColorInt int color) {
        fastScroller.setTrackColor(color);
    }

    public void setHandleColor(@ColorInt int color) {
        fastScroller.setHandleColor(color);
    }

    public void setBubbleVisible(boolean visible) {
        fastScroller.setBubbleVisible(visible);
    }

    public void setBubbleColor(@ColorInt int color) {
        fastScroller.setBubbleColor(color);
    }

    public void setBubbleTextColor(@ColorInt int color) {
        fastScroller.setBubbleTextColor(color);
    }

    public void setBubbleTextSize(int size) {
        fastScroller.setBubbleTextSize(size);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        fastScroller.attachRecyclerView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        fastScroller.detachRecyclerView();
        super.onDetachedFromWindow();
    }

    private void layout(Context context, AttributeSet attrs) {
        fastScroller = new FastScroller(context, attrs);
        fastScroller.setId(R.id.fast_scroller);
    }
}
