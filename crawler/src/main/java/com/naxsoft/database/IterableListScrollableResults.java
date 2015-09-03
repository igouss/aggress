package com.naxsoft.database;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A List<E> partial wrapper view on an Hibernate ScrollableResults.
 *
 * size() is not implemented. Preferred usage is iterator() and isEmpty().
 *
 * Some methods may fail if an inconsistent ScrollMode has been used.
 *
 * Should not be used by multiple threads, only a single iterator should
 * be used at a time, most methods just throw UnsupportedOperationException
 *
 * Clear Hibernate session regularly to keep memory under control.
 *
 * For now, only use to pass a large set of entity to ExportService.
 *
 * @author Yves Martin
 * @param <E> an Hibernate Entity
 */
public class IterableListScrollableResults<E> extends AbstractList<E> {

    /**
     * Number of entities threshold.
     * Care about dirty object detection algorithm in O(n2) ! Keep it low.
     */
    private static final int ENTITY_COUNTER_THRESHOLD = 20;

    /** Reference to Hibernate session */
    private Session hibernateSession;

    /** Hibernate ScrollableResults to browse */
    private ScrollableResults results;

    /** Number of entities loaded since last session flush. */
    private int numberOfLoadedEntities;

    /** Flag to keep information about an empty ScrollableResults. */
    private boolean empty = true;

    /** Flag to keep track when forward only result set is over. */
    private boolean ended = false;

    /** Keep current entity for forward only ScrollableResults. */
    private E currentEntity;

    /**
     * Monitor number of calls on get() to clear session regularly.
     * @return current entity row
     */
    private E monitoredGet() {
        if (++numberOfLoadedEntities > ENTITY_COUNTER_THRESHOLD) {
            hibernateSession.clear();
            numberOfLoadedEntities = 0;
        }
        Object[] row = results.get();
        if (row.length == 1) {
            this.currentEntity = (E) row[0];
        }
        return this.currentEntity;
    }

    /**
     * Monitor number of calls on next() to clear session.
     * @return current entity row
     */
    private boolean nextAndGet() {
        boolean hasNext = false;
        if (results.next()) {
            hasNext = true;
            monitoredGet();
        } else {
            this.ended = true;
            hibernateSession.close();
        }
        return hasNext;
    }

    /**
     * Return current entity.
     * @return an hibernate entity
     */
    private E getCurrent() {
        return this.currentEntity;
    }


    /**
     * Constructor.
     *
     * @param session current hibernate session used to query data
     * @param scrollableresults results returned by query
     */
    public IterableListScrollableResults(final Session session, final ScrollableResults scrollableresults) {
        this.hibernateSession = session;
        this.results = scrollableresults;
        this.empty = !nextAndGet();
    }

    @Override
    public E get(final int index) {
        if (index == results.getRowNumber()) {
            return getCurrent();
        } else if (results.setRowNumber(index)) {
            // true if position has been set properly
            return monitoredGet();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    @Override
    public int size() {
        // Try to avoid it for performance reason. Any possible implementation
        // will force Hibernate to scroll (and load) the full ResultSet !
        throw new UnsupportedOperationException();
        // Possible implementation: results.last(); return results.getRowNumber()+1;
    }

    public Iterator<E> iterator() {
//
//        if (!results.isFirst()) {
//            results.first();
//        }
        return new ResultIterator();
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) {
        if (index != results.getRowNumber()) {
            // Move to row if possible
            if (!results.setRowNumber(index)) {
                throw new IndexOutOfBoundsException();
            }
        }
        return new ResultListIterator();
    }

    /**
     * Read-only Iterator on ScrollableResults.
     */
    private class ResultIterator implements Iterator<E> {

        public boolean hasNext() {
            return !ended;
        }

        public E next() {
            if (!ended) {
                E result = getCurrent();
                nextAndGet();
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Read-only ListIterator on ScrollableResults.
     * Do not work in Forward Only mode.
     */
    private class ResultListIterator extends ResultIterator implements ListIterator<E> {

        public boolean hasPrevious() {
            return !results.isFirst();
        }

        public E previous() {
            if (results.previous()) {
                return monitoredGet();
            } else {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return results.getRowNumber();
        }

        public int previousIndex() {
            return results.getRowNumber() - 1;
        }

        public void set(final E e) {
            throw new UnsupportedOperationException();
        }

        public void add(final E e) {
            throw new UnsupportedOperationException();
        }
    }

}