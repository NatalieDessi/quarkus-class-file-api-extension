package de.natalie.classfile.deployment.postvisitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM9;

public class ClassAnnotationVisitor extends ClassVisitor {
    private final List<Class<? extends Annotation>> annotations;

    public ClassAnnotationVisitor(ClassWriter writer, List<Class<? extends Annotation>> annotations) {
        super(ASM9, writer);
        this.annotations = annotations;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        for (Class<? extends Annotation> annotation : annotations) {
            visitAnnotation(annotation.descriptorString(), true).visitEnd();
        }
    }
}
