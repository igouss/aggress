//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IterableListScrollableResults<E> extends AbstractList<E> {
    private static final int ENTITY_COUNTER_THRESHOLD = 20;
    private Session hibernateSession;
    private ScrollableResults results;
    private int numberOfLoadedEntities;
    private boolean empty = true;
    private boolean ended = false;
    private E currentEntity;

    private E monitoredGet() {
        if(++this.numberOfLoadedEntities > 20) {
            this.hibernateSession.clear();
            this.numberOfLoadedEntities = 0;
        }

        Object[] row = this.results.get();
        if(row.length == 1) {
            this.currentEntity = (E) row[0];
        }

        return this.currentEntity;
    }

    private boolean nextAndGet() {
        boolean hasNext = false;
        if(this.results.next()) {
            hasNext = true;
            this.monitoredGet();
        } else {
            this.ended = true;
            this.hibernateSession.close();
        }

        return hasNext;
    }

    private E getCurrent() {
        return this.currentEntity;
    }

    public IterableListScrollableResults(Session session, ScrollableResults scrollableresults) {
        this.hibernateSession = session;
        this.results = scrollableresults;
        this.empty = !this.nextAndGet();
    }

    public E get(int index) {
        if(index == this.results.getRowNumber()) {
            return this.getCurrent();
        } else if(this.results.setRowNumber(index)) {
            return this.monitoredGet();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        return new IterableListScrollableResults.ResultIterator();
    }

    public ListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    public ListIterator<E> listIterator(int index) {
        if(index != this.results.getRowNumber() && !this.results.setRowNumber(index)) {
            throw new IndexOutOfBoundsException();
        } else {
            return new IterableListScrollableResults.ResultListIterator();
        }
    }

    private class ResultListIterator extends ResultIterator implements ListIterator<E> {
        private ResultListIterator() {
            super();
        }

        public boolean hasPrevious() {
            return !IterableListScrollableResults.this.results.isFirst();
        }

        public E previous() {
            if(IterableListScrollableResults.this.results.previous()) {
                return IterableListScrollableResults.this.monitoredGet();
            } else {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return IterableListScrollableResults.this.results.getRowNumber();
        }

        public int previousIndex() {
            return IterableListScrollableResults.this.results.getRowNumber() - 1;
        }

        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        public void add(E e) {
            throw new UnsupportedOperationException();
        }
    }

    private class ResultIterator implements Iterator<E> {
        private ResultIterator() {
        }

        public boolean hasNext() {
            return !IterableListScrollableResults.this.ended;
        }

        public E next() {
            if(!IterableListScrollableResults.this.ended) {
                E result = IterableListScrollableResults.this.getCurrent();
                IterableListScrollableResults.this.nextAndGet();
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
