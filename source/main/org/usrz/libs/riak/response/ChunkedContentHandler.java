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

import static java.lang.Boolean.FALSE;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.usrz.libs.riak.PartialResponse;
import org.usrz.libs.riak.utils.Puttable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChunkedContentHandler extends PipedContentHandler<Boolean> {

    private final Puttable<String> puttable;
    private final ObjectMapper mapper;

    public ChunkedContentHandler(ObjectMapper mapper, Puttable<String> puttable) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        if (puttable == null) throw new NullPointerException("Null queue");
        this.mapper = mapper;
        this.puttable = puttable;
    }

    @Override
    protected Boolean read(PartialResponse<Boolean> partial, InputStream input)
    throws Exception {
        try {
            final JsonParser parser = mapper.getFactory().createParser(input);
            final MappingIterator<JsonNode> iterator = mapper.readValues(parser, JsonNode.class);
            while (iterator.hasNextValue()) {
                final JsonNode bucketsOrKeys = iterator.next();
                for (JsonNode arrayOrString: bucketsOrKeys) {
                    if (arrayOrString.isTextual()) {
                        if (!puttable.put(arrayOrString.asText())) return FALSE;
                    } else if (arrayOrString.isArray()) {
                        for (JsonNode arrayValue: arrayOrString) {
                            if (!puttable.put(arrayValue.asText())) return FALSE;
                        }
                    }
                }
            }
            return puttable.close();
        } catch (Throwable throwable) {
            puttable.fail(throwable);
            if (throwable instanceof Exception) throw (Exception) throwable;
            throw new ExecutionException(throwable);
        }
    }
}
