package com.example.jamming.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.jamming.R;
import com.example.jamming.view.ExploreEventsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExploreEventsActivityTest {

    @Rule
    public ActivityScenarioRule<ExploreEventsActivity> activityRule =
            new ActivityScenarioRule<>(ExploreEventsActivity.class);


    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            getPermissionsToGrant()
    );


    @Test
    public void testMapContainerIsDisplayed() {
         // המתנה ארוכה לטעינת המפה באמולטור איטי
        onView(withId(R.id.mapContainer))
                .check(matches(isDisplayed()));
    }




    @Test
    public void testFilterButtonsAreVisible() {

        onView(withId(R.id.filterDistance)).check(matches(isDisplayed()));
        onView(withId(R.id.filterDate)).check(matches(isDisplayed()));
        onView(withId(R.id.filterTime)).check(matches(isDisplayed()));
        onView(withId(R.id.filterMusic)).check(matches(isDisplayed()));
        onView(withId(R.id.filterCapacity)).check(matches(isDisplayed()));
    }





    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String[] getPermissionsToGrant() {
        List<String> permissions = new ArrayList<>();
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS);
        }
        return permissions.toArray(new String[0]);
    }
}