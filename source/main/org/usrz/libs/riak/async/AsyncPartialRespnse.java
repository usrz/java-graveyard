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
package org.usrz.libs.riak.async;

import static com.ning.http.util.DateUtil.parseDate;

import java.net.URI;
import java.util.Date;

import org.usrz.libs.riak.AbstractPartialResponse;
import org.usrz.libs.riak.IndexMapBuilder;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.LinksMapBuilder;
import org.usrz.libs.riak.MetadataBuilder;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseHeaders;

public class AsyncPartialRespnse<T> extends AbstractPartialResponse<T> {

    private final String vectorClock;
    private final Date lastModified;
    private final String location;
    private final Key key;

    protected AsyncPartialRespnse(AsyncRiakClient client, HttpResponseHeaders headers, int status) {
        super(client, status);

        /* Get our headers map */
        final FluentCaseInsensitiveStringsMap map = headers.getHeaders();

        /* Get the vector clock... *ALWAYS* */
        this.vectorClock = map.getFirstValue("X-Riak-Vclock");

        /* Set up our location and key */
        this.location = map.getFirstValue("Location");
        Key key = null;
        try {
            final URI locationUri = location == null ? headers.getUrl() :
                                    headers.getUrl().resolve(location);
            key = new Key(client, locationUri.getRawPath());
        } catch (Exception exception) {
            key = null; /* Invalid format, just set to null */
        } finally {
            this.key = key;
        }


        /* Set up our last modified date */
        Date lastModified = null;
        try {
            final String text = map.getFirstValue("Last-Modified");
            lastModified = lastModified == null ? null : parseDate(text);
        } catch (Exception exception) {
            lastModified = null; /* Invalid format, just set to null */
        } finally {
            this.lastModified = lastModified;
        }

        /* Parse indexes, links and metadata */
        getLinksMap().addAll(new LinksMapBuilder(client).parseHeaders(map.get("Link")).build());
        getIndexMap().addAll(new IndexMapBuilder().parseHeaders(map).build());
        getMetadata().addAll(new MetadataBuilder().parseHeaders(map).build());

    }

    @Override
    public String getVectorClock() {
        return vectorClock;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public String getLocation() {
        return location;
    }

}
