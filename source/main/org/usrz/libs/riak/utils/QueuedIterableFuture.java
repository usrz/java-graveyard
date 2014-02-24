/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.riak.utils;

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QueuedIterableFuture<T> extends AbstractFuture<Iterator<T>>
implements IterableFuture<T>, Closeable {

    private final LinkedBlockingQueue<Reference<T>> queue = new LinkedBlockingQueue<>();
    private volatile Reference<T> last;

    /* ====================================================================== */

    public QueuedIterableFuture() {
        super();
    }

    /* ====================================================================== */

    @Override
    public boolean hasNext(long timeout, TimeUnit unit)
    throws InterruptedException, TimeoutException {
        if (last != null) return true;

        final Reference<T> reference = queue.poll(timeout, unit);
        if (reference == null) throw new TimeoutException();
        return end != (last = reference);
    }

    @Override
    public T next(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
        if (!hasNext(timeout, unit)) throw new NoSuchElementException();

        /*
         * "last.get()" will throw an exception (if it needs to) so that
         * the assignment to "null" in the next line will never succeeed
         */
        final T next = last.get();
        last = null;
        return next;
    }

    /* ====================================================================== */

    public void put(T instance) {
        if (outcome.get() == null) {
            queue.add(new NormalReference(instance));
            return;
        } else switch(outcome.get()) {
            case CANCELLED: throw new CancellationException("Cancelled");
            case COMPLETED: throw new IllegalStateException("Completed");
        }
    }

    public void fail(Throwable throwable) {
        if (outcome.compareAndSet(null, Outcome.COMPLETED)) {
            queue.add(new ErrorReference(throwable));
        } else switch(outcome.get()) {
            case CANCELLED: throw new CancellationException("Cancelled");
            case COMPLETED: throw new IllegalStateException("Completed");
        }
    }

    @Override
    public void close() {
        if (outcome.compareAndSet(null, Outcome.COMPLETED)) {
            queue.add(end);
        } else switch(outcome.get()) {
            case CANCELLED: throw new CancellationException("Canceled");
            case COMPLETED: throw new IllegalStateException("Completed");
        }
    }

    /* ====================================================================== */

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public Iterator<T> get() {
        return this;
    }

    @Override
    public Iterator<T> get(long timeout, TimeUnit unit) {
        return this;
    }

    @Override
    public boolean hasNext()
    throws UncheckedInterruptedException {
        try {
            return this.hasNext(MAX_VALUE, MILLISECONDS);
        } catch (InterruptedException exception) {
            throw new UncheckedInterruptedException(exception);
        } catch (TimeoutException exception) {
            /* This should never happen, we wait forever... */
            throw new UncheckedTimeoutException(exception);
        }
    }

    @Override
    public T next()
    throws UncheckedExecutionException,
           UncheckedInterruptedException {
        try {
            return this.next(MAX_VALUE, MILLISECONDS);
        } catch (ExecutionException exception) {
            throw new UncheckedExecutionException(exception);
        } catch (InterruptedException exception) {
            throw new UncheckedInterruptedException(exception);
        } catch (TimeoutException exception) {
            /* This should never happen, we wait forever... */
            throw new UncheckedTimeoutException(exception);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* ====================================================================== */

    private static interface Reference<T> {
        T get() throws ExecutionException;
    }

    private class NormalReference implements Reference<T> {
        private final T reference;

        private NormalReference(T reference) {
            this.reference = reference;
        }

        @Override
        public T get() {
            return reference;
        }
    }

    private class ErrorReference implements Reference<T> {
        private final ExecutionException exception;

        private ErrorReference(Throwable throwable) {
            this.exception = new ExecutionException(throwable);
        }

        @Override
        public T get() throws ExecutionException {
            throw exception;
        }
    }

    private final Reference<T> end = new Reference<T>() {
        @Override public T get() { throw new NoSuchElementException(); }
    };
}
