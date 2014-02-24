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
package org.usrz.libs.riak.async;

import java.io.IOException;
import java.util.concurrent.Future;

import org.usrz.libs.riak.AbstractFetchRequest;
import org.usrz.libs.riak.Response;
import org.usrz.libs.utils.beans.Mapper;

public abstract class AsyncFetchRequest<T>
extends AbstractFetchRequest<T>
implements Mapper {

    private final AsyncRiakClient client;

    protected AsyncFetchRequest(AsyncRiakClient client, String bucket, String key, Class<T> type) {
        super(client.getBucket(bucket), key, type);
        this.client = client;
    }

    @Override
    protected Future<Response<T>> execute(String bucket, String key, Class<T> type)
    throws IOException {
        return client.executeFetch(this, bucket, key, type);
    }

}
