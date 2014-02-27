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
import org.usrz.libs.riak.utils.IterableFuture;
import org.usrz.libs.riak.utils.QueueingFuture;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunkedContentHandler extends PipedContentHandler<IterableFuture<String>> {

    private final QueueingFuture<String> queue;
    private final ObjectMapper mapper;

    public ChunkedContentHandler(ObjectMapper mapper, QueueingFuture<String> queue) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        if (queue == null) throw new NullPointerException("Null queue");
        this.mapper = mapper;
        this.queue = queue;
    }

    @Override
    protected IterableFuture<String> read(PartialResponse<IterableFuture<String>> partial, InputStream input)
    throws Exception {
        final JsonParser parser = mapper.getFactory().createParser(input);
        final MappingIterator<JsonNode> iterator = mapper.readValues(parser, JsonNode.class);
        while (iterator.hasNextValue()) {
            final JsonNode bucketsOrKeys = iterator.next();
            for (JsonNode arrayOrString: bucketsOrKeys) {
                if (arrayOrString.isTextual()) {
                    queue.put(arrayOrString.asText());
                } else if (arrayOrString.isArray()) {
                    for (JsonNode arrayValue: arrayOrString) {
                        queue.put(arrayValue.asText());
                    }
                }
            }
        }

        /* If an exception is thrown, the ResponseHandler will fail the puttable */
        queue.close();
        return queue;
    }
}
