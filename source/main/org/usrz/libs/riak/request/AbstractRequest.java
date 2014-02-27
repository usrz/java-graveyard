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
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.Request;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.RiakClient;

public abstract class AbstractRequest<T, R extends Request<T>
                                                 & KeyedRequest<T, R>
                                                 & BucketedRequest<T,R>>
implements Request<T>, KeyedRequest<T, R>, BucketedRequest<T, R> {

    protected final R thisInstance;
    private Bucket bucket;
    private Key key;

    @SuppressWarnings("unchecked")
    protected AbstractRequest(Key key) {
        if (key == null) throw new NullPointerException("Null key");
        this.bucket = key.getBucket();
        this.key = key;
        thisInstance = (R) this;
    }

    @SuppressWarnings("unchecked")
    protected AbstractRequest(Bucket bucket) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.bucket = bucket;
        this.key = null;
        thisInstance = (R) this;
    }

    @SuppressWarnings("unchecked")
    protected AbstractRequest(Bucket bucket, String key) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.key = key == null ? null : new Key(bucket, key);
        this.bucket = bucket;
        thisInstance = (R) this;
    }

    /* ====================================================================== */

    @Override
    public final Future<Response<T>> execute()
    throws IOException {
        return key == null ? execute(bucket) : execute(key);
    }

    protected abstract Future<Response<T>> execute(Bucket bucket)
    throws IOException;

    protected abstract Future<Response<T>> execute(Key key)
    throws IOException;

    /* ====================================================================== */

    @Override
    public final RiakClient getRiakClient() {
        return bucket.getRiakClient();
    }

    /* ====================================================================== */

    @Override
    public final R setKey(String key) {
        if (bucket == null) throw new NullPointerException("Null key");
        this.key = new Key(bucket, key);
        return thisInstance;
    }

    @Override
    public final R setKey(Key key) {
        if (key == null) throw new NullPointerException("Null key");
        this.bucket = key.getBucket();
        this.key = key;
        return thisInstance;
    }

    @Override
    public final R setBucket(String bucket) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.bucket = this.bucket.getRiakClient().getBucket(bucket);
        return thisInstance;
    }

    @Override
    public final R setBucket(Bucket bucket) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.bucket = bucket;
        return thisInstance;
    }

}
