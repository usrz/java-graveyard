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
package org.usrz.libs.riak.request;

import java.io.IOException;
import java.util.concurrent.Future;

import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.DeleteRequest;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.Response;

public abstract class AbstractDeleteRequest
extends AbstractRequest<Boolean, DeleteRequest>
implements DeleteRequest {

    protected AbstractDeleteRequest(Key key) {
        super(key);
    }

    /* ====================================================================== */

    @Override
    protected Future<Response<Boolean>> execute(Bucket bucket)
    throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected abstract Future<Response<Boolean>> execute(Key key)
    throws IOException;

}
