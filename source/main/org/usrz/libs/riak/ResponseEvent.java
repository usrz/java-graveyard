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

import java.util.EventObject;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.usrz.libs.riak.utils.UncheckedExecutionException;

public class ResponseEvent<T> extends EventObject {

    private final CancellationException cancellationException;
    private final ExecutionException executionException;
    private final Response<T> response;

    public ResponseEvent(RiakClient client, Response<T> response) {
        super(client);
        if (response == null) throw new NullPointerException("Null response");
        this.response = response;
        cancellationException = null;
        executionException = null;
    }

    public ResponseEvent(RiakClient client, Throwable throwable) {
        super(client);
        if (throwable == null) throw new NullPointerException("Null throwable");
        this.response = null;
        if (throwable instanceof CancellationException) {
            this.cancellationException = (CancellationException) throwable;
            this.executionException = null;
        } else if (throwable instanceof ExecutionException) {
            this.executionException = (ExecutionException) throwable;
            final Throwable cause = executionException.getCause();
            if (cause instanceof CancellationException) {
                this.cancellationException = (CancellationException) cause;
            } else {
                this.cancellationException = null;
            }
        } else {
            this.executionException = new ExecutionException(throwable);
            this.cancellationException = null;
        }
    }

    @Override
    public RiakClient getSource() {
        return (RiakClient) super.source;
    }

    public Response<T> getResponse() {
        if (cancellationException != null) throw cancellationException;
        if (executionException != null) throw new UncheckedExecutionException(executionException);
        return response;
    }

    public T getContent() {
        return getResponse().getContent();
    }

    public Throwable getThrowable() {
        return cancellationException != null ? cancellationException :
               executionException != null ? executionException.getCause() :
                   null;
    }
}
