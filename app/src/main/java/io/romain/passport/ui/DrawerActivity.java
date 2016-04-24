package io.romain.passport.ui;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import io.romain.passport.R;
import io.romain.passport.model.User;
import io.romain.passport.ui.drawable.LetterTileDrawable;
import io.romain.passport.utils.glide.CircleTransform;

public abstract class DrawerActivity extends LocationPermissionActivity {

	@Bind(R.id.content_root_view)
	DrawerLayout mDrawerLayout;

	@Bind(R.id.left_frame)
	NavigationView mDrawerNavigation;

	private ActionBarDrawerToggle mDrawerToggle;
	private LayoutInflater mInflater;

	private CircleTransform mCircleTransform;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mAccountManager = AccountManager.get(this);
		mCircleTransform = new CircleTransform(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		setContentView(mInflater.inflate(layoutResID, null));
	}

	@Override
	public void setContentView(View view) {
		setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		ViewGroup v = (ViewGroup) mInflater.inflate(R.layout.activity_drawer_layout, (ViewGroup) findViewById(android.R.id.content), false);

		ViewGroup content = (ViewGroup) v.findViewById(R.id.root_drawer_layout_content);
		content.addView(view, params);

		super.setContentView(v);

		setupDrawer();
	}

	private void setupDrawer() {
		View header = mDrawerNavigation.getHeaderView(0);
		TextView title = (TextView) header.findViewById(R.id.drawer_title);
		TextView subtitle = (TextView) header.findViewById(R.id.drawer_subtitle);
		ImageView icon = (ImageView) header.findViewById(R.id.drawer_image);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.addDrawerListener(mDrawerToggle);
		mDrawerNavigation.setNavigationItemSelectedListener(this::onNavItemSelected);

		User user = User.load(mAccountManager);
		if (user != null) {
			title.setText(user.name());
			subtitle.setText(user.email());
			Glide.with(this)
					.load(user.picture())
					.transform(mCircleTransform)
					.error(LetterTileDrawable.create(getResources(), user.name()))
					.fallback(LetterTileDrawable.create(getResources(), user.name()))
					.into(icon);
		}
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerToggle != null) {
			// Sync the toggle state after onRestoreInstanceState has occurred.
			mDrawerToggle.syncState();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null) {
			// Pass any configuration change to the drawer toggls
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawerNavigation)) {
			mDrawerLayout.closeDrawer(mDrawerNavigation);
		} else {
			super.onBackPressed();
		}
	}

	protected abstract boolean onNavItemSelected(MenuItem item);
}
