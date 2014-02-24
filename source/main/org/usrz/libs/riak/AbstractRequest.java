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

import org.usrz.libs.riak.aspects.BucketedRequest;
import org.usrz.libs.riak.aspects.KeyedRequest;
import org.usrz.libs.riak.aspects.Request;

public abstract class AbstractRequest<T, R extends Request<T>
                                                 & KeyedRequest<T, R>
                                                 & BucketedRequest<T,R>>
implements Request<T>, KeyedRequest<T, R>, BucketedRequest<T, R> {

    private final R thisInstance;
    private String bucket;
    private String key;

    @SuppressWarnings("unchecked")
    protected AbstractRequest(String bucket, String key) {
        if (bucket == null) throw new NullPointerException("Null bucket name");
        if (bucket.length() == 0) throw new NullPointerException("Empty bucket name");
        thisInstance = (R) this;
        this.bucket = bucket;
        this.key = key;
    }

    /* ====================================================================== */

    @Override
    public final Future<Response<T>> execute()
    throws IOException {
        return this.execute(bucket, key);
    }

    protected abstract Future<Response<T>> execute(String bucket, String key)
    throws IOException ;

    /* ====================================================================== */

    @Override
    public final R setKey(String key) {
        this.key = key;
        return thisInstance;
    }

    @Override
    public final R setBucket(String bucket) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.bucket = bucket;
        return thisInstance;
    }

    @Override
    public final R setBucket(Bucket bucket) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.bucket = bucket.getName();
        return thisInstance;
    }

}
