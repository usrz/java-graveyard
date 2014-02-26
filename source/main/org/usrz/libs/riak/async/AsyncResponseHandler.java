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

import static com.ning.http.util.DateUtil.parseDate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.usrz.libs.logging.Log;
import org.usrz.libs.riak.IndexMapBuilder;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.LinksMapBuilder;
import org.usrz.libs.riak.MetadataBuilder;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.ResponseHandler;
import org.usrz.libs.riak.response.ErrorResponseHandler;
import org.usrz.libs.riak.response.SiblingsResponseHandler;
import org.usrz.libs.riak.utils.SettableFuture;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;

public class AsyncResponseHandler<T> implements AsyncHandler<Void> {

    private final static Log log = new Log();

    // https://github.com/AsyncHttpClient/async-http-client/issues/489
    private final SettableFuture<Response<T>> settable = new SettableFuture<Response<T>>();

    private final ResponseHandler<T> handler;
    private final AsyncResponse<T> response;
    private final AsyncRiakClient client;
    private final Request request;

    private OutputStream output = null;
    private Future<T> future = null;

    protected AsyncResponseHandler(AsyncRiakClient client, ResponseHandler<T> handler, Request request) {
        log.trace("Handler for %s on %s created", request.getMethod(), request.getUrl());
        if (client == null) throw new NullPointerException("Null client");
        this.response = new AsyncResponse<T>(client);
        this.client = client;
        this.handler = handler;
        this.request = request;
    }

    // https://github.com/AsyncHttpClient/async-http-client/issues/489
    protected SettableFuture<Response<T>> getFuture() {
        return this.settable;
    }

    protected Request getRequest() {
        return this.request;
    }

    @Override
    public STATE onStatusReceived(HttpResponseStatus status)
    throws Exception {
        final int statusCode = status.getStatusCode();
        log.trace("Received status %d for request %s", statusCode, status.getUrl());

        /*
         * Statuses:
         * - Fetch Request:
         *   - 200 OK: read body
         *   - 300 Multiple Choices: siblings exception
         *   - 304 Not Modified: return null
         *   - 404 Not Found: return null
         * - Store request:
         *   - 200 OK: read body
         *   - 201 Created: on POST will give us a location
         *   - 204 No Content: on PUT when successful
         *   - 300 Multiple Choices: siblings exception
         * - Delete request:
         *   - 204 No Content: when successful
         *   - 404 Not Found: didn't delete anything
         */
        response.setStatus(statusCode);
        response.setSuccessful((statusCode == 200)
                            || (statusCode == 201)
                            || (statusCode == 204)
                            || (statusCode == 304));
        return STATE.CONTINUE;
    }

    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers)
    throws Exception {
        log.trace("Received headers for request %s", headers.getUrl());
        final FluentCaseInsensitiveStringsMap map = headers.getHeaders();

        /* See if we have to setup a siblings parser */
        final int statusCode = response.getStatus();

        if (response.isSuccessful()) {

            /* Parse only if we have some content (200 and 201, a type and content length) */
            if ((handler != null) && ((statusCode == 200) ||(statusCode == 201))) try {
                if (Integer.parseInt(map.getFirstValue("Content-Length")) > 0) {
                    output = handler.getOutputStream();
                    future = client.getExecutorService().submit(handler);
                    settable.addFuture(future);
                }
            } catch (NullPointerException | NumberFormatException exception) {
                /* Nothing to do, null or invalid "Content-Length" header */
            }

        } else if (statusCode == 300) {

            /* We have to setup a siblings parser */
            final String location = map.getFirstValue("Location");
            final URI locationUri = location == null ? headers.getUrl() : headers.getUrl().resolve(location);
            final Key key = new Key(client, locationUri.getRawPath());
            response.setKey(key);

            final SiblingsResponseHandler<T> handler = new SiblingsResponseHandler<>(key);
            output = handler.getOutputStream();
            future = client.getExecutorService().submit(handler);
            settable.addFuture(future);

            return STATE.CONTINUE;

        } else if (statusCode != 404) {

            /* We have to setup an error parser */
            final ErrorResponseHandler<T> handler = new ErrorResponseHandler<>(statusCode);
            output = handler.getOutputStream();
            future = client.getExecutorService().submit(handler);
            settable.addFuture(future);
            return STATE.CONTINUE;
        }

        /* Get the vector clock... *ALWAYS* */
        response.setVectorClock(map.getFirstValue("X-Riak-Vclock"));

        /* All the rest (location, last modified, indexes, metadata, .. only if successful */
        if (!response.isSuccessful()) return STATE.CONTINUE;

        /* Set up our key */
        final String location = map.getFirstValue("Location");
        final URI locationUri = location == null ? headers.getUrl() : headers.getUrl().resolve(location);
        response.setKey(new Key(client, locationUri.getRawPath()));

        /* Set up our last modified date */
        final String lastModified = map.getFirstValue("Last-Modified");
        if (lastModified != null) response.setLastModified(parseDate(lastModified));

        /* Parse indexes, links and metadata */
        response.getLinksMap().addAll(new LinksMapBuilder(client).parseHeaders(map.get("Link")).build());
        response.getIndexMap().addAll(new IndexMapBuilder().parseHeaders(map).build());
        response.getMetadata().addAll(new MetadataBuilder().parseHeaders(map).build());

        /* Continue! */
        return STATE.CONTINUE;
    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart)
    throws Exception {
        log.trace("Received body part for request %s", bodyPart.getUrl());
        if (output != null) bodyPart.writeTo(output);
        return STATE.CONTINUE;
    }

    @Override
    public Void onCompleted() {
        log.trace("Completed processing of %s", request.getUrl());

        if (output != null) try {
            output.close();
        } catch (Throwable throwable) {
            if (future != null) future.cancel(true);
            settable.fail(throwable);
            onThrowable(throwable);
        }

        try {
            final T result = future == null ? null : future.get(5, TimeUnit.SECONDS);
            response.setEntity(result);
            settable.set(response);
        } catch (Throwable throwable) {
            if (future != null) future.cancel(true);
            settable.fail(throwable);
            onThrowable(throwable);
        } finally {
            if ((future != null) && (!future.isDone())) future.cancel(true);
        }

        return null;
    }

    @Override
    public void onThrowable(Throwable throwable) {
        log.error(throwable, "Request: %s %s -> %d", request.getMethod(), request.getRawUrl(), response.getStatus());
        try {
            if (output != null) output.close();
        } catch (IOException exception) {
            log.error(exception, "I/O error closing pipe");
        } finally {
            if (future != null) future.cancel(true);
        }
    }
}
