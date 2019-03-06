package ru.ifmo.rain.tihonov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Implementor implements Impler {
    private boolean containsNonPrivateConstructor = false;
    private StringBuilder result;
    private Set<Executable> methodsSet;
    private Set<String> methodsTitle;
    private String newLine = System.lineSeparator();
    private String tab = "    ";

    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        result = new StringBuilder();
        methodsSet = new HashSet<>();
        methodsTitle = new HashSet<>();

        if (token == null || path == null) {
            throw new ImplerException("Arguments can't be null");
        }

        //todo not all
        if (token.equals(void.class) || token.equals(Enum.class) || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("We can't extends from Enum or void");
        }

        try {
            path = path.resolve(token.getPackageName().
                    replace('.', File.separatorChar)).
                    resolve(token.getSimpleName() + "Impl.java");
        } catch (InvalidPathException e) {
            throw new ImplerException("Path is invalid" + e.getMessage());
        }

        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can not create directories: " + e.getMessage());
            }
        }

        findMethods(token);
        methodsSet.addAll(Arrays.asList(token.getDeclaredConstructors()));

        setMethods(token);
        result.append("}");

        StringBuilder temp = new StringBuilder();
        setPackage(token.getPackageName(), temp);
        setTitle(token, temp);

        //todo
        if (!containsNonPrivateConstructor && !token.isInterface()) {
            throw new ImplerException("");
        }

        print(temp, path);
    }

    private void setPackage(String name, StringBuilder temp) {
        if (!name.isEmpty()) {
            temp.append("package ").append(name).append(";").append(newLine).append(newLine);
        }
    }

    private void setTitle(Class<?> token, StringBuilder temp) {
        temp.append("public class ").append(token.getSimpleName()).append("Impl").
                append((token.isInterface() ? " implements " : " extends ")).
                append(token.getCanonicalName()).append(" {").append(newLine).append(newLine);
    }

    private void print(StringBuilder temp, Path path) throws ImplerException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(temp.toString());
            writer.write(result.toString());
        } catch (IOException e) {
            throw new ImplerException("IO error while saving result: " + e.getMessage());
        }
    }

    private void findMethods(Class<?> token) {
        for (Method method : token.getDeclaredMethods()) {
            String title = method.getName() + Arrays.toString(method.getParameterTypes());
            if (!methodsTitle.contains(title)) {
                methodsTitle.add(title);
                methodsSet.add(method);
            }
        }

        if ((token.getModifiers() & Modifier.ABSTRACT) != 0) {
            for (Class<?> c : token.getInterfaces()) {
                findMethods(c);
            }
        }

        if (token.getSuperclass() != null) {
            findMethods(token.getSuperclass());
        }
    }

    private void setMethods(Class<?> token) {
        for (Executable m : methodsSet) {
            if ((m.getModifiers() & (Modifier.PRIVATE | Modifier.FINAL | Modifier.NATIVE | Modifier.SYNCHRONIZED)) != 0) {
                continue;
            }

            setTitle(token, m);
            if (m instanceof Constructor) {
                setConstructorBody(m);
            } else if (m instanceof Method) {
                setMethodBody(((Method) m).getReturnType());
            }
        }
    }

    private void setTitle(Class<?> token, Executable m) {
        result.append(tab);
        if (m instanceof Constructor) {
            result.append(token.getSimpleName()).append("Impl");
        } else if (m instanceof Method) {
            setModifiers((Method) m);
            Class<?> returnType = ((Method) m).getReturnType();
            result.append(returnType.getCanonicalName()).append(" ").append(m.getName());
        }

        result.append("(");
        setArgs(m.getParameters());
        result.append(")");

        if (m.getExceptionTypes().length != 0) {
            result.append(" throws ");
            setExceptions(m.getExceptionTypes());
        }
    }

    private void setModifiers(Method m) {
        int mod = m.getModifiers();
        result.append(Modifier.toString(mod & (mod ^ Modifier.ABSTRACT) & (mod ^ Modifier.TRANSIENT))).append(" ");
    }

    private void setArgs(Parameter[] params) {
        for (int i = 0; i < params.length; i++) {
            Parameter c = params[i];
            result.append(c.getType().getCanonicalName()).append(" ").append(c.getName());
            if (i + 1 != params.length) {
                result.append(", ");
            }
        }
    }

    private void setExceptions(Class<?>[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            result.append(exceptions[i].getCanonicalName());
            if (i + 1 != exceptions.length) {
                result.append(", ");
            }
        }
    }

    private void setConstructorBody(Executable m) {
        if (Modifier.isPrivate(m.getModifiers())) {
            return;
        }
        containsNonPrivateConstructor = true;

        result.append("{").append(newLine).append(tab).append(tab).append("super(");

        Parameter[] p = m.getParameters();
        for (int i = 0; i < m.getParameterCount(); i++) {
            result.append(p[i].getName());
            if (i + 1 != m.getParameterCount()) {
                result.append(", ");
            }
        }
        result.append(");").append(newLine).append(tab).append("}").append(newLine);
    }

    private void setMethodBody(Class<?> c) {
        result.append(" {").append(newLine);
        result.append(tab).append(tab);
        if (c.isPrimitive()) {
            if (!c.equals(void.class)) {
                result.append("return ");
                if (c.equals(boolean.class)) {
                    result.append("false;");
                } else {
                    result.append("0;");
                }
            }
        } else {
            result.append("return null;");
        }
        result.append(newLine).append(tab).append("}").append(newLine).append(newLine);
    }
}
