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

import org.usrz.libs.riak.utils.RiakIntrospector;

public abstract class AbstractStoreRequest<T>
extends AbstractRequest<T, StoreRequest<T>>
implements StoreRequest<T> {

    private final MetadataBuilder metadata;
    private final IndexMapBuilder indexMap;
    private final LinksMapBuilder linksMap;
    private final T instance;

    protected AbstractStoreRequest(String bucket, T instance, RiakIntrospector introspector) {
        super(bucket, introspector.getKey(instance));
        this.metadata = new MetadataBuilder(introspector.getMetadata(instance));
        this.indexMap = new IndexMapBuilder(introspector.getIndexMap(instance));
        this.linksMap = new LinksMapBuilder(introspector.getLinksMap(instance));
        this.instance = instance;
    }

    /* ====================================================================== */

    @Override
    protected final Future<Response<T>> execute(String bucket, String key)
    throws IOException {
        return this.execute(bucket, key, instance);
    }

    protected abstract Future<Response<T>> execute(String bucket, String key, T instance)
    throws IOException;

    /* ====================================================================== */

    @Override
    public MetadataBuilder getMetadataBuilder() {
        return metadata;
    }

    @Override
    public IndexMapBuilder getIndexMapBuilder() {
        return indexMap;
    }

    @Override
    public LinksMapBuilder getLinksMapBuilder() {
        return linksMap;
    }

}
