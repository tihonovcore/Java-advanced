package ru.ifmo.rain.tihonov.ArraySet;

import java.util.*;

public class ArraySetEasy<T> extends AbstractSet<T> implements SortedSet<T> {
    private List<T> list;
    private Comparator<? super T> comparator;
    private boolean inclusive = false;

    public ArraySetEasy() {
        list = Collections.emptyList();
    }

    public ArraySetEasy(Collection<? extends T> c) {
        list = new ArrayList<>(new TreeSet<>(c));
    }

    public ArraySetEasy(Collection<? extends T> c, Comparator<? super T> cmp) {
        Set<T> set = new TreeSet<>(cmp);
        set.addAll(c);
        list = new ArrayList<>(set);
        comparator = cmp;
    }

    public ArraySetEasy(Comparator<? super T> cmp) {
        list = Collections.emptyList();
        comparator = cmp;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (list.size() == 0) {
            return new ArraySetEasy<>(comparator);
        }

        boolean flag = inclusive;
        int start = getIndex(fromElement, true);
        inclusive = flag;
        int finish = getIndex(toElement, false);

        if (start == -1 || finish == -1 || start > finish) {
            return new ArraySetEasy<>(comparator);
        }

        return new ArraySetEasy<>(list.subList(start, finish + 1), comparator);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        if (list.size() == 0) {
            return new ArraySetEasy<>(comparator);
        }
        return subSet(list.get(0), toElement);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        inclusive = true;
        if (list.size() == 0) {
            return new ArraySetEasy<>(comparator);
        }
        return subSet(fromElement, list.get(list.size() - 1));
    }

    @Override
    public T first() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return list.get(0);
    }

    @Override
    public T last() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return list.get(list.size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return (Collections.binarySearch(list, (T) Objects.requireNonNull(o), comparator) >= 0);
    }

    private int getIndex(T t, boolean left) {
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
                index += left ? 0 : -1;
            }
        }

        inclusive = false;
        return index >= 0 && index < list.size() ? index : -1;
    }
}
