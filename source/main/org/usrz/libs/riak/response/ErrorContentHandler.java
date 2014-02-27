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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.usrz.libs.riak.PartialResponse;

public class ErrorContentHandler<T> extends PipedContentHandler<T> {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public ErrorContentHandler() {
        /* Nothing to do */
    }

    @Override
    protected T read(PartialResponse<T> partial, InputStream input)
    throws Exception {
        final ByteArrayOutputStream array = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        int read = -1;
        while ((read = input.read(buffer)) >= 0) {
            if (read > 0) array.write(buffer, 0, read);
        }
        final String message = new String(array.toByteArray(), "UTF8");
        final String normalized = WHITESPACE.matcher(message).replaceAll(" ").trim();;
        throw new IOException(partial.getStatus() + ": " + normalized);
    }
}
