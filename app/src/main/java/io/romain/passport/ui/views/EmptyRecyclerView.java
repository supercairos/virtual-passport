/*
 *    Copyright 2016 Romain
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.romain.passport.ui.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class EmptyRecyclerView extends RecyclerView {

	private View mEmptyView;

	private AdapterDataObserver mEmptyObserver = new AdapterDataObserver() {

		@Override
		public void onChanged() {
			Adapter<?> adapter = getAdapter();
			if (adapter != null && mEmptyView != null) {
				if (adapter.getItemCount() == 0) {
					mEmptyView.setVisibility(View.VISIBLE);
					setVisibility(View.GONE);
				} else {
					mEmptyView.setVisibility(View.GONE);
					setVisibility(View.VISIBLE);
				}
			}

		}
	};

	public EmptyRecyclerView(Context context) {
		super(context);
	}

	public EmptyRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
		if (adapter != null) {
			adapter.registerAdapterDataObserver(mEmptyObserver);
		}

		mEmptyObserver.onChanged();
	}

	public void setEmptyView(View view) {
		mEmptyView = view;
	}

}
