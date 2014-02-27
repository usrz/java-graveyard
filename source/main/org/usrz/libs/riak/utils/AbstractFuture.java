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

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractFuture<T> implements Future<T>, Fallible {

    protected enum Outcome { CANCELLED, COMPLETED };
    protected final AtomicReference<Outcome> outcome = new AtomicReference<>(null);
    private final List<Future<?>> futures = new ArrayList<>();

    /* ====================================================================== */

    public AbstractFuture() {
        super();
    }

    public final void addFuture(Future<?> future) {
        this.futures.add(future);
    }

    /* ====================================================================== */

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        if (outcome.compareAndSet(null, Outcome.CANCELLED)) {
            try {
                /* Fail this with a cancellation exception */
                this.failed(new CancellationException("Cancelled"));
            } finally {
                /* Cancel others *ONLY* if we're being cancelled (avoid loops) */
                for (Future<?> future: futures) future.cancel(mayInterruptIfRunning);
            }
        }
        return isCancelled();
    }

    @Override
    public final boolean isCancelled() {
        return outcome.get() == Outcome.CANCELLED;
    }

    @Override
    public final boolean isDone() {
        return outcome.get() != null;
    }

    @Override
    public T get()
    throws InterruptedException, ExecutionException {
        try {
            return this.get(MAX_VALUE, MILLISECONDS);
        } catch (TimeoutException exception) {
            /* This should never happen, we wait forever... */
            throw new UncheckedTimeoutException(exception);
        }
    }

    @Override
    public final void fail(Throwable throwable) {
        if (outcome.compareAndSet(null, Outcome.COMPLETED)) {
            try {
                /* Fail this unwrapping any ExecutionException */
                this.failed(throwable instanceof ExecutionException ? throwable.getCause() : throwable);
            } finally {
                /* Cancel others *ONLY* if we're being cancelled (avoid loops) */
                for (Future<?> future: futures) future.cancel(true);
            }

        } else switch(outcome.get()) {
            case CANCELLED: throw new CancellationException("Cancelled");
            case COMPLETED: throw new IllegalStateException("Completed");
        }
    }

    protected abstract void failed(Throwable throwable);

}
