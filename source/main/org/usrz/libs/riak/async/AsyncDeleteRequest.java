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

import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.request.AbstractDeleteRequest;
import org.usrz.libs.riak.response.SuccessContentHandler;
import org.usrz.libs.utils.beans.Mapper;

import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Request;

public abstract class AsyncDeleteRequest
extends AbstractDeleteRequest
implements Mapper {

    private final AsyncRiakClient client;

    public AsyncDeleteRequest(AsyncRiakClient client, Key key) {
        super(key);
        this.client = client;
    }

    @Override
    protected Future<Response<Boolean>> execute(Key key)
    throws IOException {

        final BoundRequestBuilder builder = client.prepareDelete(key.getLocation());
        final Request request = client.instrument(mappedProperties(), builder).build();
        return client.execute(builder, request, new SuccessContentHandler()); // TODO: should use null handler?

    }

}
