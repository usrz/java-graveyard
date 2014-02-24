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

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractIterableFuture<T> implements IterableFuture<T> {

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

}
