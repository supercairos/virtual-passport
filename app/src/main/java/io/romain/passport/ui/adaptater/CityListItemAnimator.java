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
package io.romain.passport.ui.adaptater;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;

public class CityListItemAnimator extends SimpleItemAnimator {

	@Override
	public boolean animateRemove(RecyclerView.ViewHolder holder) {
		return false;
	}

	@Override
	public boolean animateAdd(RecyclerView.ViewHolder holder) {
		return false;
	}

	@Override
	public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
		return false;
	}

	@Override
	public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
		return false;
	}

	@Override
	public void runPendingAnimations() {

	}

	@Override
	public void endAnimation(RecyclerView.ViewHolder item) {

	}

	@Override
	public void endAnimations() {

	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
