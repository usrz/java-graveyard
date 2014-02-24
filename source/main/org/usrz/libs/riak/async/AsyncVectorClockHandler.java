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

import org.usrz.libs.logging.Log;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;

public class AsyncVectorClockHandler implements AsyncHandler<Void> {

    private static final Log log = new Log();

    private final AsyncResponseHandler<?> handler;
    private final AsyncHttpClient client;

    private String vectorClock = null;

    public AsyncVectorClockHandler(AsyncHttpClient client, AsyncResponseHandler<?> handler) {
        log.trace("Vector Clock Handler %s created", handler.getRequest().getUrl());
        this.handler = handler;
        this.client = client;
    }

    @Override
    public STATE onStatusReceived(HttpResponseStatus status)
    throws Exception {
        /* Don't check for status, just go */
        log.trace("Vector Clock status for %s is %d", status.getUrl(), status.getStatusCode());
        return STATE.CONTINUE;
    }

    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers)
    throws Exception {
        log.trace("Vector Clock headers for %s received", headers.getUrl());
        vectorClock = headers.getHeaders().getFirstValue("X-Riak-Vclock");
        return STATE.ABORT;
    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart)
    throws Exception {
        return STATE.ABORT;
    }

    @Override
    public Void onCompleted() throws Exception {
        final Request request = handler.getRequest();

        if (vectorClock != null) {
            request.getHeaders().replace("X-Riak-Vclock", vectorClock);
        }

        log.debug("Calling %s on %s (vclock=%s)", request.getMethod(), request.getUrl(), vectorClock);
        handler.getFuture().addFuture(client.executeRequest(request, handler));
        return null;
    }


    @Override
    public void onThrowable(Throwable throwable) {
        log.error(throwable, "Exception detected attempting to find vector clock for %s", handler.getRequest().getUrl());
        handler.getFuture().fail(throwable);
    }

}
