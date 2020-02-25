package my.artfultom.vecenta.transport;

import java.util.ArrayList;
import java.util.List;

public class Envelope {
    private List<Message> messages = new ArrayList<>();

    public Envelope() {
    }

    public Envelope(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
