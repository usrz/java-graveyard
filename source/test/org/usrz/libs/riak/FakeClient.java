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

public class FakeClient extends AbstractRiakClient {

    @Override
    public IterableFuture<String> getBucketNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IterableFuture<String> getKeyNames(String bucket)
    throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> FetchRequest<T> fetch(String bucket, String key, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> StoreRequest<T> store(String bucket, T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> StoreRequest<T> store(String bucket, String key, T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeleteRequest delete(String bucket, String key) {
        throw new UnsupportedOperationException();
    }

}
