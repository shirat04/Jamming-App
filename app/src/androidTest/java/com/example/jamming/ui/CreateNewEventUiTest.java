package com.example.jamming.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.example.jamming.uihelpers.DialogHelpers.clickPositive;
import static com.example.jamming.uihelpers.EditTextAssertions.hasNoError;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.jamming.R;
import com.example.jamming.fakes.FakeAuthRepository;
import com.example.jamming.fakes.FakeEventRepository;
import com.example.jamming.uihelpers.LocationTestData;
import com.example.jamming.view.CreateNewEventActivity;
import com.example.jamming.viewmodel.CreateNewEventViewModel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateNewEventUiTest {

    @Rule
    public ActivityScenarioRule<CreateNewEventActivity> rule =
            new ActivityScenarioRule<>(CreateNewEventActivity.class);

    // Screen-specific setup (kept inside this test file intentionally)

    private void injectTestingViewModel(
            FakeAuthRepository auth,
            FakeEventRepository repo
    ) {
        CreateNewEventViewModel vm =
                new CreateNewEventViewModel(auth, repo);

        rule.getScenario().onActivity(activity ->
                activity.setTestingViewModel(vm)
        );
    }


    // Atomic helpers – one action each

    private void fillName(String name) {
        onView(withId(R.id.eventNameInput))
                .perform(replaceText(name), closeSoftKeyboard());
    }

    private void fillCapacity(String capacity) {
        onView(withId(R.id.eventCapacityInput))
                .perform(replaceText(capacity), closeSoftKeyboard());
    }

    private void fillDescription(String description) {
        onView(withId(R.id.eventDescriptionInput))
                .perform(replaceText(description), closeSoftKeyboard());
    }
    private void simulateLocationSelection(LocationTestData.LocationData location) {
        rule.getScenario().onActivity(activity ->
                activity.getViewModel().onLocationSelected(
                        location.lat,
                        location.lng,
                        location.address
                )
        );
    }

    private void pickDate() {
        onView(withId(R.id.dateInput)).perform(click());
        clickPositive();
    }

    private void pickTime() {
        onView(withId(R.id.timeInput)).perform(click());
        clickPositive();
    }

    private void pickGenre(String genreText) {
        onView(withId(R.id.genreSpinner)).perform(click());
        onView(withText(genreText)).perform(click());
        clickPositive();
    }

    private void clickPublish() {
        onView(withId(R.id.publishEventBtn)).perform(click());
    }


    /**
     * UI Test 1:
     * Filling all required fields and clicking publish should complete the flow successfully.
     */
    @Test
    public void createEvent_success_activityFinishes() throws InterruptedException{

        // Arrange
        injectTestingViewModel(
                new FakeAuthRepository("test-owner-id"),
                new FakeEventRepository().succeed()
        );


        // Act
        simulateLocationSelection(LocationTestData.TEL_AVIV);
        fillName("Jam Night");
        pickDate();
        pickTime();
        pickGenre("Rock");
        fillCapacity("80");
        fillDescription("Live music event");
        clickPublish();
        Thread.sleep(500);
        // Assert
        rule.getScenario().onActivity(activity ->
                org.junit.Assert.assertTrue(activity.isFinishing())
        );
    }

    /**
     * UI Test 2:
     * Submitting an empty form should show a validation error,
     * and fixing the input should clear the error.
     */
    @Test
    public void createEvent_emptyThenFixName_errorAppearsAndClears() {

        // Arrange
        injectTestingViewModel(
                new FakeAuthRepository("test-owner-id"),
                new FakeEventRepository().succeed());

        // Act 1: submit empty form
        clickPublish();

        // Assert 1: name validation error appears
        onView(withId(R.id.eventNameInput))
                .check(matches(
                        androidx.test.espresso.matcher.ViewMatchers
                                .hasErrorText("נא להזין שם אירוע")
                ));

        // Act 2: user fixes the name
        fillName("Jam Night");

        // Assert 2: error is fully cleared (no icon, no message)
        onView(withId(R.id.eventNameInput))
                .check(hasNoError());
    }

    /**
     * UI Test 3:
     * Entering an invalid capacity value should display a capacity validation error.
     */
    @Test
    public void createEvent_invalidCapacity_showsCapacityError() {

        // Arrange
        injectTestingViewModel(
                new FakeAuthRepository("test-owner-id"),
                new FakeEventRepository().succeed()
        );


        // Act
        fillName("Jam Night");
        simulateLocationSelection(LocationTestData.TEL_AVIV);
        pickDate();
        pickTime();
        pickGenre("Rock");
        fillCapacity("-5");
        clickPublish();

        // Assert
        onView(withId(R.id.eventCapacityInput))
                .check(matches(
                        androidx.test.espresso.matcher.ViewMatchers
                                .hasErrorText("קיבולת לא תקינה")
                ));
    }
}
