package ru.ifmo.rain.tihonov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {
    private String OTHER = "";

    private Comparator<Student> comparator = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
                        .thenComparing(Student::getId);

    private <collection extends Collection<String>>
    collection collectStudents(List<Student> students, Function<Student, String> mapper, Collector<String, ?, collection> collector) {
        return students.stream().map(mapper).collect(collector);
    }

    private List<String> collectStudents(List<Student> students, Function<Student, String> mapper) {
        return students.stream().map(mapper).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return collectStudents(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return collectStudents(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return collectStudents(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return collectStudents(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return collectStudents(students, Student::getFirstName, Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Comparator.comparingInt(Student::getId))
                .map(Student::getFirstName)
                .orElse(OTHER);
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, comparator);
    }

    private List<Student> studentFilterAndCollect(Collection<Student> students, Predicate<Student> predicate) {
        return sortStudentsByName(students.stream().filter(predicate).collect(Collectors.toList()));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return studentFilterAndCollect(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return studentFilterAndCollect(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo))
                );
    }

    private Stream<Map.Entry<String, List<Student>>> getGroupsStream(Collection<Student> collection) {
        return collection.stream()
                .collect(Collectors.groupingBy(Student::getGroup, HashMap::new, Collectors.toList()))
                .entrySet()
                .stream();
    }

    private List<Group> getSortedGroupList(Collection<Student> collection, Function<Map.Entry<String, List<Student>>, Group> mapper) {
        return getGroupsStream(collection)
                .map(mapper)
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> collection) {
        return getSortedGroupList(collection, t -> new Group(t.getKey(), sortStudentsByName(t.getValue())));
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> collection) {
        return getSortedGroupList(collection, t -> new Group(t.getKey(), sortStudentsById(t.getValue())));
    }

    private String largestGroup(Collection<Student> collection, Comparator<Map.Entry<String, List<Student>>> comparator) {
        return getGroupsStream(collection).max(comparator).map(Map.Entry::getKey).orElse(OTHER);
    }

    private Comparator<Map.Entry<String, List<Student>>>
    groupsComparator(ToIntFunction<Map.Entry<String, List<Student>>> f) {
        return Comparator
                .comparingInt(f)
                .thenComparing(Map.Entry::getKey, Collections.reverseOrder(String::compareTo));
    }

    @Override
    public String getLargestGroup(Collection<Student> collection) {
        return largestGroup(collection,
                groupsComparator((Map.Entry<String, List<Student>> t) -> t.getValue().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> collection) {
        return largestGroup(collection,
                groupsComparator((Map.Entry<String, List<Student>> t) -> getDistinctFirstNames(t.getValue()).size()));
    }
}
