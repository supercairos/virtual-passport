package io.romain.passport.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import io.romain.passport.R;
import io.romain.passport.ui.drawable.LetterTileDrawable;
import io.romain.passport.utils.glide.CircleTransform;

public abstract class DrawerActivity extends LocationPermissionActivity {

	@BindView(R.id.content_root_view)
	@SuppressWarnings("WeakerAccess")
	DrawerLayout mDrawerLayout;

	@BindView(R.id.left_frame)
	@SuppressWarnings("WeakerAccess")
	NavigationView mDrawerNavigation;

	private ActionBarDrawerToggle mDrawerToggle;
	private LayoutInflater mInflater;

	private CircleTransform mCircleTransform;
	private TextView mTitleView;
	private TextView mSubtitleView;
	private ImageView mIconView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		mTitleView = (TextView) header.findViewById(R.id.drawer_title);
		mSubtitleView = (TextView) header.findViewById(R.id.drawer_subtitle);
		mIconView = (ImageView) header.findViewById(R.id.drawer_image);

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
	void onUserSignedIn(FirebaseUser user) {
		mTitleView.setText(user.getDisplayName());
		mSubtitleView.setText(user.getEmail());
		Glide.with(this)
				.load(user.getPhotoUrl())
				.transform(mCircleTransform)
				.error(LetterTileDrawable.create(getResources(), user.getDisplayName()))
				.fallback(LetterTileDrawable.create(getResources(), user.getEmail()))
				.into(mIconView);
	}

	@Override
	void onUserSignedOut() {
		LandingActivity.start(this);
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
