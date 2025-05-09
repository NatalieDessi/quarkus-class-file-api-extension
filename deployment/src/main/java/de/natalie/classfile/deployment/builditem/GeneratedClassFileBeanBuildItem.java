package de.natalie.classfile.deployment.builditem;

import io.quarkus.arc.Unremovable;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.builder.item.MultiBuildItem;
import jakarta.enterprise.context.Dependent;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

/**
 * A {@link MultiBuildItem} representing a generated Java class which will be registered as a CDI bean at build time.
 * This item is designed for use in Quarkus extensions that generate class bytecode ahead of time
 * (e.g., with the Class-File API) and want to expose that class as a CDI-managed bean.
 *
 * <p>This item is conceptually similar to {@code SyntheticBeanBuildItem}, but instead of defining
 * an existing Java class, it defines a class that will be generated via a {@link ClassBuilder}.
 *
 * <p>Example usage:
 * <pre>{@code
 * GeneratedClassFileBeanBuildItem.builder()
 *                                .unremovable(true)
 *                                .generateConstructor(true)
 *                                .scope(ApplicationScoped.class)
 *                                .classBuilder(ExtensionProcessor::buildHandler)
 *                                .classDesc(ClassDesc.of("de.example.ReplyHandler"))
 *                                .build());
 * }</pre>
 *
 * <p>This extension will:
 * <ul>
 *     <li>Generate the bytecode for the class using the provided {@code classBuilder}</li>
 *     <li>Mark it with the specified CDI scope (default: {@code Dependent})</li>
 *     <li>Mark it as unremovable using the Annotation {@code Unremovable})</li>
 *     <li>Optionally generate a default constructor</li>
 *     <li>Register it as a CDI bean during build time</li>
 * </ul>
 *
 * @see Dependent
 * @see Unremovable
 * @see ClassBuilder
 * @see SyntheticBeanBuildItem
 */
@Getter
@Builder
@RequiredArgsConstructor(access = PRIVATE)
public final class GeneratedClassFileBeanBuildItem extends MultiBuildItem {
    /**
     * The CDI scope under which the generated class should be registered.
     * Defaults to {@code Dependent} if not explicitly set.
     */
    @Builder.Default private final Class<? extends Annotation> scope = Dependent.class;

    /**
     * Whether a default (no-arg) constructor should be automatically generated.
     * This is useful if the generated class does not declare its own constructor.
     */
    private final boolean generateConstructor;

    /**
     * If {@code true}, the bean is marked as unremovable, preventing it from being pruned
     * during unused bean elimination.
     */
    private final boolean unremovable;

    /**
     * The class descriptor (internal name) of the generated class.
     * Example: {@code ClassDesc.of("com.example.MyGeneratedBean")}
     */
    private final ClassDesc classDesc;

    /**
     * A callback that receives a {@link ClassBuilder} and emits the class's bytecode structure.
     * This is typically a method reference such as {@code MyProcessor::buildMyHandler}.
     */
    private final Consumer<ClassBuilder> classBuilder;
}
