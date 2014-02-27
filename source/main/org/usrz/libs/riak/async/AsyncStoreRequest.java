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
import java.util.Map;
import java.util.concurrent.Future;

import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.ContentHandler;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.request.AbstractStoreRequest;
import org.usrz.libs.riak.response.VectorClockContentHandler;
import org.usrz.libs.riak.utils.SettableFuture;
import org.usrz.libs.utils.beans.Mapper;

import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Request;

public abstract class AsyncStoreRequest<T>
extends AbstractStoreRequest<T>
implements Mapper {

    private final AsyncRiakClient client;

    public AsyncStoreRequest(AsyncRiakClient client, Bucket bucket, T instance, ContentHandler<T> handler) {
        super(bucket, instance, handler, client.getIntrospector());
        this.client = client;
    }

    public AsyncStoreRequest(AsyncRiakClient client, Key key, T instance, ContentHandler<T> handler) {
        super(key, instance, handler, client.getIntrospector());
        this.client = client;
    }

    private BoundRequestBuilder prepare(BoundRequestBuilder builder, T instance) {
        final Map<String, ?> properties = mappedProperties();

        /* Return body */
        builder.addQueryParameter("returnbody", Boolean.toString(getReturnBody()));

        /* Append our body */
        builder.setBody(new AsyncJsonGenerator(client, instance))
               .addHeader("Content-Type", "application/json");

        /* Instrument the rest of the request */
        return client.instrument(properties,
               client.instrument(getIndexMap(),
               client.instrument(getLinksMap(),
               client.instrument(getMetadata(),
                       builder))));
    }

    @Override
    protected Future<Response<T>> execute(Bucket bucket, T instance, ContentHandler<T> handler)
    throws IOException {

        final String location = bucket.getLocation() + "keys/";
        final BoundRequestBuilder builder = client.preparePost(location);
        final Request request = prepare(builder, instance).build();
        return client.execute(builder, request, handler);

    }

    @Override
    protected Future<Response<T>> execute(final Key key, T instance, final ContentHandler<T> handler, String vectorClock)
    throws IOException {

        final BoundRequestBuilder builder = client.preparePut(key.getLocation());
        final Request request = prepare(builder, instance).build();

        /* Vector clock */
        if (vectorClock != null) {

            /* We have a vector clock, just do the PUT */
            request.getHeaders().replace("X-Riak-Vclock", vectorClock);
            return client.execute(builder, request, handler);

        } else {

            /* Call "HEAD" to get the cector clock */
            final SettableFuture<Response<T>> future = new SettableFuture<>();
            future.addFuture(client.getExecutorService().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String vclock = client.fetch(key, new VectorClockContentHandler())
                                                        .setReturnBody(false)
                                                        .execute()
                                                        .get()
                                                        .getContent();

                            /* We might now have a vector clock, just do the PUT */
                            request.getHeaders().replace("X-Riak-Vclock", vclock);
                            future.set(client.execute(builder, request, handler).get());
                        } catch (Throwable throwable) {
                            future.fail(throwable);
                        }
                    }
                }));
            return future;
        }
    }
}
