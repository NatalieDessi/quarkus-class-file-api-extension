package de.example;

import de.natalie.builditem.GeneratedClassFileBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.classfile.ClassBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.CD_String;

class ExtensionProcessor {
    private static final String FEATURE = "extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    private static void buildInvoker(ClassBuilder classBuilder) {
        final var constantPool = classBuilder.constantPool();
        final var producerInterface = constantPool.classEntry(Producer.class.describeConstable().orElseThrow());
        classBuilder.withInterfaces(producerInterface);

        classBuilder.withMethod("produce", MethodTypeDesc.of(CD_String), ACC_PUBLIC, methodBuilder ->
                methodBuilder.withCode(codeBuilder -> codeBuilder.ldc("Pong!").areturn()));
    }

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
}
