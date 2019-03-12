package ru.ifmo.rain.tihonov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> list;
    private Comparator<? super T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends T> c) {
        this(c, null);
    }

    public ArraySet(Comparator<? super T> cmp) {
        this(Collections.emptyList(), cmp);
    }

    private ArraySet(List<T> list, Comparator<? super T> cmp) {
        this.list = list;
        comparator = cmp;
    }

    public ArraySet(Collection<? extends T> c, Comparator<? super T> cmp) {
        Set<T> set = new TreeSet<>(cmp);
        set.addAll(c);
        list = new ArrayList<>(set);
        comparator = cmp;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (T) Objects.requireNonNull(o), comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public T first() {
        return get(0);
    }

    @Override
    public T last() {
        return get(list.size() - 1);
    }

    private T get(int index) {
        if (list.size() == 0) {
            throw new NoSuchElementException("Set is empty");
        }
        return list.get(index);
    }

    @Override
    public T lower(T t) {
        return getValue(t, false, false);
    }

    @Override
    public T floor(T t) {
        return getValue(t, true, false);
    }

    @Override
    public T ceiling(T t) {
        return getValue(t, true, true);
    }

    @Override
    public T higher(T t) {
        return getValue(t, false, true);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int from = getIndex(fromElement, fromInclusive, true);
        int to = getIndex(toElement, toInclusive, false);

        if (from == -1 || to == -1 || from > to) {
            return new ArraySet<>(comparator);
        }

        return new ArraySet<>(list.subList(from, to + 1), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (comparator != null) {
            if (comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException();
            }
        } else {
            if (fromElement instanceof Comparable && ((Comparable) fromElement).compareTo(toElement) > 0) {
                throw new IllegalArgumentException();
            }
        }

        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (list.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(list.get(0), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (list.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(fromElement, inclusive, list.get(list.size() - 1), true);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        if (list.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(list.get(0), true, toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        if (list.isEmpty()) {
            return new ArraySet<>(comparator);
        }
        return subSet(fromElement, true, list.get(list.size() - 1), true);
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversibleList<>(list), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private T getValue(T t, boolean inclusive, boolean left) {
        int index = getIndex(t, inclusive, left);
        return index != -1 ? list.get(index) : null;
    }

    private int getIndex(T t, boolean inclusive, boolean left) {
        int index = Collections.binarySearch(list, t, comparator);
        if (index < 0) {
            index = -index - 1;

            if (inclusive) {
                if (!left) {
                    index--;
                }
            } else {
                index += left ? 0 : -1;
            }
        } else {
            if (!inclusive) {
                index += left ? 1 : -1;
            }
        }

        return index >= 0 && index < list.size() ? index : -1;
    }
}
