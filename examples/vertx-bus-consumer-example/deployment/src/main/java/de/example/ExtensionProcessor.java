package de.example;

import de.natalie.builditem.GeneratedClassFileBeanBuildItem;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.core.eventbus.MessageConsumer;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.annotation.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.MTD_void;

class ExtensionProcessor {
    private static final String FEATURE = "extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    private static void buildHandler(final ClassDesc classDesc, ClassBuilder classBuilder) {
        ClassDesc eventBusDesc = EventBus.class.describeConstable().orElseThrow();
        ClassDesc eventHandlerDesc = EventHandler.class.describeConstable().orElseThrow();
        ClassDesc consumerDesc = Consumer.class.describeConstable().orElseThrow();
        ClassDesc messageDesc = Message.class.describeConstable().orElseThrow();
        ClassDesc messageConsumerDesc = MessageConsumer.class.describeConstable().orElseThrow();
        ClassDesc arcDesc = Arc.class.describeConstable().orElseThrow();
        ClassDesc arcContainerDesc = ArcContainer.class.describeConstable().orElseThrow();
        ClassDesc instanceHandleDesc = InstanceHandle.class.describeConstable().orElseThrow();
        ClassDesc annotationsDesc = Annotation.class.arrayType().describeConstable().orElseThrow();
        ClassEntry eventHandlerEntry = classBuilder.constantPool().classEntry(eventHandlerDesc);
        ClassEntry consumerEntry = classBuilder.constantPool().classEntry(consumerDesc);

        classBuilder.withFlags(ACC_PUBLIC)
                    .withSuperclass(CD_Object)
                    .withInterfaces(eventHandlerEntry, consumerEntry)

                    .withMethodBody("<init>", MethodTypeDesc.of(CD_void, eventBusDesc), ACC_PUBLIC, code -> code
                            .aload(0)
                            .invokespecial(CD_Object, "<init>", MTD_void)
                            .aload(0)
                            .aload(1)
                            .putfield(classDesc, "eventBus", eventBusDesc)
                            .return_())

                    // private void handle(Message message) { message.reply(message.body()); }
                    .withMethodBody("accept", MethodTypeDesc.of(CD_void, CD_Object), ACC_PUBLIC,
                                    code -> code.aload(1)
                                                .checkcast(messageDesc)
                                                .astore(2).aload(2)
                                                .invokevirtual(messageDesc, "body", MethodTypeDesc.of(CD_Object))
                                                .aload(2).swap()
                                                .invokevirtual(messageDesc, "reply", MethodTypeDesc.of(CD_void, CD_Object))
                                                .return_())

                    // public void registerHandler() { Arc.container().instance(EventBus.class).get().consumer("channel", this::handle); }
                    .withMethodBody("registerHandler", MTD_void, ACC_PUBLIC,
                                    code -> code.invokestatic(arcDesc, "container", MethodTypeDesc.of(arcContainerDesc))
                                                .ldc(eventBusDesc)
                                                .iconst_0()
                                                .anewarray(ClassDesc.of("java.lang.annotation.Annotation"))
                                                .invokeinterface(arcContainerDesc, "instance",
                                                                 MethodTypeDesc.of(instanceHandleDesc, CD_Class, annotationsDesc))

                                                .astore(1)
                                                .aload(1)
                                                .invokeinterface(instanceHandleDesc, "get", MethodTypeDesc.of(CD_Object))
                                                .checkcast(eventBusDesc)
                                                .ldc("channel")
                                                .aload(0)
                                                .invokevirtual(eventBusDesc, "consumer",
                                                               MethodTypeDesc.of(messageConsumerDesc, CD_String, consumerDesc))
                                                .aload(1)
                                                .invokeinterface(instanceHandleDesc, "close", MethodTypeDesc.of(CD_void))

                                                .return_());
    }

    @BuildStep
    void produceClasses(BuildProducer<GeneratedClassFileBeanBuildItem> generatedClasses) {
        final var classDesc = ClassDesc.of("de.example", "ReplyHandler");
        generatedClasses.produce(GeneratedClassFileBeanBuildItem.builder()
                                                                .unremovable(true)
                                                                .generateConstructor(true)
                                                                .scope(ApplicationScoped.class)
                                                                .classDesc(classDesc)
                                                                .classBuilder(classBuilder -> buildHandler(classDesc, classBuilder))
                                                                .build());
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    void registerConsumers(EventHandlerRecorder recorder) {
        recorder.registerConsumers();
    }
}
