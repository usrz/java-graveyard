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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.usrz.libs.logging.Log;
import org.usrz.libs.riak.ContentHandler;
import org.usrz.libs.riak.PartialResponse;
import org.usrz.libs.riak.Response;

public abstract class PipedContentHandler<T> implements ContentHandler<T> {

    private static final Log log = new Log();

    private PipedInputStream input;
    private PartialResponse<T> partial;

    protected PipedContentHandler() {
        /* Nothing to do */
    }

    @Override
    public OutputStream getOutputStream(PartialResponse<T> partial)
    throws IllegalStateException, IOException {
        if (input != null) throw new IllegalStateException();
        this.partial = partial;
        input = new PipedInputStream();
        return new PipedOutputStream(input);
    }

    @Override
    public Response<T> call()
    throws Exception {
        try {
            return new Response<T>(partial, read(partial, input));
        } finally {
            try {
                input.close();
            } catch (IOException exception) {
                /* This should never happen, but anyhow.... */
                log.error(exception, "I/O exception closing pipe");
            } finally {
                input = null;
            }
        }
    }

    protected abstract T read(PartialResponse<T> partial, InputStream input)
    throws Exception;

}
