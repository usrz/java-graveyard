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
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.usrz.libs.logging.Log;
import org.usrz.libs.riak.AbstractDeleteRequest;
import org.usrz.libs.riak.AbstractFetchRequest;
import org.usrz.libs.riak.AbstractJsonClient;
import org.usrz.libs.riak.AbstractStoreRequest;
import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.DeleteRequest;
import org.usrz.libs.riak.FetchRequest;
import org.usrz.libs.riak.Index;
import org.usrz.libs.riak.IndexMap;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.LinksMap;
import org.usrz.libs.riak.Metadata;
import org.usrz.libs.riak.Quorum;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.ResponseHandler;
import org.usrz.libs.riak.RiakLocation;
import org.usrz.libs.riak.StoreRequest;
import org.usrz.libs.riak.annotations.RiakIntrospector;
import org.usrz.libs.riak.utils.IterableFuture;
import org.usrz.libs.riak.utils.QueuedIterableFuture;
import org.usrz.libs.riak.utils.RiakUtils;
import org.usrz.libs.riak.utils.SettableFuture;
import org.usrz.libs.riak.utils.WrappingIterableFuture;
import org.usrz.libs.utils.beans.InstanceBuilder;
import org.usrz.libs.utils.beans.Mapper;
import org.usrz.libs.utils.beans.MapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Request;
import com.ning.http.util.DateUtil;

public class AsyncRiakClient extends AbstractJsonClient {

    private final Log log = new Log();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final RiakIntrospector introspector = new RiakIntrospector(this);
    private final AsyncHttpClient client;

    private final Class<AbstractFetchRequest<?>> fetchRequestClass;
    private final Class<AbstractStoreRequest<?>> storeRequestClass;
    private final Class<AbstractDeleteRequest> deleteRequestClass;

    public AsyncRiakClient(AsyncHttpClient client) {
        super(new ObjectMapper());
        if (client == null) throw new NullPointerException("Null client");
        this.client = client;

        final MapperBuilder builder = new MapperBuilder();
        fetchRequestClass = builder.newClass(AbstractFetchRequest.class);
        storeRequestClass = builder.newClass(AbstractStoreRequest.class);
        deleteRequestClass = builder.newClass(AbstractDeleteRequest.class);
    }

    /* ====================================================================== */

    protected RiakIntrospector getIntrospector() {
        return introspector;
    }

    protected ExecutorService getExecutorService() {
        return executor;
    }

    protected ObjectMapper getObjectMapper() {
        return mapper;
    }

    /* ====================================================================== */

    @Override
    public IterableFuture<Bucket> getBuckets() throws IOException {
        final Request r = client.prepareGet("http://127.0.0.1:4198/buckets/").addQueryParameter("buckets", "stream").build();
        final AsyncChunkedHandler h = new AsyncChunkedHandler(this, r);
        final QueuedIterableFuture<String> f = h.getFuture();
        f.addFuture(client.executeRequest(r, h));

        return new WrappingIterableFuture<Bucket, String>(f) {
            @Override
            public Bucket next(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return getBucket(future.next(timeout, unit));
            }
        };
    }

    @Override
    public IterableFuture<Key> getKeys(final Bucket bucket) throws IOException {
        final Request r = client.prepareGet("http://127.0.0.1:4198" + bucket.getLocation() + "/keys/").addQueryParameter("keys", "stream").build();
        final AsyncChunkedHandler h = new AsyncChunkedHandler(this, r);
        final QueuedIterableFuture<String> f = h.getFuture();
        f.addFuture(client.executeRequest(r, h));

        return new WrappingIterableFuture<Key, String>(f) {
            @Override
            public Key next(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return new Key(bucket, future.next(timeout, unit));
            }
        };
    }

    /* ====================================================================== */

    @Override
    public <T> FetchRequest<T> fetch(Key key, ResponseHandler<T> handler) {
        log.trace("Preparing FETCH request for %s", key.getLocation());
        return InstanceBuilder.newInstance(fetchRequestClass, key, handler);
    }

    @Override
    protected <T> Future<Response<T>> executeFetch(FetchRequest<T> request, Key key, ResponseHandler<T> handler)
    throws IOException {
        final String url = getUrl(key);
        final Map<String, ?> p = ((Mapper) request).mappedProperties();
        final Request r = build(p, client.prepareGet(url)).build();
        final AsyncResponseHandler<T> h = new AsyncResponseHandler<T>(this, handler, r);
        final SettableFuture<Response<T>> f = h.getFuture();

        log.debug("Calling %s on %s", r.getMethod(), r.getUrl());
        f.addFuture(client.executeRequest(r, h));
        return f;
    }

