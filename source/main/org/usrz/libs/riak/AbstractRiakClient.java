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
package org.usrz.libs.riak;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.usrz.libs.riak.utils.IterableFuture;
import org.usrz.libs.riak.utils.WrappingIterableFuture;

public abstract class AbstractRiakClient implements RiakClient {

    @Override
    public final Bucket getBucket(String name) {
        return new Bucket(this, name);
    }

    /* ====================================================================== */

    @Override
    public abstract IterableFuture<Bucket> getBuckets()
    throws IOException;

    @Override
    public final IterableFuture<String> getBucketNames()
    throws IOException {
        return new WrappingIterableFuture<String, Bucket>(getBuckets()) {
            @Override
            public String next(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return future.next(timeout, unit).getName();
            }
        };
    }

    /* ====================================================================== */

    @Override
    public abstract IterableFuture<Key> getKeys(Bucket bucket)
    throws IOException;

    @Override
    public final IterableFuture<Key> getKeys(final String bucket)
    throws IOException {
        return getKeys(getBucket(bucket));
    }

    @Override
    public final IterableFuture<String> getKeyNames(Bucket bucket)
    throws IOException {
        return new WrappingIterableFuture<String, Key>(this.getKeys(bucket)) {
            @Override
            public String next(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return future.next(timeout, unit).getName();
            }
        };
    }

    @Override
    public final IterableFuture<String> getKeyNames(String bucket)
    throws IOException {
        return getKeyNames(getBucket(bucket));
    }

    /* ====================================================================== */

    @Override
    public abstract <T> FetchRequest<T> fetch(Key key, Class<T> type);

    @Override
    public final <T> FetchRequest<T> fetch(Bucket bucket, String key, Class<T> type) {
        return this.fetch(new Key(bucket, key), type);
    }

    @Override
    public final <T> FetchRequest<T> fetch(String bucket, String key, Class<T> type) {
        return this.fetch(getBucket(bucket), key, type);
    }

    protected abstract <T> Future<Response<T>> executeFetch(FetchRequest<T> request, Key key, Class<T> type)
    throws IOException;

    /* ====================================================================== */

    @Override
    public abstract <T> StoreRequest<T> store(Bucket bucket, T object);

    @Override
    public final <T> StoreRequest<T> store(String bucket, T object) {
        return this.store(getBucket(bucket), object);
    }

    protected abstract <T> Future<Response<T>> executeStore(StoreRequest<T> request, Bucket bucket, T instance)
    throws IOException;

    /* ---------------------------------------------------------------------- */

    @Override
    public abstract <T> StoreRequest<T> store(Key key, T object);


    @Override
    public final <T> StoreRequest<T> store(Bucket bucket, String key, T object) {
        return this.store(new Key(bucket, key), object);
    }

    @Override
    public final <T> StoreRequest<T> store(String bucket, String key, T object) {
        return this.store(getBucket(bucket), key, object);
    }

    protected abstract <T> Future<Response<T>> executeStore(StoreRequest<T> request, Key key, T instance)
    throws IOException;

    /* ====================================================================== */

    @Override
    public abstract DeleteRequest delete(Key key);

    @Override
    public final DeleteRequest delete(Bucket bucket, String key) {
        return this.delete(new Key(bucket, key));
    }

    @Override
    public final DeleteRequest delete(String bucket, String key) {
        return this.delete(getBucket(bucket), key);
    }

    protected abstract Future<Response<Void>> executeDelete(DeleteRequest request, Key key)
    throws IOException;

}
