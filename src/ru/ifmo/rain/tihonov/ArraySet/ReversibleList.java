package ru.ifmo.rain.tihonov.ArraySet;

import java.util.AbstractList;
import java.util.List;

public class ReversibleList<T> extends AbstractList<T> {
    private List<T> list;
    private boolean reverse = true;

    ReversibleList(List<T> list) {
        this.list = list;
    }

    @Override
    public T get(int index) {
        return list.get(reverse ? size() - index - 1 : index);
    }

    @Override
    public int size() {
        return list.size();
    }

    public void reverse() {
        reverse = !reverse;
    }
}
