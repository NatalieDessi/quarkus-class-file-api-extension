package de.natalie;

import io.quarkus.arc.Unremovable;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.classfile.ClassBuilder;

import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import static java.lang.constant.ConstantDescs.MTD_void;

@UtilityClass
public class ProcessorUtils {
    public static byte[] generateScope(final Class<? extends Annotation> scope, final boolean unremovable, final byte[] bytes) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, 0);

        var annotator = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                visitAnnotation(scope.descriptorString(), true).visitEnd();
                if (unremovable) visitAnnotation(Unremovable.class.descriptorString(), true).visitEnd();
            }
        };

        reader.accept(annotator, 0);
        return writer.toByteArray();
    }

    public static void generateConstructor(final ClassBuilder builder) {
        builder.withMethod(INIT_NAME, MTD_void, ACC_PUBLIC, methodBuilder ->
                methodBuilder.withCode(code -> code.aload(0).invokespecial(CD_Object, INIT_NAME, MTD_void).return_()));
    }
}
