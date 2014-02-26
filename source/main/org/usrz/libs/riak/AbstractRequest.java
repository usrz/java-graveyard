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

import org.usrz.libs.riak.requests.BucketedRequest;
import org.usrz.libs.riak.requests.KeyedRequest;

public abstract class AbstractRequest<T, R extends Request<T>
                                                 & KeyedRequest<T, R>
                                                 & BucketedRequest<T,R>>
implements Request<T>, KeyedRequest<T, R>, BucketedRequest<T, R> {

    private final R thisInstance;
    protected final ResponseHandler<T> handler;
    protected Bucket bucket;
    protected Key key;

    @SuppressWarnings("unchecked")
    protected AbstractRequest(Key key, ResponseHandler<T> handler) {
        if (handler == null) throw new NullPointerException("Null response handler");
        if (key == null) throw new NullPointerException("Null key");
        this.bucket = key.getBucket();
        this.key = key;
        this.handler = handler;
        thisInstance = (R) this;
    }

    @SuppressWarnings("unchecked")
    protected AbstractRequest(Bucket bucket, ResponseHandler<T> handler) {
        if (handler == null) throw new NullPointerException("Null response handler");
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.bucket = bucket;
        this.key = null;
        this.handler = handler;
        thisInstance = (R) this;
    }

    @SuppressWarnings("unchecked")
    protected AbstractRequest(Bucket bucket, String key, ResponseHandler<T> handler) {
        if (handler == null) throw new NullPointerException("Null response handler");
        if (bucket == null) throw new NullPointerException("Null bucket");
        this.key = key == null ? null : new Key(bucket, key);
        this.bucket = bucket;
        this.handler = handler;
        thisInstance = (R) this;
    }

    /* ====================================================================== */

    @Override
    public final Future<Response<T>> execute()
    throws IOException {
        return key == null ? execute(bucket) : execute(key);
    }

    protected Future<Response<T>> execute(Bucket bucket)
    throws IOException {
        throw new UnsupportedOperationException();
    }

    protected Future<Response<T>> execute(Key key)
    throws IOException {
        throw new UnsupportedOperationException();
    }

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
