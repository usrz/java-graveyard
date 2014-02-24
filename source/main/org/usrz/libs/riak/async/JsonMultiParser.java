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

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.usrz.libs.riak.utils.QueuedIterableFuture;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMultiParser<T> implements Callable<Void> {

    private final QueuedIterableFuture<T> future;
    private final ObjectMapper mapper;
    private final InputStream input;
    private final Class<T> type;

    public JsonMultiParser(QueuedIterableFuture<T> future, ObjectMapper mapper, InputStream input, Class<T> type) {
        this.future = future;
        this.mapper = mapper;
        this.input = input;
        this.type = type;
    }

    @Override
    public Void call()
    throws Exception {
        try {
            final JsonParser parser = mapper.getFactory().createParser(input);
            final MappingIterator<T> iterator = mapper.readValues(parser, type);
            while (iterator.hasNextValue()) future.put(iterator.nextValue());
            return null;
        } catch (Exception exception) {
            future.fail(exception);
            throw exception;
        } finally {
            input.close();
        }
    }
}
