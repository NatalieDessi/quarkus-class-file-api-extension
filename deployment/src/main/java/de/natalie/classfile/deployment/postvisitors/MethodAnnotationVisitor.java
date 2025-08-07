package de.natalie.classfile.deployment.postvisitors;

import io.quarkus.runtime.Startup;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.annotation.Annotation;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM9;

public class MethodAnnotationVisitor extends ClassVisitor {
    private final Map<String, Class<? extends Annotation>> annotatedMethods;

    public MethodAnnotationVisitor(ClassWriter writer, Map<String, Class<? extends Annotation>> annotatedMethods) {
        super(ASM9, writer);
        this.annotatedMethods = annotatedMethods;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        var visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (!annotatedMethods.containsKey(name)) return visitor;

        return new MethodVisitor(api, visitor) {
            @Override
            public void visitCode() {
                visitAnnotation(Startup.class.descriptorString(), true).visitEnd();
                super.visitCode();
            }
        };
    }
}
