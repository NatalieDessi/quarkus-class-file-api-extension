package de.natalie;

import de.natalie.classfile.example.extension.runtime.Invokable;
import io.quarkus.arc.Arc;
import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ExtensionTest {
    @RegisterExtension
    static final QuarkusUnitTest APPLICATION_ROOT = new QuarkusUnitTest().withApplicationRoot(_ -> { });

    @Test
    public void test() throws ClassNotFoundException {
        final var clazz = Class.forName("de.natalie.Ping");
        try (final var bean = Arc.container().instance(clazz)) {
            ((Invokable) bean.get()).invoke();
        }
    }
}
