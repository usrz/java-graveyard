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

import java.util.Date;
import java.util.Map;

public abstract class AbstractPartialResponse<T> implements PartialResponse<T> {

    private final LinksMap linksMap;
    private final IndexMap indexMap;
    private final Metadata metadata;
    private final RiakClient client;
    private final int status;

    protected AbstractPartialResponse(PartialResponse<T> partial) {
        this(partial.getRiakClient(),
             partial.getLinksMap(),
             partial.getIndexMap(),
             partial.getMetadata(),
             partial.getStatus());
    }

    protected AbstractPartialResponse(RiakClient client,
                                      Map<String, ? extends Iterable<String>> map,
                                      int status) {
        this(client,
             new LinksMapBuilder(client).parseHeaders(map.get("Link")).build(),
             new IndexMapBuilder().parseHeaders(map).build(),
             new MetadataBuilder().parseHeaders(map).build(),
             status);
    }

        protected AbstractPartialResponse(RiakClient client,
                                      LinksMap linksMap,
                                      IndexMap indexMap,
                                      Metadata metadata,
                                      int status) {
        if (client == null) throw new NullPointerException("Null client");
        this.linksMap = linksMap == null ? new LinksMap(client) : linksMap;
        this.indexMap = indexMap == null ? new IndexMap() : indexMap;
        this.metadata = metadata == null ? new Metadata() : metadata;
        this.client = client;
        this.status = status;
    }

    @Override
    public final RiakClient getRiakClient() {
        return client;
    }

    @Override
    public final Metadata getMetadata() {
        return metadata;
    }

    @Override
    public final IndexMap getIndexMap() {
        return indexMap;
    }

    @Override
    public final LinksMap getLinksMap() {
        return linksMap;
    }

    @Override
    public final boolean isSuccessful() {
        return ((status == 200)    // 200 OK
             || (status == 201)    // 201 Created
             || (status == 204)    // 204 No Content
             || (status == 304));  // 304 Not Modified
    }

    @Override
    public final int getStatus() {
        return status;
    }

    @Override
    public abstract String getVectorClock();

    @Override
    public abstract Date getLastModified();

    @Override
    public abstract Key getKey();

}
