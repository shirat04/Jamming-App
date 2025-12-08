import com.example.jamming.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    private static EventRepository instance;
    private final List<Event> events = new ArrayList<>();

    private EventRepository() {}

    public static EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    public void addEvent(Event e) {
        events.add(e);
    }
    public void removeEvent(Event e){
        events.remove(e);
    }

    public List<Event> getEventsByOwner(String ownerId) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.getOwnerId().equals(ownerId)) {
                result.add(e);
            }
        }
        return result;
    }

    public List<Event> getEventsByDate(Long date) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.getDateTime() == date) {
                result.add(e);
            }
        }
        return result;
    }




}
}
