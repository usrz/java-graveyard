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

import org.usrz.libs.riak.utils.IterableFuture;

public interface RiakClient {

    public Bucket getBucket(String name);

    public IterableFuture<Bucket> getBuckets()
    throws IOException;

    public IterableFuture<String> getBucketNames()
    throws IOException;

    public IterableFuture<String> getKeys(String bucket)
    throws IOException;

    public IterableFuture<String> getKeys(Bucket bucket)
    throws IOException;

    public IterableFuture<Reference> getReferences(String bucket)
    throws IOException;

    public IterableFuture<Reference> getReferences(Bucket bucket)
    throws IOException;


    public <T> FetchRequest<T> fetch(String bucket, String key, Class<T> type);

    public <T> FetchRequest<T> fetch(Bucket bucket, String key, Class<T> type);

    public <T> FetchRequest<T> fetch(Reference reference, Class<T> type);


    public <T> StoreRequest<T> store(String bucket, T object);

    public <T> StoreRequest<T> store(Bucket bucket, T object);

    public <T> StoreRequest<T> store(String bucket, String key, T object);

    public <T> StoreRequest<T> store(Bucket bucket, String key, T object);

    public <T> StoreRequest<T> store(Reference reference, T object);


    public DeleteRequest delete(String bucket, String key);

    public DeleteRequest delete(Bucket bucket, String key);

    public DeleteRequest delete(Reference reference);
}
