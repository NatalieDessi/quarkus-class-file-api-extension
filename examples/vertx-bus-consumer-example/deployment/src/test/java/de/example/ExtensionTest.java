package de.example;

import io.quarkus.test.QuarkusUnitTest;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtensionTest {
    @RegisterExtension
    static final QuarkusUnitTest APPLICATION_ROOT = new QuarkusUnitTest().withApplicationRoot(_ -> { });

    @Inject EventBus eventBus;

    @Test
    public void test() {
        final var message = "Hello World";
        final var result = eventBus.requestAndAwait("channel", message);

        assertEquals(message, result.body());
    }
}
