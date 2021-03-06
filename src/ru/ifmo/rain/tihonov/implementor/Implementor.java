package ru.ifmo.rain.tihonov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation for {@link JarImpler}
 */
public class Implementor implements JarImpler {
    /**
     * Error flag.
     * {@code true} if error occurred, {@code false} otherwise
     */
    private static boolean errorOccurred = false;

    /**
     * {@link String} equal line separator on current system
     */
    private final static String newLine = System.lineSeparator();

    /**
     * {@link String} contains 4 whitespace
     */
    private final static String indent = "    ";

    /**
     * Default constructor
     */
    public Implementor() {
    }

    /**
     * Run {@code Implement} or {@code JarImplement} depends on arguments.
     * <p>
     * If arguments is {@code [realizable class] [path to save file] }
     * create java-file with implementation of realizable class on the path.
     * If arguments is {@code [-jar] [realizable class] [path to save file] }
     * create jar-file with implementation of realizable class on the path.
     *
     * @param args command-line arguments. Should contains
     *             {@code [realizable class] [path to save file] }
     *             or {@code [-jar] [realizable class] [path to save jar-file] }
     */
    public static void main(String[] args) {
        if (args == null) {
            error("Expected not null argument");
            return;
        }

        if (args.length != 2 && args.length != 3) {
            error("Expected 2 or 3 arguments");
        }

        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            error("Arguments can not be null");
        }

        if (args.length == 3 && !args[0].equals("-jar")) {
            error("Unknown command: " + args[0]);
        }

        if (errorOccurred) {
            return;
        }

