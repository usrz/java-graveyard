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
import java.util.concurrent.Future;

public abstract class AbstractFetchRequest<T>
extends AbstractRequest<T, FetchRequest<T>>
implements FetchRequest<T> {

    protected AbstractFetchRequest(Key key, ResponseHandler<T> handler) {
        super(key, handler);
    }

    /* ====================================================================== */

    @Override
    protected Future<Response<T>> execute(Key key)
    throws IOException {
        return ((AbstractRiakClient)key.getRiakClient()).executeFetch(this, key, handler);
    }

}
