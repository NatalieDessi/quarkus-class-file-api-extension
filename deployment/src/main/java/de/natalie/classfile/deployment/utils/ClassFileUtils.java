package de.natalie.classfile.deployment.utils;

import lombok.experimental.UtilityClass;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;

@UtilityClass
public final class ClassFileUtils {
    public static ClassDesc classDesc(Class<?> type) {
        return type.describeConstable().orElseThrow();
    }

    public static ClassDesc arrayClassDesc(Class<?> type) {
        return classDesc(type.arrayType());
    }

    public static ClassEntry classEntry(ClassBuilder classBuilder, Class<?> type) {
        return classBuilder.constantPool().classEntry(classDesc(type));
    }
}
