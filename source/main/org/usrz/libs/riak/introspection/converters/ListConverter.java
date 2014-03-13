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
package org.usrz.libs.riak.introspection.converters;

import java.util.ArrayList;
import java.util.List;

import org.usrz.libs.riak.RiakClient;

import com.fasterxml.jackson.databind.util.Converter;

public class ListConverter<IN , OUT> extends RiakConverter<Iterable<IN>, List<OUT>> {

    private final Converter<IN, OUT> converter;

    public ListConverter(RiakClient client, Converter<IN, OUT> converter) {
        super(client);
        this.converter = converter;
    }

    @Override
    public List<OUT> convert(Iterable<IN> values) {
        final List<OUT> set = new ArrayList<>();
        for (IN value: values) set.add(converter.convert(value));
        return set;
    }

}
