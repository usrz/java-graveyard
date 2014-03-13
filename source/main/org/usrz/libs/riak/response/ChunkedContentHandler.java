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
import java.util.concurrent.ExecutionException;

import org.usrz.libs.riak.PartialResponse;
import org.usrz.libs.utils.futures.Puttable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ChunkedContentHandler<T, H extends ChunkedContentHandler<T, H>>
extends PipedContentHandler<Boolean> {

    private final Class<? extends Chunk<T, H>> chunkType;
    private final Puttable<T> puttable;
    private final ObjectMapper mapper;
    private final H thisInstance;

    @SuppressWarnings("unchecked")
    public ChunkedContentHandler(ObjectMapper mapper, Puttable<T> puttable, Class<? extends Chunk<T, H>> chunkType) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        if (puttable == null) throw new NullPointerException("Null queue");
        if (chunkType == null) throw new NullPointerException("Null chunk type");
        this.mapper = mapper;
        this.puttable = puttable;
        this.chunkType = chunkType;
        thisInstance = (H) this;
    }

    @Override
    protected Boolean read(PartialResponse<Boolean> partial, InputStream input)
    throws Exception {
        try {
            final JsonParser parser = mapper.getFactory().createParser(input);
            final MappingIterator<? extends Chunk<T, H>> iterator = mapper.readValues(parser, chunkType);
            while (iterator.hasNextValue()) {
                final Chunk<T, H> chunk = iterator.next();
                if (chunk.putAll(partial, thisInstance)) continue;
                else return false;
            }
            return puttable.close();
        } catch (Throwable throwable) {
            puttable.fail(throwable);
            if (throwable instanceof Exception) throw (Exception) throwable;
            throw new ExecutionException(throwable);
        }
    }

    public boolean put(T instance) {
        return puttable.put(instance);
    }

    public boolean fail(Throwable throwable) {
        return puttable.fail(throwable);
    }

    public static abstract class Chunk<T, H extends ChunkedContentHandler<T, H>> {

        public abstract boolean putAll(PartialResponse<Boolean> partial, H handler);

    }
}
