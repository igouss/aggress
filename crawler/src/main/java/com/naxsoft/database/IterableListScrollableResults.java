//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import java.util.Iterator;

//class ScrollableResultIterator<T> implements Iterator<T> {
//    private final ScrollableResults results;
//    private final Class<T> type;
//
//    ScrollableResultIterator(ScrollableResults results, Class<T> type) {
//        this.results = results;
//        this.type = type;
//    }
//
//    @Override
//    public boolean hasNext() {
//        return results.next();
//    }
//
//    @Override
//    public T next() {
//        return type.cast(results.get(0));
//    }
//}
public class IterableListScrollableResults<T> implements Iterable<T> {
    Iterator<T> iterator;

    public IterableListScrollableResults(StatelessSession session, ScrollableResults sr) {
        iterator = new ScrollableResultsIterator<>(session, sr);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    class ScrollableResultsIterator<T> implements Iterator<T> {
        private static final int DEFAULT_FLUSH_LIMIT = 1000;

        private ScrollableResults sr;
        private T next = null;
        private StatelessSession session;
        private int count = 0;
        private boolean elementPulled = false;

        public ScrollableResultsIterator(StatelessSession session, ScrollableResults sr) {
            this.sr = sr;
            this.session = session;
        }

        /**
         * ScrollableResults does not provide a hasNext method, implemented here for Iterator interface.
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean hasNext() {
            // if we have a next element that was not pulled, just simply return true.
            if (!elementPulled && next != null) {
                return true;
            }

            if (sr.next()) {
                //we remember the element
                next = (T) sr.get()[0];
                elementPulled = false;
                return true;
            }
            sr.close();
            session.close();
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            T toReturn = null;
            //next variable could be null because the last element was sent or because we iterate on the next()
            //instead of hasNext()
            if (next == null) {
                //if we can retrieve an element, do it
                if (sr.next()) {
                    toReturn = (T) sr.get()[0];
                }
            } else { //the element was fetched by hasNext, return it
                toReturn = next;
                next = null;
            }

            //if we are at the end, close the result set
            if (toReturn == null) {
                sr.close();
            } else { //clear memory to avoid memory leak
                if (count == DEFAULT_FLUSH_LIMIT) {
                    count = 0;
                }
                count++;
            }
            elementPulled = true;
            return toReturn;
        }


        /**
         * Unsupported Operation for this implementation.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

