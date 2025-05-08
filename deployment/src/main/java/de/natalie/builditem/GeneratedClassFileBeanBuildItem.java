package de.natalie.builditem;

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
