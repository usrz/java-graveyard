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

import org.usrz.libs.riak.response.JsonResponseHandler;

import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class AbstractJsonClient extends AbstractRiakClient {

    protected final ObjectMapper mapper;

    protected AbstractJsonClient(ObjectMapper mapper) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        this.mapper = mapper;
    }

    /* ====================================================================== */

    @Override
    public <T> FetchRequest<T> fetch(Key key, Class<T> type) {
        return this.fetch(key, new JsonResponseHandler<T>(mapper, type));
    }

    @Override
    public <T> FetchRequest<T> fetch(String bucket, String key, ResponseHandler<T> handler) {
        return this.fetch(getBucket(bucket), key, handler);
    }

    @Override
    public <T> FetchRequest<T> fetch(Bucket bucket, String key, ResponseHandler<T> handler) {
        return this.fetch(new Key(bucket, key), handler);
    }

    @Override
    public abstract <T> FetchRequest<T> fetch(Key key, ResponseHandler<T> handler);

    /* ====================================================================== */

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object) {
        return this.store(bucket, object, this.newHandler(object));
    }

    @Override
    public <T> StoreRequest<T> store(String bucket, T object, ResponseHandler<T> handler) {
        return this.store(getBucket(bucket), object, handler);
    }

    @Override
    public abstract <T> StoreRequest<T> store(Bucket bucket, T object, ResponseHandler<T> handler);

    /* ---------------------------------------------------------------------- */

    @Override
    public <T> StoreRequest<T> store(Key key, T object) {
        return this.store(key, object, this.newHandler(object));
    }

    @Override
    public <T> StoreRequest<T> store(String bucket, String key, T object, ResponseHandler<T> handler) {
        return this.store(getBucket(bucket), key, object, handler);
    }

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, String key, T object, ResponseHandler<T> handler) {
        return this.store(new Key(bucket, key), object, handler);
    }

    @Override
    public abstract <T> StoreRequest<T> store(Key key, T object, ResponseHandler<T> handler);

    /* ====================================================================== */

    private final <T> JsonResponseHandler<T> newHandler(T object) {
        @SuppressWarnings("unchecked")
        final Class<T> type = (Class<T>) object.getClass();
        return new JsonResponseHandler<T>(mapper, type);

    }
}
