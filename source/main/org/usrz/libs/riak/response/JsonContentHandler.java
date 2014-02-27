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
package org.usrz.libs.riak.response;

import java.io.InputStream;

import org.usrz.libs.riak.PartialResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonContentHandler<T> extends PipedContentHandler<T> {

    private final ObjectMapper mapper;
    private final Class<T> type;

    public JsonContentHandler(ObjectMapper mapper, Class<T> type) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        if (type == null) throw new NullPointerException("Null type");
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    protected T read(PartialResponse<T> partial, InputStream input)
    throws Exception {

        /* Use a MappingIterator, as we don't want to fail on empty JSON */
        final JsonParser parser = mapper.getFactory().createParser(input);
        final MappingIterator<T> iterator = mapper.readValues(parser, type);

        /* Read only the first value, then close */
        while (iterator.hasNextValue()) try {
            return iterator.next();
        } finally {
            iterator.close();
        }

        /* Didn't even get the first value */
        return null;
    }
}
