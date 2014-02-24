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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class WrappingIterableFuture<T, F> extends AbstractIterableFuture<T> {

    protected final IterableFuture<F> future;

    protected WrappingIterableFuture(IterableFuture<F> future) {
        if (future == null) throw new NullPointerException("Null future");
        this.future = future;
    }

    @Override
    public boolean hasNext(long timeout, TimeUnit unit)
    throws InterruptedException, TimeoutException {
        return future.hasNext(timeout, unit);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

}
