package de.natalie;

import de.natalie.builditem.GeneratedClassFileBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.util.List;

import static de.natalie.ProcessorUtils.generateConstructor;
import static de.natalie.ProcessorUtils.generateScope;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

class QuarkusClassFileApiExtensionProcessor {

    private static final String FEATURE = "quarkus-class-file-api-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerGeneratedBeans(List<GeneratedClassFileBeanBuildItem> generatedBeans,
                                BuildProducer<GeneratedBeanBuildItem> producer) {
        generatedBeans.forEach(bean -> {
            final var desc = bean.classDesc();
            final var builder = bean.classBuilder();
            final var bytes = ClassFile.of().build(desc, classBuilder -> {
                if (nonNull(builder)) builder.accept(classBuilder);
                if (bean.generateConstructor()) generateConstructor(classBuilder);
            });
            final var scopedBytes = nonNull(bean.scope()) ? generateScope(bean.scope(), bean.unremovable(), bytes) : bytes;
            producer.produce(new GeneratedBeanBuildItem(format("%s.%s", desc.packageName(), desc.displayName()), scopedBytes));
        });
    }
}
