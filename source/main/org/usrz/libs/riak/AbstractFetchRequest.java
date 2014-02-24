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

    private final Class<T> type;

    protected AbstractFetchRequest(String bucket, String key, Class<T> type) {
        super(bucket, key);
        if (key == null) throw new NullPointerException("Null key");
        if (key.length() == 0) throw new NullPointerException("Empty key");
        this.type = type;
    }

    /* ====================================================================== */

    @Override
    protected final Future<Response<T>> execute(String bucket, String key)
    throws IOException {
        return this.execute(bucket, key, type);
    }

    protected abstract Future<Response<T>> execute(String bucket, String key, Class<T> type)
    throws IOException;

}
