package io.romain.passport.ui;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.romain.passport.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Test
	public void mainActivityTest() {
		ViewInteraction viewFlipper = onView(
				allOf(withId(R.id.detected_position_layout), isDisplayed()));
		viewFlipper.perform(click());

		ViewInteraction cardView = onView(
				allOf(withId(R.id.item_city_list_card_view),
						withParent(allOf(withId(android.R.id.list),
								withParent(withId(R.id.city_list_fragment)))),
						isDisplayed()));
		cardView.perform(click());

	}
}
