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
import org.usrz.libs.riak.AbstractRiakClient;
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

public class AsyncRiakClient extends AbstractRiakClient {

    private final Log log = new Log();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final RiakIntrospector introspector = new RiakIntrospector(this);
    private final ObjectMapper mapper = new ObjectMapper();
    private final AsyncHttpClient client;

    private final Class<AbstractFetchRequest<?>> fetchRequestClass;
    private final Class<AbstractStoreRequest<?>> storeRequestClass;
    private final Class<AbstractDeleteRequest> deleteRequestClass;

    public AsyncRiakClient(AsyncHttpClient client) {
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
    public <T> FetchRequest<T> fetch(Key key, Class<T> type) {
        log.trace("Preparing FETCH request for %s", key.getLocation());
        return InstanceBuilder.newInstance(fetchRequestClass, key, type);
    }

    @Override
    protected <T> Future<Response<T>> executeFetch(FetchRequest<T> request, Key key, Class<T> type)
    throws IOException {
        final String url = getUrl(key.getBucketName(), key.getName());
        final Request r = build((Mapper) request, client.prepareGet(url)).build();
        final AsyncResponseHandler<T> h = new AsyncResponseHandler<T>(this, type, r);
        final SettableFuture<Response<T>> f = h.getFuture();

        log.debug("Calling %s on %s", r.getMethod(), r.getUrl());
        f.addFuture(client.executeRequest(r, h));
        return f;
    }

    /* ====================================================================== */

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object) {
        log.trace("Preparing STORE request for %s (no key)", bucket.getLocation());
        return InstanceBuilder.newInstance(storeRequestClass, bucket, object, getIntrospector());
    }

    @Override
    public <T> StoreRequest<T> store(Key key, T object) {
        log.trace("Preparing STORE request for %s (no key)", key.getLocation());
        return InstanceBuilder.newInstance(storeRequestClass, key, object, getIntrospector());
    }

    @Override
    protected <T> Future<Response<T>> executeStore(StoreRequest<T> request, Bucket bucket, T instance)
    throws IOException {
        return this.executeStore(request, bucket, null, instance);
    }

    @Override
    protected <T> Future<Response<T>> executeStore(StoreRequest<T> request, Key key, T instance)
    throws IOException {
        return this.executeStore(request, key.getBucket(), key.getName(), instance);
    }

    // TODO rewrite, and avoid unnecessary casts!
    private <T> Future<Response<T>> executeStore(StoreRequest<T> request, Bucket bucket, String key, T instance)
    throws IOException {
        final Map<String, ?> properties = ((Mapper)request).mappedProperties();

        @SuppressWarnings("unchecked")
        final Class<T> type = (Class<T>) instance.getClass();
        final String url = getUrl(bucket.getName(), key);
        final BoundRequestBuilder b = build(request.getIndexMap(),
                                      build(request.getLinksMap(),
                                      build(request.getMetadata(),
                                      build((Mapper) request, key == null ?
                                              client.preparePost(url) :
                                              client.preparePut(url)
                                          ))));

        /* Return body (default is TRUE) */
        if (!properties.containsKey("returnBody")) b.addQueryParameter("returnbody", "true");

        /* Vector clock (if any) */
        final String vectorClock = (String) properties.get("vectorClock");
        if (vectorClock != null) b.setHeader("X-Riak-Vclock", vectorClock);

        /* Append our body and create our request */
        final Request r = b.setBody(new AsyncJsonGenerator(this, instance))
                           .addHeader("Content-Type", "application/json")
                           .build();
        final AsyncResponseHandler<T> h = new AsyncResponseHandler<T>(this, type, r);
        final SettableFuture<Response<T>> f = h.getFuture();

        /* At this point we check if we need to do a HEAD request first */
        if ((key != null) && (vectorClock == null)) {

            /* Let's figure out what's our vector clock first */
            final AsyncVectorClockHandler handler = new AsyncVectorClockHandler(client, h);
            log.debug("Calling HEAD on %s", url);
            f.addFuture(client.prepareHead(url).execute(handler));

        } else {

            /* We have either no key, or a vector clock, proceed! */
            log.debug("Calling %s on %s", r.getMethod(), r.getUrl());
            f.addFuture(client.executeRequest(r, h));

        }

        return f;
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
        final String url = getUrl(key.getBucketName(), key.getName());
        final Request r = build((Mapper) request, client.prepareDelete(url)).build();
        final AsyncResponseHandler<Void> h = new AsyncResponseHandler<Void>(this, null, r);
        final SettableFuture<Response<Void>> f = h.getFuture();

        log.debug("Calling %s on %s", r.getMethod(), r.getUrl());
        f.addFuture(client.executeRequest(r, h));
        return f;
    }

    /* ====================================================================== */

    // TODO TODO TODO
    private String getUrl(String bucket, String key) {
        final StringBuilder builder = new StringBuilder("http://127.0.0.1:4198/buckets/");
        if (bucket != null) {
            builder.append(RiakUtils.encode(bucket)).append("/keys/");
            if (key != null) builder.append(RiakUtils.encode(key));
        }
        return builder.toString();
    }

    private BoundRequestBuilder build(Mapper mapper, BoundRequestBuilder builder) {
        final Map<String, ?> properties = mapper.mappedProperties();

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
