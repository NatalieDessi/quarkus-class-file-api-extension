package de.example;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class EventHandlerRecorder {
    public void registerConsumers() {
        final var handlers = Arc.container().listAll(EventHandler.class);
        for (final var handler : handlers) {
            try (final var instance = handler) {
                instance.get().registerHandler();
            }
        }
    }
}
