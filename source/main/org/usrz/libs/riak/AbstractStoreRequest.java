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

import org.usrz.libs.riak.annotations.RiakIntrospector;

public abstract class AbstractStoreRequest<T>
extends AbstractRequest<T, StoreRequest<T>>
implements StoreRequest<T> {

    private final Metadata metadata;
    private final IndexMap indexMap;
    private final LinksMap linksMap;
    private final T instance;

    protected AbstractStoreRequest(Bucket bucket, T instance, ResponseHandler<T> handler, RiakIntrospector introspector) {
        super(bucket, introspector.getKeyName(instance), handler);
        if (instance == null) throw new NullPointerException("Null instance");
        this.metadata = introspector.getMetadata(instance);
        this.indexMap = introspector.getIndexMap(instance);
        this.linksMap = introspector.getLinksMap(instance);
        this.instance = instance;
    }

    protected AbstractStoreRequest(Key key, T instance, ResponseHandler<T> handler, RiakIntrospector introspector) {
        super(key, handler);
        if (instance == null) throw new NullPointerException("Null instance");
        if (handler == null) throw new NullPointerException("Null response handler");
        this.metadata = introspector.getMetadata(instance);
        this.indexMap = introspector.getIndexMap(instance);
        this.linksMap = introspector.getLinksMap(instance);
        this.instance = instance;
    }

    /* ====================================================================== */

    @Override
    protected Future<Response<T>> execute(Bucket bucket)
    throws IOException {
        return ((AbstractRiakClient)bucket.getRiakClient()).executeStore(this, bucket, instance, handler);
    }

    @Override
    protected Future<Response<T>> execute(Key key)
    throws IOException {
        return ((AbstractRiakClient)key.getRiakClient()).executeStore(this, key, instance, handler);
    }

    /* ====================================================================== */

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public IndexMap getIndexMap() {
        return indexMap;
    }

    @Override
    public LinksMap getLinksMap() {
        return linksMap;
    }

}
