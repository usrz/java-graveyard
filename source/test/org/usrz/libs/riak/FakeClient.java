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
import java.util.concurrent.Future;

import org.usrz.libs.riak.utils.IterableFuture;

public class FakeClient extends AbstractRiakClient {

    @Override
    public IterableFuture<Bucket> getBuckets() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IterableFuture<Key> getKeys(Bucket bucket) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> FetchRequest<T> fetch(Key key, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> StoreRequest<T> store(Key key, T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeleteRequest delete(Key key) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <T> Future<Response<T>> executeFetch(FetchRequest<T> request, Key key, Class<T> type) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <T> Future<Response<T>> executeStore(StoreRequest<T> request, Bucket bucket, T instance) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <T> Future<Response<T>> executeStore(StoreRequest<T> request, Key key, T instance) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Future<Response<Void>> executeDelete(DeleteRequest request, Key key) throws IOException {
        throw new UnsupportedOperationException();
    }

}
