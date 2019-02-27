package ru.ifmo.rain.tihonov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class Implementor implements Impler {
    private StringBuilder result = new StringBuilder();
    private HashSet<Class<?>> imports = new HashSet<>();

    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        result = new StringBuilder();
        imports = new HashSet<>();

        //todo
        if (token == null || !token.isInterface()) {
            throw new ImplerException();
        }

        path = path.
                resolve(token.getPackageName().
                replace('.', File.separatorChar)).
                resolve(token.getSimpleName() + "Impl.java");

        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                //todo
                throw new ImplerException();
            }
        }

        imports.add(token);

        getMethods(token);

        StringBuilder temp = new StringBuilder();
        setPackageAndImports(token.getPackageName(), temp);
        setName(token.getSimpleName(), temp);

        print(temp, path);
    }

    private void setPackageAndImports(String name, StringBuilder temp) {
        if (!name.equals("")) {
            temp.append("package ").append(name).append(";\r\n\r\n");
        }

        for (Class<?> c : imports) {
            if (!c.isPrimitive() && !c.isArray()) {
                temp.append("import ").append(c.getPackageName()).append(".").
                        append(c.getSimpleName()).append(";\r\n");
            }
        }
    }

    private void setName(String name, StringBuilder temp) {
        temp.append("public class ").append(name).append("Impl").
                append(" implements ").append(name).append(" {\r\n\r\n");
    }

    private void print(StringBuilder temp, Path path) throws ImplerException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(temp.toString());
            writer.write(result.toString());
        } catch (IOException e) {
            //todo
            e.printStackTrace();
            throw new ImplerException();
        }
    }

    private void getMethods(Class<?> token) {
        for (Method m : token.getMethods()) {
            getModifiers(m);

            result.append("(");
            getArgs(m.getParameterTypes());
            result.append(")");

            if (m.getExceptionTypes().length != 0) {
                result.append(" throws ");
                getExceptions(m.getExceptionTypes());
            }

            getBody(m.getReturnType());
        }
        result.append("}");
    }

    private void getModifiers(Method m) {
        int mod = m.getModifiers();
        result.append(Modifier.toString(mod & (mod ^ Modifier.ABSTRACT) & (mod ^ Modifier.TRANSIENT))).append(" ");
        result.append(m.getReturnType().getSimpleName()).append(" ").append(m.getName());
        imports.add(m.getReturnType());
    }

    private void getArgs(Class<?>[] params) {
        for (int i = 0; i < params.length; i++) {
            Class<?> c = params[i];
            imports.add(c);
            result.append(c.getSimpleName()).append(" ");

            StringBuilder temp = new StringBuilder(c.getSimpleName());
            while (temp.toString().endsWith("[]")) {
                temp.delete(temp.length() - 2, temp.length());
            }
            result.append(temp.toString().toLowerCase());

            if (i + 1 != params.length) {
                result.append(", ");
            }
        }
    }

    private void getExceptions(Class<?>[] exceptions ) {
        for (int i = 0; i < exceptions.length; i++) {
            imports.add(exceptions[i]);
            result.append(exceptions[i].getSimpleName());
            if (i + 1 != exceptions.length) {
                result.append(", ");
            }
        }
    }

    private void getBody(Class<?> c) {
        result.append(" {\r\n");
        if (c.isPrimitive()) {
            if (!c.equals(void.class)) {
                result.append("return ");
                if (c.equals(boolean.class)) {
                    result.append("false");
                } else {
                    result.append("0");
                }
            }
        } else {
            result.append("return ");
            result.append("null");
        }
        result.append(";\r\n");
        result.append("}\r\n\r\n");
    }

    //todo remove
    public StringBuilder get() {
        return result;
    }
}
