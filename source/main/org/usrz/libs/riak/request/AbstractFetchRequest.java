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
import org.usrz.libs.riak.ContentHandler;
import org.usrz.libs.riak.FetchRequest;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.Response;

public abstract class AbstractFetchRequest<T>
extends AbstractContentRequest<T, FetchRequest<T>>
implements FetchRequest<T> {

    protected AbstractFetchRequest(Key key, ContentHandler<T> handler) {
        super(key, handler);
    }

    @Override
    protected Future<Response<T>> execute(Bucket bucket, ContentHandler<T> handler)
    throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected abstract Future<Response<T>> execute(Key key, ContentHandler<T> handler)
    throws IOException;

}
