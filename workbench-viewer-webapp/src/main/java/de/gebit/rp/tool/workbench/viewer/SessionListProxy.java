package de.gebit.rp.tool.workbench.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import de.gebit.rp.tool.workbench.viewercommon.ConsoleSession;

public class SessionListProxy implements List<ConsoleSession> {

    private ConsoleSession allSession;
    private List<ConsoleSession> backendList;

    public SessionListProxy(ConsoleSession allSession, List<ConsoleSession> backendList) {
        this.allSession = Objects.requireNonNull(allSession);
        this.backendList = Objects.requireNonNull(backendList);
    }


    @Override
    public int size() {
        return backendList.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return o == allSession || backendList.contains(o);
    }

    @Override
    public Iterator<ConsoleSession> iterator() {
        ArrayList tempList = new ArrayList<>(backendList);
        tempList.add(0, allSession);
        return tempList.iterator();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(ConsoleSession consoleSession) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends ConsoleSession> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index,  Collection<? extends ConsoleSession> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsoleSession get(int index) {
        if (index == 0) {
            return allSession;
        }
        return backendList.get(index - 1);
    }

    @Override
    public ConsoleSession set(int index, ConsoleSession element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, ConsoleSession element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsoleSession remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        if (allSession == o) {
            return 0;
        } else {
            int result = backendList.indexOf(o);
            if (result == -1) {
                return -1;
            } else {
                return result + 1;
            }
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ConsoleSession> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ConsoleSession> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConsoleSession> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