    /* ====================================================================== */

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object, ResponseHandler<T> handler) {
        log.trace("Preparing STORE request for %s (no key)", bucket.getLocation());
        return InstanceBuilder.newInstance(storeRequestClass, bucket, object, handler, getIntrospector());
    }

    @Override
    public <T> StoreRequest<T> store(Key key, T object, ResponseHandler<T> handler) {
        log.trace("Preparing STORE request for %s (no key)", key.getLocation());
        return InstanceBuilder.newInstance(storeRequestClass, key, object, handler, getIntrospector());
    }

    @Override
    protected <T> Future<Response<T>> executeStore(StoreRequest<T> request, Bucket bucket, T instance, ResponseHandler<T> handler)
    throws IOException {

        final BoundRequestBuilder builder = client.preparePost(getUrl(bucket) + "keys/");
        final AsyncResponseHandler <T> asyncHandler = prepareStore(request, builder, instance, handler);
        final Request asyncRequest = asyncHandler.getRequest();

        final SettableFuture<Response<T>> future = asyncHandler.getFuture();

        log.debug("Calling %s on %s", asyncRequest.getMethod(), asyncRequest.getUrl());
        future.addFuture(client.executeRequest(asyncRequest, asyncHandler));

        return future;
    }

    @Override
    protected <T> Future<Response<T>> executeStore(StoreRequest<T> request, Key key, T instance, ResponseHandler<T> handler)
    throws IOException {

        final String url = getUrl(key);
        final BoundRequestBuilder builder = client.preparePut(url);
        final AsyncResponseHandler <T> asyncHandler = prepareStore(request, builder, instance, handler);
        final Request asyncRequest = asyncHandler.getRequest();
        final SettableFuture<Response<T>> future = asyncHandler.getFuture();

        /* Vector clock */
        final Map<String, ?> properties = ((Mapper)request).mappedProperties();
        final String vectorClock = (String) properties.get("vectorClock");
        if (vectorClock != null) {

            /* We have a vector clock, just do the PUT */
            asyncRequest.getHeaders().replace("X-Riak-Vclock", vectorClock);
            log.debug("Calling %s on %s", asyncRequest.getMethod(), asyncRequest.getUrl());
            future.addFuture(client.executeRequest(asyncRequest, asyncHandler));

        } else {

            /* Call "HEAD" to get the cector clock */
            final AsyncVectorClockHandler vh = new AsyncVectorClockHandler(client, asyncHandler);
            log.debug("Calling HEAD on %s", url);
            future.addFuture(client.prepareHead(url).execute(vh));

        }

        /* Return our future */
        return future;
    }

    private <T> AsyncResponseHandler<T> prepareStore(StoreRequest<T> request, BoundRequestBuilder builder, T instance, ResponseHandler<T> handler) {
        final Map<String, ?> properties = ((Mapper)request).mappedProperties();

        builder = build(request.getIndexMap(),
                  build(request.getLinksMap(),
                  build(request.getMetadata(),
                  build(properties, builder))));

        /* Return body (default is TRUE) */
        if (!properties.containsKey("returnBody")) builder.addQueryParameter("returnbody", "true");

        /* Append our body and create our request */
        final Request asyncRequest = builder.setBody(new AsyncJsonGenerator(this, instance))
                                            .addHeader("Content-Type", "application/json")
                                            .build();
        return new AsyncResponseHandler<T>(this, handler, asyncRequest);
    }

    /* ====================================================================== */

    @Override
    public DeleteRequest delete(Key key) {
        log.trace("Preparing DELETE request for %s", key.getLocation());
        return InstanceBuilder.newInstance(deleteRequestClass, key);
    }

    @Override
    protected Future<Response<Void>> executeDelete(DeleteRequest request, Key key)
    throws IOException {
        final String url = getUrl(key);
        final Map<String, ?> p = ((Mapper) request).mappedProperties();
        final Request r = build(p, client.prepareDelete(url)).build();
        final AsyncResponseHandler<Void> h = new AsyncResponseHandler<Void>(this, null, r);
        final SettableFuture<Response<Void>> f = h.getFuture();

        log.debug("Calling %s on %s", r.getMethod(), r.getUrl());
        f.addFuture(client.executeRequest(r, h));
        return f;
    }

    /* ====================================================================== */

    // TODO TODO TODO
    private String getUrl(RiakLocation location) {
        return "http://127.0.0.1:4198" + location.getLocation();
    }

    private BoundRequestBuilder build(Map<String, ?> properties, BoundRequestBuilder builder) {

        /*
         * From "ConditionalRequest":
         * - ifMatch // String
         * - ifModifiedSince // Date
         * - ifNoneMatch // String
         * - ifUnmodifiedSince // Date
         */
        if (properties.containsKey("ifMatch"))           builder.setHeader("If-Match",      (String) properties.get("ifMatch"));
        if (properties.containsKey("ifNoneMatch"))       builder.setHeader("If-None-Match", (String) properties.get("ifNoneMatch"));
        if (properties.containsKey("ifModifiedSince"))   builder.setHeader("If-Modified-Since",   DateUtil.formatDate((Date) properties.get("ifModifiedSince")));
        if (properties.containsKey("ifUnmodifiedSince")) builder.setHeader("If-Unmodified-Since", DateUtil.formatDate((Date) properties.get("ifUnmodifiedSince")));

        /*
         * From "OptionalBodyRequest":
         * - returnBody // boolean
         */
        if (properties.containsKey("returnBody")) builder.addQueryParameter("returnbody", ((Boolean) properties.get("returnBody")).toString());

        /*
         * From "BasicQuorumRequest":
         * - basicQuorum // boolean
         */
        if (properties.containsKey("basicQuorum")) builder.addQueryParameter("basic_quorum", ((Boolean) properties.get("basicQuorum")).toString());

        /*
         * From "ReadQuorumRequest":
         * - readQuorum // int or Quorum
         * - primaryReadQuorum // int or Quorum
         *
         * From "WriteQuorumRequest":
         * - writeQuorum // int or Quorum
         * - primaryWriteQuorum // int or Quorum
         * - durableWriteQuorum // int or Quorum
         */
        if (properties.containsKey("readQuorum"))         builder.addQueryParameter("r",  Quorum.getParameter(properties.get("readQuorum")));
        if (properties.containsKey("primaryReadQuorum"))  builder.addQueryParameter("pr", Quorum.getParameter(properties.get("primaryReadQuorum")));
        if (properties.containsKey("writeQuorum"))        builder.addQueryParameter("w",  Quorum.getParameter(properties.get("writeQuorum")));
        if (properties.containsKey("primaryWriteQuorum")) builder.addQueryParameter("pw", Quorum.getParameter(properties.get("primaryWriteQuorum")));
        if (properties.containsKey("durableWriteQuorum")) builder.addQueryParameter("dw", Quorum.getParameter(properties.get("durableWriteQuorum")));

        /*
         * From "SiblingsRequest":
         * - sibling // string
         */
        if (properties.containsKey("sibling")) builder.addQueryParameter("vtag", (String) properties.get("sibling"));

        /* Done! */
        return builder;
    }

    private BoundRequestBuilder build(IndexMap indexMap, BoundRequestBuilder builder) {
        for (Entry<Index, Set<String>> entry: indexMap.entrySet()) {
            final Index index = entry.getKey();
            builder.addHeader("X-Riak-Index-" + RiakUtils.encode(index.getName()) + index.getType().getSuffix(),
                              RiakUtils.encode(entry.getValue()));
        }
        return builder;
    }

    private BoundRequestBuilder build(LinksMap linksMap, BoundRequestBuilder builder) {
        for (Entry<String, Set<Key>> entry: linksMap.entrySet()) {
            final String tag = entry.getKey();
            for (Key key: entry.getValue()) {
                final String location = key.getLocation();
                builder.addHeader("Link", "<" + location + ">; riaktag=\"" + tag + "\"");
            }
        }
        return builder;
    }

    private BoundRequestBuilder build(Metadata metadata, BoundRequestBuilder builder) {
        for (Entry<String, Set<String>> entry: metadata.entrySet())
            builder.addHeader("X-Riak-Meta-" + RiakUtils.encode(entry.getKey()),
                              RiakUtils.encode(entry.getValue()));
        return builder;
    }
}
