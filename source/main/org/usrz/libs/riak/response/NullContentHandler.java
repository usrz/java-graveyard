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
package org.usrz.libs.riak.response;

import java.io.IOException;
import java.io.OutputStream;

import org.usrz.libs.riak.ContentHandler;
import org.usrz.libs.riak.PartialResponse;
import org.usrz.libs.riak.Response;

public class NullContentHandler<T> implements ContentHandler<T> {

    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override public void write(int b) {}
        @Override public void write(byte[] b) {}
        @Override public void write(byte[] b, int off, int len) {}
        @Override public void flush() {}
        @Override public void close() {}
    };

    private PartialResponse<T> partial;

    public NullContentHandler() {
        /* Nothing to do */
    }

    @Override
    public OutputStream getOutputStream(PartialResponse<T> partial)
    throws IllegalStateException, IOException {
        if (this.partial != null) throw new IllegalStateException();
        this.partial = partial;
        return NULL_OUTPUT_STREAM;
    }

    @Override
    public final Response<T> call()
    throws Exception {
        try {
            return new Response<T>(partial, call(partial));
        } finally {
            partial = null;
        }
    }

    protected T call(PartialResponse<T> partial)
    throws Exception {
        return null;
    }

}
