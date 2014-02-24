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

import java.util.Date;

import org.usrz.libs.riak.IndexMap;
import org.usrz.libs.riak.LinksMap;
import org.usrz.libs.riak.Metadata;
import org.usrz.libs.riak.Reference;
import org.usrz.libs.riak.Response;

public class AsyncResponse<T> implements Response<T> {

    private final Metadata metadata = new Metadata();
    private final IndexMap indexMap = new IndexMap();
    private final LinksMap linksMap = new LinksMap();

    private String vectorClock;
    private Date lastModified;
    private Reference reference;
    private boolean successful;
    private int status;
    private T entity;

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

    @Override
    public String getVectorClock() {
        return vectorClock;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public T getEntity() {
        return entity;
    }

    protected void setVectorClock(String vectorClock) {
        this.vectorClock = vectorClock;
    }

    protected void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    protected void setReference(Reference reference) {
        this.reference = reference;
    }

    protected void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    protected void setStatus(int status) {
        this.status = status;
    }

    protected void setEntity(T entity) {
        this.entity = entity;
    }

}
