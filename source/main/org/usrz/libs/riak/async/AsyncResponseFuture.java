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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.ResponseEvent;
import org.usrz.libs.riak.ResponseFuture;
import org.usrz.libs.riak.ResponseListener;
import org.usrz.libs.riak.utils.SettableFuture;

import com.ning.http.client.listenable.ExecutionList;

/* See https://github.com/AsyncHttpClient/async-http-client/issues/489 */
public class AsyncResponseFuture<T>
extends SettableFuture<Response<T>>
implements ResponseFuture<T> {

    private final ExecutionList executionList = new ExecutionList();
    private final AsyncRiakClient client;
    private final Executor executor;

    public AsyncResponseFuture(AsyncRiakClient client) {
        this.executor = client.getExecutorService();
        this.client = client;
    }

    @Override
    public AsyncResponseFuture<T> addListener(final ResponseListener<T> listener) {
        executionList.add(new Runnable() {

            @Override
            public void run() {
                try {
                    final Response<T> response = get();
                    if (response == null) {
                        listener.responseFailed(new ResponseEvent<T>(client, new IllegalStateException("Null response from Future")));
                    } else {
                        listener.responseHandled(new ResponseEvent<T>(client, response));
                    }
                } catch (Throwable throwable) {
                    if (throwable instanceof ExecutionException) throwable = throwable.getCause();
                    listener.responseFailed(new ResponseEvent<T>(client, throwable));
                }

            }}, executor);
        return this;
    }

    @Override
    public boolean set(Response<T> response) {
        try {
            return super.set(response);
        } finally {
            this.executionList.run();
        }
    }

    @Override
    public AsyncResponseFuture<T> addFuture(Future<?> future) {
        super.addFuture(future);
        return this;
    }

}
