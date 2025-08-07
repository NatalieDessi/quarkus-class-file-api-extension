package de.natalie.classfile.deployment.utils;

import de.natalie.classfile.deployment.postvisitors.ClassAnnotationVisitor;
import de.natalie.classfile.deployment.postvisitors.MethodAnnotationVisitor;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.annotation.Annotation;
import java.lang.classfile.ClassBuilder;
import java.util.List;
import java.util.Map;

import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import static java.lang.constant.ConstantDescs.MTD_void;

@UtilityClass
public class ProcessorUtils {
    public static byte[] generateClassAnnotations(final List<Class<? extends Annotation>> annotations, final byte[] bytes) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, 0);
        var annotator = new ClassAnnotationVisitor(writer, annotations);

        reader.accept(annotator, 0);

        return writer.toByteArray();
    }

    public static byte[] generateMethodAnnotations(final Map<String, Class<? extends Annotation>> annotatedMethods, final byte[] bytes) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, 0);
        var annotator = new MethodAnnotationVisitor(writer, annotatedMethods);

        reader.accept(annotator, 0);

        return writer.toByteArray();
    }

    public static void generateConstructor(final ClassBuilder builder) {
        builder.withMethod(INIT_NAME, MTD_void, ACC_PUBLIC, methodBuilder ->
                methodBuilder.withCode(code -> code.aload(0).invokespecial(CD_Object, INIT_NAME, MTD_void).return_()));
    }
}
