package de.natalie.classfile.deployment;

import de.natalie.classfile.deployment.builditem.GeneratedClassFileBeanBuildItem;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.classfile.ClassFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static de.natalie.classfile.deployment.utils.ProcessorUtils.generateClassAnnotations;
import static de.natalie.classfile.deployment.utils.ProcessorUtils.generateConstructor;
import static de.natalie.classfile.deployment.utils.ProcessorUtils.generateMethodAnnotations;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
class QuarkusClassFileApiExtensionProcessor {
    private static final String FEATURE = "quarkus-class-file-api-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerGeneratedBeans(List<GeneratedClassFileBeanBuildItem> generatedBeans,
                                BuildProducer<GeneratedBeanBuildItem> producer,
                                OutputTargetBuildItem outputTargetBuildItem) {
        generatedBeans.forEach(bean -> {
            final var desc = bean.classDesc();
            final var builder = bean.classBuilder();
            final var bytes = ClassFile.of().build(desc, classBuilder -> {
                if (nonNull(builder)) builder.accept(classBuilder);
                if (bean.generateConstructor()) generateConstructor(classBuilder);
            });

            final var classAnnotations = new ArrayList<Class<? extends Annotation>>();
            if (bean.unremovable()) classAnnotations.add(Unremovable.class);
            if (nonNull(bean.scope())) classAnnotations.add(bean.scope());

            final var classAnnotatedBytes = generateClassAnnotations(classAnnotations, bytes);
            final var methodAnnotatedBytes = generateMethodAnnotations(bean.annotatedMethods(), classAnnotatedBytes);

            if (bean.outputGeneratedFile()) {
                final var outputDir = outputTargetBuildItem.getOutputDirectory()
                                                           .resolve("generated-classes")
                                                           .resolve(desc.packageName().replace('.', '/'));
                final var fileName = format("%s.class", desc.displayName());

                try {
                    Files.createDirectories(outputDir);
                    Files.write(outputDir.resolve(fileName), methodAnnotatedBytes);
                } catch (IOException exception) {
                    log.error("Cannot write output class file", exception);
                }
            }

            producer.produce(new GeneratedBeanBuildItem(format("%s.%s", desc.packageName(), desc.displayName()), methodAnnotatedBytes));
        });
    }
}
