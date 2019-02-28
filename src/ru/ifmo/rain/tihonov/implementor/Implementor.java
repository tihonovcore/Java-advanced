package ru.ifmo.rain.tihonov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Implementor implements Impler {
    private StringBuilder result = new StringBuilder();
    private HashSet<Class<?>> imports = new HashSet<>();
    private Set<Executable> methodSet;
    private String newLine = System.lineSeparator();


    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        result = new StringBuilder();
        imports = new HashSet<>();

        //todo add class
        //todo add excep
        if (token == null || token.equals(void.class) || token.equals(Enum.class)) {
            throw new ImplerException();
        }

        path = path.resolve(token.getPackageName().
                replace('.', File.separatorChar)).
                resolve(token.getSimpleName() + "Impl.java");

        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can not create directories: " + e.getMessage());
            }
        }

        methodSet = new TreeSet<>((o1, o2) -> {
            StringBuilder name1 = new StringBuilder(Arrays.toString(o1.getParameterTypes()));
            name1.append(o1.getName());
            StringBuilder name2 = new StringBuilder(Arrays.toString(o2.getParameterTypes()));
            name2.append(o2.getName());
            return name1.compareTo(name2);
        });

        imports.add(token);

        findMethods(token);
        setMethods(token);
        result.append("}");

        StringBuilder temp = new StringBuilder();
        setPackageAndImports(token.getPackageName(), temp);
        setTitle(token, temp);

        print(temp, path);
    }

    private void setPackageAndImports(String name, StringBuilder temp) {
        if (!name.equals("")) {
            temp.append("package ").append(name).append(";").append(newLine).append(newLine);
        }

        for (Class<?> c : imports) {
            if (!c.isPrimitive() && !c.isArray()) {
                temp.append("import ").append(c.getPackageName()).append(".").
                        append(c.getSimpleName()).append(";").append(newLine);
            }
        }

        if (!imports.isEmpty()) {
            temp.append(newLine);
        }
    }

    private void setTitle(Class<?> token, StringBuilder temp) {
        String name = token.getSimpleName();
        temp.append("public class ").append(name).append("Impl").
                append((token.isInterface() ? " implements " : " extends ")).
                append(name).append(" {").append(newLine).append(newLine);
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
        methodSet.addAll(Arrays.asList(token.getConstructors()));
        methodSet.addAll(Arrays.asList(token.getDeclaredMethods()));

        if ((token.getModifiers() & Modifier.ABSTRACT) != 0) {
            for (Class<?> c : token.getInterfaces()) {
                findMethods(c);
            }
        }
    }

    private void setMethods(Class<?> token) {
        for (Executable m : methodSet) {
            if ((m.getModifiers() & (Modifier.PRIVATE | Modifier.FINAL | Modifier.NATIVE)) != 0) {
                continue;
            }
            result.append("    @SuppressWarnings(\"unchecked\")").append(newLine);
            result.append("    ");
            setTitle(token, m);

            if (m instanceof Constructor) {
                setConstructorBody(m);
            } else if (m instanceof Method) {
                setMethodBody(((Method) m).getReturnType());
            }
        }
    }

    private void setTitle(Class<?> token, Executable m) {
        if (m instanceof Constructor) {
            result.append(token.getSimpleName()).append("Impl");
        } else if (m instanceof Method) {
            setModifiers((Method) m);
            result.append(((Method) m).getReturnType().getSimpleName()).append(" ").append(m.getName());
        }
        result.append("(");
        setArgs(m.getParameterTypes());
        result.append(")");

        if (m.getExceptionTypes().length != 0) {
            result.append(" throws ");
            setExceptions(m.getExceptionTypes());
        }

    }

    private void setModifiers(Method m) {
        int mod = m.getModifiers();
        result.append(Modifier.toString(mod & (mod ^ Modifier.ABSTRACT) & (mod ^ Modifier.TRANSIENT))).append(" ");
        imports.add(m.getReturnType());
    }

    private void setArgs(Class<?>[] params) {
        for (int i = 0; i < params.length; i++) {
            Class<?> c = params[i];
            imports.add(c);
            result.append(c.getSimpleName()).append(" ");

            //todo fix same names
            result.append((char) (i + 'a'));

            if (i + 1 != params.length) {
                result.append(", ");
            }
        }
    }

    private void setExceptions(Class<?>[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            imports.add(exceptions[i]);
            result.append(exceptions[i].getSimpleName());
            if (i + 1 != exceptions.length) {
                result.append(", ");
            }
        }
    }

    private void setConstructorBody(Executable m) {
        result.append("{").append(newLine).append("        super(");
        for (int i = 0; i < m.getParameterCount(); i++) {
            result.append((char) (i + 'a'));
            if (i + 1 != m.getParameterCount()) {
                result.append(", ");
            }
        }
        result.append(");}").append(newLine);
    }

    private void setMethodBody(Class<?> c) {
        result.append(" {").append(newLine);
        result.append("    ").append("    ");
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
        result.append(newLine).append("    }").append(newLine).append(newLine);

    }
}
