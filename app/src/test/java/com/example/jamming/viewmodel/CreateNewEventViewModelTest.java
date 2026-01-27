package com.example.jamming.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.view.EventField;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateNewEventViewModelTest {
    private CreateNewEventViewModel viewModel;

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    private AuthRepository mockAuth;

    @Mock
    private EventRepository mockEvent;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new CreateNewEventViewModel(mockAuth, mockEvent);

        when(mockAuth.getCurrentUid()).thenReturn("owner123");

    }

    /**
     * Test 1:
     * Publishing an event with an empty title should set TITLE error
     */
    @Test
    public void publish_emptyTitle_setsTitleError() {
        // Arrange
        viewModel.onLocationSelected(32.0, 34.8, "Tel Aviv");
        viewModel.setDate(2026, 0, 10);
        viewModel.setTime(20, 0);
        viewModel.toggleGenre(MusicGenre.ROCK, true);

        // Act
        viewModel.publish("", "100", "Test description");

        // Assert
        assertEquals(EventField.TITLE, viewModel.getErrorField().getValue());
    }

    /**
     * Test 2:
     * getCheckedGenres should return a correct boolean array
     * according to the selected genres
     */

    @Test
    public void getCheckedGenres_returnsCorrectCheckedArray() {
        // Arrange
        MusicGenre[] allGenres = {MusicGenre.ROCK, MusicGenre.JAZZ, MusicGenre.POP};

        viewModel.toggleGenre(MusicGenre.ROCK, true);
        viewModel.toggleGenre(MusicGenre.POP, true);
        // Act
        boolean[] checked = viewModel.getCheckedGenres(allGenres);

        // Assert
        assertTrue(checked[0]);   // Rock selected
        assertFalse(checked[1]);  // Jazz not selected
        assertTrue(checked[2]);   // Pop selected
    }


    /**
     * Test 3 (with Mock):
     * Valid publish should call repository and succeed
     */
    @Test
    public void publish_validEvent_callsRepositoryAndSucceeds() {
        // Arrange (Mocking dependencies):
        // Mock an asynchronous Firebase Task so the test is independent of Firebase and Android runtime
        Task<Void> mockTask = mock(Task.class);

        // Configure the repository to return the mocked Task when creating an event
        when(mockEvent.createEvent(any(Event.class))).thenReturn(mockTask);

        // Simulate successful and failure callback chaining on the Task
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        // Arrange
        viewModel.onLocationSelected(32.0, 34.8, "Tel Aviv");
        viewModel.setDate(2026, 0, 10);
        viewModel.setTime(20, 0);
        viewModel.toggleGenre(MusicGenre.ROCK, true);

        // Act
        viewModel.publish("Live Concert", "150", "Great music night");

        // Assert
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);

        verify(mockEvent, times(1)).createEvent(captor.capture());

        Event createdEvent = captor.getValue();

        assertEquals("Live Concert", createdEvent.getName());
        assertEquals(150, createdEvent.getMaxCapacity());
        assertEquals("owner123", createdEvent.getOwnerId());

        assertNull(viewModel.getErrorField().getValue());


    }


}
