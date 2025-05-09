package de.example.deployment;

import de.example.runtime.Producer;
import de.natalie.classfile.deployment.builditem.GeneratedClassFileBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.classfile.ClassBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Supplier;

import static de.natalie.classfile.deployment.utils.ClassFileUtils.classEntry;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.CD_String;

class ExtensionProcessor {
    private static final String FEATURE = "extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    /**
     * Generates the bytecode for a simple class that implements the {@link Producer} interface,
     * overriding the {@code produce()} method to return a static string.
     *
     * <p>The resulting class is equivalent to the following Java code:
     *
     * <pre>{@code
     * @ApplicationScoped
     * public class Ping implements Producer {
     *     @Override
     *     public String produce() {
     *         return "Pong!";
     *     }
     * }
     * }</pre>
     *
     * <p>This method:
     * <ul>
     *     <li>Registers the {@link Producer} interface on the class.</li>
     *     <li>Defines a public method {@code produce()} with return type {@code String}.</li>
     *     <li>Generates a constant return of {@code "Pong!"} as bytecode.</li>
     * </ul>
     *
     * <p>This kind of generated class is useful in scenarios like bean proxying, default interface
     * implementations, or lightweight service registrations.
     *
     * @param classBuilder the {@link ClassBuilder} used to define the interface and method structure
     * @see Supplier
     * @see ClassBuilder
     */
    private static void buildInvoker(ClassBuilder classBuilder) {
        final var producerInterface = classEntry(classBuilder, Producer.class);
        final var producerMethodDesc = MethodTypeDesc.of(CD_String);

        classBuilder.withInterfaces(producerInterface)
                    .withMethod("produce", producerMethodDesc, ACC_PUBLIC,
                                methodBuilder -> methodBuilder.withCode(codeBuilder -> codeBuilder.ldc("Pong!").areturn()));
    }

    /**
     * Registers the generated {@code Ping} class as a CDI bean during build time.
     *
     * <p>This build step uses {@link GeneratedClassFileBeanBuildItem} to produce a class that
     * implements the {@link Producer} interface, returning a static string from the {@code produce()} method.
     *
     * <p>The build item is configured with:
     * <ul>
     *     <li>{@code @ApplicationScoped} for CDI lifecycle management</li>
     *     <li>{@code unremovable} to prevent removal during optimization</li>
     *     <li>Automatically equipped with a no-arg constructor</li>
     * </ul>
     *
     * <p>This allows the {@code Ping} class to be injected wherever the {@code Producer} interface is required.
     *
     * @param generatedClasses the {@link BuildProducer} that collects build items for generated classes
     * @see GeneratedClassFileBeanBuildItem
     * @see Producer
     */
    @BuildStep
    void produceClasses(BuildProducer<GeneratedClassFileBeanBuildItem> generatedClasses) {
        generatedClasses.produce(GeneratedClassFileBeanBuildItem.builder()
                                                                .unremovable(true)
                                                                .generateConstructor(true)
                                                                .scope(ApplicationScoped.class)
                                                                .classDesc(ClassDesc.of("de.example.Ping"))
                                                                .classBuilder(ExtensionProcessor::buildInvoker)
                                                                .build());
    }
}
