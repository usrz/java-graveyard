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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SettableFuture<T> extends AbstractFuture<T> {

    private final Semaphore semaphore = new Semaphore(-1);
    private volatile ExecutionException failure;
    private volatile T result;

    @Override
    public T get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
        if (this.semaphore.tryAcquire(timeout, unit)) {
            if (failure != null) throw failure;
            return result;
        } else {
            throw new TimeoutException();
        }
    }

    public void set(T instance) {
        if (outcome.compareAndSet(null, Outcome.COMPLETED)) {
            result = instance;
            semaphore.release(MAX_VALUE);
        } else switch(outcome.get()) {
            case CANCELLED: throw new CancellationException("Cancelled");
            case COMPLETED: throw new IllegalStateException("Completed");
        }
    }

    public void fail(Throwable throwable) {
        if (outcome.compareAndSet(null, Outcome.COMPLETED)) {
            failure = throwable instanceof ExecutionException ?
                          (ExecutionException) throwable :
                          new ExecutionException(throwable);
            semaphore.release(MAX_VALUE);
        } else switch(outcome.get()) {
            case CANCELLED: throw new CancellationException("Cancelled");
            case COMPLETED: throw new IllegalStateException("Completed");
        }
    }

}
