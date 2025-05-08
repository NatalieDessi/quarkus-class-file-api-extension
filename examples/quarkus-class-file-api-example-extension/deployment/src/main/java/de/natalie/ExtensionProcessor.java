package de.natalie;

import de.natalie.builditem.GeneratedClassFileBeanBuildItem;
import de.natalie.classfile.example.extension.runtime.Invokable;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.PrintStream;
import java.lang.classfile.ClassBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.MTD_void;

class ExtensionProcessor {
    private static final String FEATURE = "extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    private static void buildInvoker(ClassBuilder classBuilder) {
        final var constantPool = classBuilder.constantPool();
        final var system = System.class.describeConstable().orElseThrow();
        final var printStream = PrintStream.class.describeConstable().orElseThrow();

        final var invokable = constantPool.classEntry(Invokable.class.describeConstable().orElseThrow());
        classBuilder.withInterfaces(invokable);

        final var ping = constantPool.stringEntry("ping");
        final var outField = constantPool.fieldRefEntry(system, "out", printStream);
        final var println = constantPool.methodRefEntry(printStream, "println", MethodTypeDesc.of(CD_void, CD_String));
        classBuilder.withMethod("invoke", MTD_void, ACC_PUBLIC, methodBuilder ->
                methodBuilder.withCode(codeBuilder -> codeBuilder.getstatic(outField)
                                                                 .ldc(ping)
                                                                 .invokevirtual(println)
                                                                 .return_()));
    }

    @BuildStep
    void produceClasses(BuildProducer<GeneratedClassFileBeanBuildItem> generatedClasses) {
        generatedClasses.produce(GeneratedClassFileBeanBuildItem.builder()
                                                                .unremovable(true)
                                                                .generateConstructor(true)
                                                                .scope(ApplicationScoped.class)
                                                                .classDesc(ClassDesc.of("de.natalie", "Ping"))
                                                                .classBuilder(ExtensionProcessor::buildInvoker)
                                                                .build());
    }
}
