# Class-File API CDI Bean Generator (Preview)

This Quarkus extension provides a build item for generating CDI beans using the
new [Java Class-File API (JEP 484)](https://openjdk.org/jeps/484), introduced with Java 24.

## 🧱 What It Does

The extension defines a single build item: `GeneratedClassFileBeanBuildItem`.
It enables developers to generate complete Java classes (typically CDI beans) directly as bytecode using the modern and
type-safe Class-File API provided in Java 24.

The structure and semantics of the build item are modeled after Quarkus’s `SyntheticBeanBuildItem`, with specific
adaptation for bytecode-level class definitions.

## ✅ Use Cases

* Generating lightweight or proxy-style beans at build time.
* Replacing bytecode libraries like ASM or Gizmo with native Java 24 APIs.
* Producing classes for AOT scenarios in Quarkus extensions or frameworks.
* Experimenting with the modern Class-File API in a real-world context.

## 🔧 Requirements

* **Java 24** (required for the Class-File API)
* **Quarkus 3.22.2**

## ❌ Limitations

The current Class-File API (as of Java 24) **does not support annotation generation**.
This means annotations like `@ApplicationScoped`, `@Inject`, etc., **must still be added using a secondary tool**
like [Gizmo](https://github.com/quarkusio/gizmo) or [ASM](https://asm.ow2.io/).

> For example: CDI scope annotations (like `@ApplicationScoped`) are currently added via Gizmo fallback.

This is a known limitation of JEP 484 and may be addressed in future iterations of the API.

## 🧪 Status

* This project is in **preview**.
* No stable release is available yet, but **a first release is planned soon**.
* API and package structure may still change slightly before 1.0.0.

## 💡 Example Usage

```java
@BuildStep
void produceClasses(BuildProducer<GeneratedClassFileBeanBuildItem> generatedClasses) {
    generatedClasses.produce(GeneratedClassFileBeanBuildItem.builder()
                                                            .unremovable(true)
                                                            .generateConstructor(true)
                                                            .scope(ApplicationScoped.class)
                                                            .classDesc(ClassDesc.of("de.example", "Ping"))
                                                            .classBuilder(ExtensionProcessor::buildInvoker)
                                                            .build());
}
```

This example generates a `Ping` class implementing a simple interface and returns `"Pong!"` from a method.
The resulting class is registered as a CDI bean with `@ApplicationScoped` scope.

## 📁 Examples

You can find working usage examples in the `examples/` folder:

* [`examples/interface-implementation-example`](examples/interface-implementation-example)
  Shows how to generate a class that implements a simple Java interface.

* [`examples/vertx-bus-consumer-example`](examples/vertx-bus-consumer-example)
  Demonstrates generating and registering a Vert.x EventBus consumer using Quarkus and the Class-File API.

These examples are useful starting points for extension authors and anyone exploring JEP 484 in practice.

## 📦 Build Item Structure

```java
@Getter
@Builder
@RequiredArgsConstructor(access = PRIVATE)
public final class GeneratedClassFileBeanBuildItem extends MultiBuildItem {
    @Builder.Default private final Class<? extends Annotation> scope = Dependent.class;
    private final boolean generateConstructor;
    private final boolean unremovable;
    private final ClassDesc classDesc;
    private final Consumer<ClassBuilder> classBuilder;
}
```

## 🛠️ Goals

* Provide a developer-friendly path to using the Class-File API in Quarkus.
* Offer a modern alternative to low-level bytecode manipulation libraries.
* Keep class generation declarative and aligned with CDI/Quarkus patterns.

## 🗘️ Roadmap

* ✅ Initial prototype with basic class generation support
* ⏳ Annotation support without fallback-library
* ⏳ First stable release with Java 25 GA

## 📣 Feedback

This project is actively evolving.
Feedback, issues, and suggestions are very welcome!

Please note that since this relies on Java 24, behavior and APIs may change until the next lts release (Java 25).
