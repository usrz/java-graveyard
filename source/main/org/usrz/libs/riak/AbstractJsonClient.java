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

import org.usrz.libs.riak.response.JsonContentHandler;

import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class AbstractJsonClient extends AbstractRiakClient {

    protected final ObjectMapper mapper;

    protected AbstractJsonClient(ObjectMapper mapper) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        this.mapper = mapper;
    }

    @Override
    public <T> FetchRequest<T> fetch(Key key, Class<T> type) {
        final JsonContentHandler <T> handler = new JsonContentHandler<T>(mapper, type);
        return this.fetch(key, handler);
    }

    /* ====================================================================== */

    @Override
    public <T> StoreRequest<T> store(Bucket bucket, T object) {
        final JsonContentHandler <T> handler = new JsonContentHandler<T>(mapper, getType(object));
        return this.store(bucket, object, handler);
    }

    @Override
    public <T> StoreRequest<T> store(Key key, T object) {
        final JsonContentHandler <T> handler = new JsonContentHandler<T>(mapper, getType(object));
        return this.store(key, object, handler);
    }

    /* ====================================================================== */

    @SuppressWarnings("unchecked")
    private final <T> Class<T> getType(T object) {
        return (Class<T>) object.getClass();
    }
}
