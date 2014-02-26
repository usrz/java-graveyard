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

import org.usrz.libs.riak.ResponseHandler;
import org.usrz.libs.riak.utils.QueuedIterableFuture;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunksResponseHandler extends ResponseHandler<Void> {

    private final QueuedIterableFuture<String> iterable;
    private final ObjectMapper mapper;

    public ChunksResponseHandler(ObjectMapper mapper, QueuedIterableFuture<String> iterable) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        if (iterable == null) throw new NullPointerException("Null iterable future");
        this.mapper = mapper;
        this.iterable = iterable;
    }

    @Override
    protected Void call(InputStream input)
    throws Exception {
        try {
            final JsonParser parser = mapper.getFactory().createParser(input);
            final MappingIterator<JsonNode> iterator = mapper.readValues(parser, JsonNode.class);
            while (iterator.hasNextValue()) {
                final JsonNode bucketsOrKeys = iterator.next();
                for (JsonNode arrayOrString: bucketsOrKeys) {
                    if (arrayOrString.isTextual()) {
                        iterable.put(arrayOrString.asText());
                    } else if (arrayOrString.isArray()) {
                        for (JsonNode arrayValue: arrayOrString) {
                            iterable.put(arrayValue.asText());
                        }
                    }
                }
            }
            iterable.close();
            return null;
        } catch (Exception exception) {
            iterable.fail(exception);
            throw exception;
        } finally {
            input.close();
        }
    }
}
