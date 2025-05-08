package de.example;

import io.quarkus.arc.Arc;
import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtensionTest {
    @RegisterExtension
    static final QuarkusUnitTest extension = new QuarkusUnitTest().withApplicationRoot(_ -> { });

    @Test
    public void test() throws ClassNotFoundException {
        final var producerClass = Class.forName("de.example.Ping");

        try (final var instance = Arc.container().instance(producerClass)) {
            final var producer = (Producer) instance.get();
            assertEquals("Pong!", producer.produce());
        }
    }
}
