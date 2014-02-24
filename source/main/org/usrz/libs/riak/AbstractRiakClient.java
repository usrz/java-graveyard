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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.usrz.libs.riak.utils.IterableFuture;
import org.usrz.libs.riak.utils.WrappingIterableFuture;


public abstract class AbstractRiakClient implements RiakClient {

    @Override
    public Bucket getBucket(String name) {
        return new Bucket(this, name);
    }

    @Override
    public IterableFuture<Bucket> getBuckets()
    throws IOException {
        return new WrappingIterableFuture<Bucket, String>(getBucketNames()) {
            @Override
            public Bucket next(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return getBucket(future.next(timeout, unit));
            }
        };
    }

    @Override
    public IterableFuture<String> getKeys(Bucket bucket)
    throws IOException {
        return this.getKeys(bucket.getName());
    }

    @Override
    public IterableFuture<Reference> getReferences(final String bucket)
    throws IOException {
        return new WrappingIterableFuture<Reference, String>(this.getKeys(bucket)) {
            @Override
            public Reference next(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return new Reference(AbstractRiakClient.this, bucket, future.next(timeout, unit));
            }
        };
    }

    @Override
    public IterableFuture<Reference> getReferences(Bucket bucket)
    throws IOException {
        return this.getReferences(bucket.getName());
    }

    @Override
    public <T> FetchRequest<T> fetch(Bucket bucket, String key, Class<T> type) {
        return this.fetch(bucket.getName(), key, type);
    }

    @Override
    public <T> FetchRequest<T> fetch(Reference reference, Class<T> type) {
        return this.fetch(reference.getBucket(), reference.getKey(), type);
    }

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object) {
        return this.store(bucket.getName(), object);
    }

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, String key, T object) {
        return this.store(bucket.getName(), key, object);
    }

    @Override
    public <T> StoreRequest<T> store(Reference reference, T object) {
        return this.store(reference.getBucket(), reference.getKey(), object);
    }

    @Override
    public DeleteRequest delete(Bucket bucket, String key) {
        return this.delete(bucket.getName(), key);
    }

    @Override
    public DeleteRequest delete(Reference reference) {
        return this.delete(reference.getBucket(), reference.getKey());
    }
}
