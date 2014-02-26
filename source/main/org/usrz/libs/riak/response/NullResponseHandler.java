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

import org.usrz.libs.riak.ResponseHandler;

public class NullResponseHandler extends ResponseHandler<Void> {

    public static final NullResponseHandler NULL_RESPONSE_HANDLER = new NullResponseHandler();

    private NullResponseHandler() {
        /* Nothing to do */
    }

    @Override
    protected Void call(InputStream input)
    throws IOException {
        final byte[] buffer = new byte[4096];
        while (input.read(buffer) >= 0) { }
        return null;
    }

}
