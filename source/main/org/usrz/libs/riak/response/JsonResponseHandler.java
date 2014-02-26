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

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonResponseHandler<T> extends ResponseHandler<T> {

    private final ObjectMapper mapper;
    private final Class<T> type;

    public JsonResponseHandler(ObjectMapper mapper, Class<T> type) {
        if (mapper == null) throw new NullPointerException("Null object mapper");
        if (type == null) throw new NullPointerException("Null type");
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    protected T call(InputStream input)
    throws Exception {
        try {
            return mapper.readValue(input, type);
        } finally {
            input.close();
        }
    }

}
