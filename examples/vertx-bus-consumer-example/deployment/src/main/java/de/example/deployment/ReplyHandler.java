package de.example.deployment;

import de.example.runtime.EventHandler;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;

import java.util.function.Consumer;

public class ReplyHandler<T> implements EventHandler, Consumer<Message<T>> {
    public void accept(Message<T> message) {
        message.reply(message.body());
    }

    public void registerHandler() {
        try (InstanceHandle<EventBus> handle = Arc.container().instance(EventBus.class)) {
            handle.get().consumer("channel", this);
        }
    }
}