        Implementor implementor = new Implementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ClassNotFoundException e) {
            error("Not found class: " + e.getMessage());
        } catch (ImplerException e) {
            error("Error while generating file or jar: " + e.getMessage());
        }
    }

    /**
     * Print message if error occurred and set flag {@code errorOccurred} as {@code true}
     *
     * @param message information about error
     */
    private static void error(String message) {
        errorOccurred = true;
        System.err.println(message);
    }

    /**
     * Return new path, append to {@code path} package and file name
     *
     * @param token  implementing class or interface
     * @param path   path for saving file
     * @param suffix file extension
     * @return {@code path} completed package and class name
     * @throws ImplerException if {@code path} is invalid
     */
    private Path getPath(Class<?> token, Path path, String suffix) throws ImplerException {
        try {
            path = path.resolve(token.getPackageName().
                    replace('.', File.separatorChar)).
                    resolve(token.getSimpleName() + "Impl" + suffix);
        } catch (InvalidPathException e) {
            throw new ImplerException("Path is invalid", e);
        }
        return path;
    }

    /**
     * Create missing directories on the {@code path}
     *
     * @param path path to creating directories
     * @return parameter {@code path}
     * @throws ImplerException can not create directories
     */
    private Path createDirs(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can not create directories: " + e.getMessage(), e);
            }
        }

        return path;
    }

    /**
     * Generate java-code with implementation of {@code token} and
     * saving result in jar-file by {@code path}.
     * <p>
     * Call {@code implement} to generate class and save result
     * in {@code .java} file. Then compile it and put into jar-file
     * on the {@code path}.
     *
     * @param token realizable class or interface
     * @param path  path to saving jar-file
     * @throws ImplerException can not create temporary directory, or
     *                         compile error, or error while creating {@code .jar}
     */
    @Override
    public void implementJar(Class<?> token, Path path) throws ImplerException {
        Path temp;
        try {
            temp = Files.createTempDirectory(path.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Error while creating temporary directory: " + e.getMessage(), e);
        }

        implement(token, temp);

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        if (javaCompiler == null) {
            throw new ImplerException("Can not create compiler");
        }

        String[] args = new String[]{
                getPath(token, temp, ".java").toString(),
                "-cp",
                temp.toString() + File.pathSeparator + System.getProperty("java.class.path"),
                "-encoding",
                "UTF8"
        };

        if (javaCompiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Compile error");
        }

        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(path), getManifest("Tihonov Vitaly"))) {
            writer.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
            Files.copy(getPath(token, temp, ".class"), writer);
        } catch (InvalidPathException e) {
            throw new ImplerException("You are invalid: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ImplerException("IO or ZIP error : " + e.getMessage(), e);
        }
    }

    /**
     * Create {@link Manifest} and set vendor as attribute
     *
     * @param vendor implementor vendor name
     * @return {@link Manifest} with vendor as attribute
     */
    private Manifest getManifest(String vendor) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, vendor);
        return manifest;
    }

    /**
     * Generate java-code with implementation of {@code token} and
     * saving result on the {@code path}.
     * <p>
     * Call {@code setPackage}, {@code setTitle}, {@code setMethod} to generate
     * class and save in {@code .java} file
     *
     * @param token realizable class or interface
     * @param path  path to saving result
     * @throws ImplerException either arguments are null or {@code token} is Enum,
     *                         primitive, array, final or utility class
     */
    @Override
    public void implement(Class<?> token, Path path) throws ImplerException {
        StringBuilder result = new StringBuilder();
        Set<Executable> methodsSet = new HashSet<>();
        Set<String> methodsTitle = new HashSet<>();

        if (token == null || path == null) {
            throw new ImplerException("Arguments can't be null");
        }

        if (token.isArray() || token.isPrimitive() || token.equals(Enum.class)
                || Modifier.isFinal(token.getModifiers()) || isUtility(token)) {
            throw new ImplerException("We can't extends from Enum, primitive, array, final or utility class");
        }

        setPackage(token.getPackageName(), result);
        setTitle(token, result);

        findMethods(token, methodsSet, methodsTitle);
        methodsSet.addAll(Arrays.asList(token.getDeclaredConstructors()));

        setMethods(token, methodsSet, result);
        result.append("}");

        path = createDirs(getPath(token, path, ".java"));
        write(path, result);
    }

    /**
     * Append line with package name to {@code result}.
     *
     * @param name   package name
     * @param result package name add to this
     */
    private void setPackage(String name, StringBuilder result) {
        if (!name.isEmpty()) {
            result.append("package ").append(name).append(";").append(newLine).append(newLine);
        }
    }

    /**
     * Append line class title.
     *
     * @param token  realizable interface or class
     * @param result class title add to this
     */
    private void setTitle(Class<?> token, StringBuilder result) {
        result.append("public class ").append(token.getSimpleName()).append("Impl").
                append((token.isInterface() ? " implements " : " extends ")).
                append(token.getCanonicalName()).append(" {").append(newLine).append(newLine);
    }

    /**
     * Save implementation on the {@code path}.
     *
     * @param path   path to saving result
     * @param result print this in Unicode
     * @throws ImplerException error while creating file or saving result
     */
    private void write(Path path, StringBuilder result) throws ImplerException {
        result = convertToUnicode(result);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(result.toString());
        } catch (IOException e) {
            throw new ImplerException("IO error while saving result: " + e.getMessage(), e);
        }
    }

    /**
     * Translate international symbols from {@code result} to Unicode
     *
     * @param result contains result file
     * @return {@code result} converted to Unicode
     */
    private StringBuilder convertToUnicode(StringBuilder result) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < result.length(); i++) {
            char ch = result.charAt(i);
            if (ch >= 128) {
                unicode.append("\\u").append(String.format("%04X", (int) ch));
            } else {
                unicode.append(ch);
            }
        }
        return unicode;
    }

    /**
     * Recursive search methods in superclass and interfaces
     *
     * @param token        realizable interface or class
     * @param methodsSet   {@link Set} which contains methods and constructors
     * @param methodsTitle {@link Set} which contains title of methods and constructors
     */
    private void findMethods(Class<?> token, Set<Executable> methodsSet, Set<String> methodsTitle) {
        for (Method method : token.getDeclaredMethods()) {
            String title = method.getName() + Arrays.toString(method.getParameterTypes());
            if (!methodsTitle.contains(title)) {
                methodsTitle.add(title);
                methodsSet.add(method);
            }
        }

        if ((token.getModifiers() & Modifier.ABSTRACT) != 0) {
            for (Class<?> c : token.getInterfaces()) {
                findMethods(c, methodsSet, methodsTitle);
            }
        }

        if (token.getSuperclass() != null) {
            findMethods(token.getSuperclass(), methodsSet, methodsTitle);
        }
    }

    /**
     * Append title and body of methods and constructors.
     *
     * @param token      realizable interface or class
     * @param methodsSet {@link Set} which contains methods and constructors
     * @param result     contains result file
     */
    private void setMethods(Class<?> token, Set<Executable> methodsSet, StringBuilder result) {
        for (Executable m : methodsSet) {
            if ((m.getModifiers() & (Modifier.PRIVATE | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.VOLATILE)) != 0) {
                continue;
            }

            setTitle(token, m, result);
            if (m instanceof Constructor) {
                setConstructorBody(m, result);
            } else if (m instanceof Method) {
                setMethodBody(((Method) m).getReturnType(), result);
            }
        }
    }

    /**
     * Append title of constructor or method.
     * <p>
     * Call methods to append modifiers, return type,
     * name of constructor or method, parameters list,
     * exceptions.
     *
     * @param token  realizable interface or class
     * @param m      current method or constructor
     * @param result contains result file
     */
    private void setTitle(Class<?> token, Executable m, StringBuilder result) {
        result.append(indent);
        if (m instanceof Constructor) {
            result.append(token.getSimpleName()).append("Impl");
        } else if (m instanceof Method) {
            setModifiers((Method) m, result);
            Class<?> returnType = ((Method) m).getReturnType();
            result.append(returnType.getCanonicalName()).append(" ").append(m.getName());
        }

        result.append("(");
        setArgs(m.getParameters(), result);
        result.append(")");

        if (m.getExceptionTypes().length != 0) {
            result.append(" throws ");
            setExceptions(m.getExceptionTypes(), result);
        }
    }

    /**
     * Append modifiers, besides <code>abstract</code>
     *
     * @param m      current method
     * @param result contains result file
     */
    private void setModifiers(Method m, StringBuilder result) {
        int mod = m.getModifiers();
        result.append(Modifier.toString(mod & (mod ^ (Modifier.NATIVE | Modifier.ABSTRACT | Modifier.TRANSIENT)))).append(" ");
    }

    /**
     * Append list of parameters
     *
     * @param params array of {@link Parameter} to append
     * @param result contains result file
     */
    private void setArgs(Parameter[] params, StringBuilder result) {
        for (int i = 0; i < params.length; i++) {
            Parameter c = params[i];
            result.append(c.getType().getCanonicalName()).append(" ").append(c.getName());
            if (i + 1 != params.length) {
                result.append(", ");
            }
        }
    }

    /**
     * Append list of exceptions
     *
     * @param exceptions array of {@link Class} to append
     * @param result     contains result file
     */
    private void setExceptions(Class<?>[] exceptions, StringBuilder result) {
        for (int i = 0; i < exceptions.length; i++) {
            result.append(exceptions[i].getCanonicalName());
            if (i + 1 != exceptions.length) {
                result.append(", ");
            }
        }
    }

    /**
     * Append body for parameters constructor
     *
     * @param m      current constructor
     * @param result contains result file
     */
    private void setConstructorBody(Executable m, StringBuilder result) {
        if (Modifier.isPrivate(m.getModifiers())) {
            return;
        }

        result.append("{").append(newLine).append(indent).append(indent).append("super(");

        Parameter[] p = m.getParameters();
        for (int i = 0; i < m.getParameterCount(); i++) {
            result.append(p[i].getName());
            if (i + 1 != m.getParameterCount()) {
                result.append(", ");
            }
        }
        result.append(");").append(newLine).append(indent).append("}").append(newLine);
    }

    /**
     * Append method body.
     * <p>
     * Body contains return statement, if return type isn't
     * <code>void</code>.Return value depends on return type.
     * If return type is <code>boolean</code>, then return value
     * is <code>false</code>. For other primitive - <code>0</code>.
     * For references types - <code>null</code>.
     *
     * @param c      return type of realizable method
     * @param result contains result file
     */
    private void setMethodBody(Class<?> c, StringBuilder result) {
        result.append(" {").append(newLine);
        result.append(indent).append(indent);
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
        result.append(newLine).append(indent).append("}").append(newLine).append(newLine);
    }

    /**
     * Check {@code token} on containing none private constructors
     *
     * @param token realizable class or interface
     * @return true if all constructors of {@code token} are private,
     * otherwise - false
     */
    private boolean isUtility(Class<?> token) {
        return !token.isInterface() && Arrays.stream(token.getDeclaredConstructors()).allMatch(c -> Modifier.isPrivate(c.getModifiers()));
    }
}
