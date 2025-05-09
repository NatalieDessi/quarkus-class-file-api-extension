package de.example.deployment;

import de.example.runtime.EventHandler;
import de.example.runtime.EventHandlerRecorder;
import de.natalie.classfile.deployment.builditem.GeneratedClassFileBeanBuildItem;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.core.eventbus.MessageConsumer;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.annotation.Annotation;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import static de.natalie.classfile.deployment.utils.ClassFileUtils.arrayClassDesc;
import static de.natalie.classfile.deployment.utils.ClassFileUtils.classDesc;
import static de.natalie.classfile.deployment.utils.ClassFileUtils.classEntry;
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

    /**
     * Emits the bytecode for the {@code accept} method of the {@code ReplyHandler} class.
     * <p>
     * The generated method corresponds to the following Java (simplified) code:
     *
     * <pre>{@code
     * public void accept(Message<T> message) {
     *     message.reply(message.body());
     * }
     * }</pre>
     *
     * <p>Bytecode steps:
     * <ul>
     *     <li>Loads, casts and stores the method argument (a {@link Message} object).</li>
     *     <li>Calls {@code body()} to get the message content.</li>
     *     <li>Calls {@code reply(...)} with the retrieved body.</li>
     *     <li>Returns void.</li>
     * </ul>
     *
     * <p>The message object is temporarily stored in local variable index 2 after type casting.
     *
     * @param builder the {@link CodeBuilder} used to emit bytecode instructions for the method
     * @see Message
     */
    private static void buildAcceptMethod(CodeBuilder builder) {
        final var messageDesc = classDesc(Message.class);

        final var bodyMethodDesc = MethodTypeDesc.of(CD_Object);
        final var replyMethodDesc = MethodTypeDesc.of(CD_void, CD_Object);

        builder.aload(1)
               .checkcast(messageDesc)
               .astore(2).aload(2)
               .invokevirtual(messageDesc, "body", bodyMethodDesc)
               .aload(2).swap()
               .invokevirtual(messageDesc, "reply", replyMethodDesc)
               .return_();
    }

    /**
     * Emits the bytecode for the {@code registerHandler} method of the {@code ReplyHandler} class.
     * <p>
     * The generated method corresponds to the following (simplified) Java code:
     *
     * <pre>{@code
     * public void registerHandler() {
     *     try (InstanceHandle<EventBus> handle = Arc.container().instance(EventBus.class)) {
     *         handle.get().consumer("channel", this);
     *     }
     * }
     * }</pre>
     *
     * <p>Bytecode steps:
     * <ul>
     *     <li>Access the {@link Arc} container and request an {@link InstanceHandle} for {@link EventBus}.</li>
     *     <li>Retrieve the {@code EventBus} instance and register this object as a consumer on the
     *         {@code "channel"} address.</li>
     *     <li>Close the handle to release CDI resources.</li>
     * </ul>
     *
     * <p>This method makes use of constant loading, interface and virtual method invocations,
     * and array creation for annotation parameters.
     *
     * @param builder the {@link CodeBuilder} used to emit bytecode instructions for the method
     * @see Arc
     * @see EventBus
     * @see ArcContainer
     * @see InstanceHandle
     */
    private static void buildRegisterHandler(CodeBuilder builder) {
        final var arcDesc = classDesc(Arc.class);
        final var eventBusDesc = classDesc(EventBus.class);
        final var consumerDesc = classDesc(Consumer.class);
        final var annotationDesc = classDesc(Annotation.class);
        final var arcContainerDesc = classDesc(ArcContainer.class);
        final var instanceHandleDesc = classDesc(InstanceHandle.class);
        final var messageConsumerDesc = classDesc(MessageConsumer.class);

        final var annotationsDesc = arrayClassDesc(Annotation.class);

        final var getMethodDesc = MethodTypeDesc.of(CD_Object);
        final var containerMethodDesc = MethodTypeDesc.of(arcContainerDesc);
        final var consumerMethodDesc = MethodTypeDesc.of(messageConsumerDesc, CD_String, consumerDesc);
        final var instanceMethodDesc = MethodTypeDesc.of(instanceHandleDesc, CD_Class, annotationsDesc);

        builder.invokestatic(arcDesc, "container", containerMethodDesc)
               .ldc(eventBusDesc)
               .iconst_0()
               .anewarray(annotationDesc)
               .invokeinterface(arcContainerDesc, "instance", instanceMethodDesc)
               .astore(1)
               .aload(1)
               .invokeinterface(instanceHandleDesc, "get", getMethodDesc)
               .checkcast(eventBusDesc)
               .ldc("channel")
               .aload(0)
               .invokevirtual(eventBusDesc, "consumer", consumerMethodDesc)
               .aload(1)
               .invokeinterface(instanceHandleDesc, "close", MTD_void)
               .return_();
    }

    /**
     * Defines the bytecode structure of the {@code ReplyHandler} class using the Class-File API.
     * <p>
     * The resulting class is equivalent to the following Java code:
     *
     * <pre>{@code
     * @ApplicationScoped
     * public class ReplyHandler<T> implements EventHandler, Consumer<Message<T>> {
     *     @Override
     *     public void accept(Message<T> message) {
     *         message.reply(message.body());
     *     }
     *
     *     public void registerHandler() {
     *         try (InstanceHandle<EventBus> handle = Arc.container().instance(EventBus.class)) {
     *             handle.get().consumer("channel", this);
     *         }
     *     }
     * }
     * }</pre>
     *
     * <p>The method configures this structure using the provided {@link ClassBuilder}, including:
     * <ul>
     *     <li>Public access modifier and implementation of {@code EventHandler} and {@code Consumer<Message<T>>}.</li>
     *     <li>Two methods: {@code accept} and {@code registerHandler}, built via helper methods.</li>
     * </ul>
     *
     * @param classBuilder the {@link ClassBuilder} instance used to construct the class
     * @see Arc
     * @see Message
     * @see EventBus
     */
    private static void buildHandler(ClassBuilder classBuilder) {
        final var consumerEntry = classEntry(classBuilder, Consumer.class);
        final var eventHandlerEntry = classEntry(classBuilder, EventHandler.class);

        final var acceptMethodDesc = MethodTypeDesc.of(CD_void, CD_Object);

        classBuilder.withFlags(ACC_PUBLIC)
                    .withSuperclass(CD_Object)
                    .withInterfaces(eventHandlerEntry, consumerEntry)
                    .withMethodBody("accept", acceptMethodDesc, ACC_PUBLIC, ExtensionProcessor::buildAcceptMethod)
                    .withMethodBody("registerHandler", MTD_void, ACC_PUBLIC, ExtensionProcessor::buildRegisterHandler);
    }

    /**
     * Registers the {@code ReplyHandler} class for generation and inclusion as a CDI bean during build time.
     * <p>
     * This build step produces a {@link GeneratedClassFileBeanBuildItem}, which defines a class that will be
     * compiled to bytecode and made available as an unremovable CDI bean with {@link ApplicationScoped} scope.
     * The structure of the class is defined via {@link ExtensionProcessor#buildHandler}.
     *
     * <p>The build item is configured with:
     * <ul>
     *   <li>{@code unremovable(true)} to prevent removal during unused-bean elimination</li>
     *   <li>{@code generateConstructor(true)} to generate a default constructor</li>
     *   <li>{@code scope(ApplicationScoped.class)} for CDI lifecycle control</li>
     *   <li>A {@code classBuilder} callback for bytecode emission</li>
     * </ul>
     *
     * @param generatedClasses the {@link BuildProducer} responsible for collecting generated class build items
     */
    @BuildStep
    void produceClasses(BuildProducer<GeneratedClassFileBeanBuildItem> generatedClasses) {
        generatedClasses.produce(GeneratedClassFileBeanBuildItem.builder()
                                                                .unremovable(true)
                                                                .generateConstructor(true)
                                                                .scope(ApplicationScoped.class)
                                                                .classBuilder(ExtensionProcessor::buildHandler)
                                                                .classDesc(ClassDesc.of("de.example.deployment.ReplyHandler"))
                                                                .build());
    }

    /**
     * Registers all event consumers at runtime using the {@link EventHandlerRecorder}.
     * <p>
     * This method is executed during {@code RUNTIME_INIT} phase and ensures that any generated
     * {@code ReplyHandler} beans are activated and registered on the event bus.
     * <p>
     * It consumes {@link SyntheticBeansRuntimeInitBuildItem} to guarantee execution after synthetic beans
     * have been initialized, ensuring that all generated handlers are present before registration.
     *
     * <p>The actual registration logic is delegated to a {@link Recorder} class, which typically
     * uses runtime APIs (e.g., {@code Arc.container()}, {@code EventBus}) to wire up the handler logic.
     *
     * @param recorder the {@link EventHandlerRecorder} responsible for performing runtime registration logic
     */
    @BuildStep
    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    void registerConsumers(EventHandlerRecorder recorder) {
        recorder.registerConsumers();
    }
}
