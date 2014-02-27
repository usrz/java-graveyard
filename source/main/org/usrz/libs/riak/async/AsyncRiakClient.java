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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.usrz.libs.logging.Log;
import org.usrz.libs.riak.AbstractJsonClient;
import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.ContentHandler;
import org.usrz.libs.riak.DeleteRequest;
import org.usrz.libs.riak.FetchRequest;
import org.usrz.libs.riak.Index;
import org.usrz.libs.riak.IndexMap;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.LinksMap;
import org.usrz.libs.riak.Metadata;
import org.usrz.libs.riak.Quorum;
import org.usrz.libs.riak.ResponseEvent;
import org.usrz.libs.riak.ResponseFuture;
import org.usrz.libs.riak.ResponseListenerAdapter;
import org.usrz.libs.riak.RiakClient;
import org.usrz.libs.riak.StoreRequest;
import org.usrz.libs.riak.annotations.RiakIntrospector;
import org.usrz.libs.riak.response.BucketListContentHandler;
import org.usrz.libs.riak.response.KeyListContentHandler;
import org.usrz.libs.riak.utils.IterableFuture;
import org.usrz.libs.riak.utils.Puttable;
import org.usrz.libs.riak.utils.QueueingFuture;
import org.usrz.libs.riak.utils.RiakUtils;
import org.usrz.libs.utils.beans.InstanceBuilder;
import org.usrz.libs.utils.beans.MapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Request;
import com.ning.http.util.DateUtil;

public class AsyncRiakClient extends AbstractJsonClient implements RiakClient {

    private final Log log = new Log();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final RiakIntrospector introspector = new RiakIntrospector(this);
    private final AsyncHttpClient client;

    private final Class<AsyncFetchRequest<?>> fetchRequestClass;
    private final Class<AsyncStoreRequest<?>> storeRequestClass;
    private final Class<AsyncDeleteRequest> deleteRequestClass;

    public AsyncRiakClient(AsyncHttpClient client) {
        super(new ObjectMapper());
        if (client == null) throw new NullPointerException("Null client");
        this.client = client;

        final MapperBuilder builder = new MapperBuilder();
        fetchRequestClass = builder.newClass(AsyncFetchRequest.class);
        storeRequestClass = builder.newClass(AsyncStoreRequest.class);
        deleteRequestClass = builder.newClass(AsyncDeleteRequest.class);
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
    public IterableFuture<Bucket> getBuckets()
    throws IOException {
        final QueueingFuture<Bucket> iterable = new QueueingFuture<>();

        final Request request = prepareGet("/buckets?buckets=stream").build();
        final BucketListContentHandler handler = new BucketListContentHandler(mapper, iterable);

        return iterable.notify(iterate(request, iterable, handler));
    }

    @Override
    public IterableFuture<Key> getKeys(final Bucket bucket)
    throws IOException {
        final QueueingFuture<Key> iterable = new QueueingFuture<>();

        final Request request = prepareGet(bucket.getLocation() + "keys/?keys=stream").build();
        final KeyListContentHandler handler = new KeyListContentHandler(mapper, bucket, iterable);

        return iterable.notify(iterate(request, iterable, handler));
    }

    /* ====================================================================== */

    @Override
    public <T> FetchRequest<T> fetch(Key key, ContentHandler<T> handler) {
        log.trace("Preparing FETCH request for %s", key.getLocation());
        return InstanceBuilder.newInstance(fetchRequestClass, this, key, handler);
    }


    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object, ContentHandler<T> handler) {
        log.trace("Preparing STORE request for %s (no key)", bucket.getLocation());
        return InstanceBuilder.newInstance(storeRequestClass, this, bucket, object, handler);
    }

    @Override
    public <T> StoreRequest<T> store(Key key, T object, ContentHandler<T> handler) {
        log.trace("Preparing STORE request for %s (no key)", key.getLocation());
        return InstanceBuilder.newInstance(storeRequestClass, this, key, object, handler);
    }

    @Override
    public DeleteRequest delete(Key key) {
        log.trace("Preparing DELETE request for %s", key.getLocation());
        return InstanceBuilder.newInstance(deleteRequestClass, this, key);
    }

    /* ====================================================================== */

    private final String getUrl(String location) {
        return "http://127.0.0.1:4198" + location;
    }

    protected BoundRequestBuilder prepareHead(String location) {
        return client.prepareHead(getUrl(location));
    }

    protected BoundRequestBuilder prepareGet(String location) {
        return client.prepareGet(getUrl(location));
    }

    protected BoundRequestBuilder preparePost(String location) {
        return client.preparePost(getUrl(location));
    }

    protected BoundRequestBuilder preparePut(String location) {
        return client.preparePut(getUrl(location));
    }

    protected BoundRequestBuilder prepareDelete(String location) {
        return client.prepareDelete(getUrl(location));
    }

    protected <T> ResponseFuture<T> execute(Request request, ContentHandler<T> handler)
    throws IOException {
        log.debug("Calling %s on %s", request.getMethod(), request.getUrl());

        /* See https://github.com/AsyncHttpClient/async-http-client/issues/489 */
        final AsyncResponseFuture<T> future = new AsyncResponseFuture<>(this);
        future.notify(client.executeRequest(request, new AsyncResponseHandler<>(this, request, handler, future)));
        return future;

    }

    protected <T, R> ResponseFuture<R> iterate(final Request request, final Puttable<T> iterable, ContentHandler<R> handler)
    throws IOException {
        return this.execute(request, handler)
                .addListener(new ResponseListenerAdapter<R>() {

                    @Override
                    public void responseHandled(ResponseEvent<R> event) {
                        final int status = event.getResponse().getStatus();
                        if (status == 200) return;
                        iterable.fail(new IOException("Invalid status " + status + " for " + request.getUrl()));
                    }

                    @Override
                    public void responseFailed(ResponseEvent<R> event) {
                        iterable.fail(event.getThrowable());
                    }

                });
    }

    /* ====================================================================== */

    protected BoundRequestBuilder instrument(Map<String, ?> properties, BoundRequestBuilder builder) {

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

    protected BoundRequestBuilder instrument(IndexMap indexMap, BoundRequestBuilder builder) {
        for (Entry<Index, Set<String>> entry: indexMap.entrySet()) {
            final Index index = entry.getKey();
            builder.addHeader("X-Riak-Index-" + RiakUtils.encode(index.getName()) + index.getType().getSuffix(),
                              RiakUtils.encode(entry.getValue()));
        }
        return builder;
    }

    protected BoundRequestBuilder instrument(LinksMap linksMap, BoundRequestBuilder builder) {
        for (Entry<String, Set<Key>> entry: linksMap.entrySet()) {
            final String tag = entry.getKey();
            for (Key key: entry.getValue()) {
                final String location = key.getLocation();
                builder.addHeader("Link", "<" + location + ">; riaktag=\"" + tag + "\"");
            }
        }
        return builder;
    }

    protected BoundRequestBuilder instrument(Metadata metadata, BoundRequestBuilder builder) {
        for (Entry<String, Set<String>> entry: metadata.entrySet())
            builder.addHeader("X-Riak-Meta-" + RiakUtils.encode(entry.getKey()),
                              RiakUtils.encode(entry.getValue()));
        return builder;
    }

}
