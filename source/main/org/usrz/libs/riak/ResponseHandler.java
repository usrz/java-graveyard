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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;

import org.usrz.libs.logging.Log;

public abstract class ResponseHandler<T> implements Callable<T> {

    private static final Log log = new Log();

    private  PipedInputStream input;

    protected ResponseHandler() {
        /* Nothing to do */
    }

    public OutputStream getOutputStream()
    throws IllegalStateException, IOException {
        if (input != null) throw new IllegalStateException();
        input = new PipedInputStream();
        return new PipedOutputStream(input);
    }

    @Override
    public T call()
    throws Exception {
        try {
            return this.call(input);
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

    protected abstract T call(InputStream input)
    throws Exception;

}
