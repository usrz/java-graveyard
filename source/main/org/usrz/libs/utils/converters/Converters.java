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
package org.usrz.libs.utils.converters;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Converters {

    private final Map<ConverterKey<?, ?>, Converter<?, ?>> converters = new ConcurrentHashMap<>();

    public Converters() {
        register(new HashSetConverter());
        // TODO Auto-generated constructor stub
    }

    public void register(Converter<?, ?> converter) {
        final List<Class<?>> arguments = TypeFinder.getTypeArguments(Converter.class, converter.getClass());
        final Class<?> in = arguments.get(0);
        final Class<?> out = arguments.get(1);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ConverterKey<?, ?> key = new ConverterKey(in, out);

        converters.put(key, converter);
    }

    public Converter<?, ?> getConverter(ConvertibleType in, ConvertibleType out) {
        final List<ConvertibleType> inTypes = in.getArguments();
        final List<ConvertibleType> outTypes = out.getArguments();

        final Converter<?, ?> rawConverter = this.getConverter(in.getRawType(), out.getRawType());
        if (rawConverter == null) {
            throw new IllegalStateException("Unable to convert raw type from " + in + " to " + out);
        }

        if (inTypes.size() != outTypes.size()) return null;
        final int count = inTypes.size();

        final Converter<?, ?>[] argumentsConverters = new Converter<?, ?>[count];
        for (int x = 0; x < count; x ++) {
            argumentsConverters[x] = getConverter(inTypes.get(x), outTypes.get(x));
            if (argumentsConverters[x] == null) {
                // DO SOMETHING
            }

        }

    }

    @SuppressWarnings("unchecked")
    public <IN, OUT> Converter<IN, OUT> getConverter(Class<?> xin, Class<?> xout) {
        final Class<?> in = alias(xin);
        final Class<?> out = alias(xout);

        if (out.isAssignableFrom(in)) return (Converter<IN, OUT>) IdentityConverter.instance();

        for (Entry<ConverterKey<?, ?>, Converter<?, ?>> entry: converters.entrySet()) {
            final Class<?> cin = entry.getKey().in;
            final Class<?> cout = entry.getKey().out;
            final Converter<?, ?> converter = entry.getValue();

            if ((cin.isAssignableFrom(in)) &&
                (out.isAssignableFrom(cout))) {
                return (Converter<IN, OUT>) converter;
            }
        }

        return null;
    }

    private Class<?> alias(Class<?> type) {
        return type == boolean.class ? Boolean.class   :
               type == byte.class    ? Byte.class      :
               type == short.class   ? Short.class     :
               type == int.class     ? Integer.class   :
               type == long.class    ? Long.class      :
               type == float.class   ? Float.class     :
               type == double.class  ? Double.class    :
               type == char.class    ? Character.class :
               type == void.class    ? Void.class      :
               type;
    }

    /* ====================================================================== */

    private static final class ConverterKey<IN, OUT> {

        private final Class<IN> in;
        private final Class<OUT> out;

        private ConverterKey(Class<IN> in, Class<OUT> out) {
            if (in == null) throw new NullPointerException("Null \"in\" class");
            if (out == null) throw new NullPointerException("Null \"out\" class");
            this.in = in;
            this.out = out;
        }

        @Override
        public int hashCode() {
            return in.hashCode() ^ out.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) return true;
            if (object == null) return false;
            try {
                final ConverterKey<?, ?> key = (ConverterKey<?, ?>) object;
                return this.in.equals(key.in) && this.out.equals(key.out);
            } catch (ClassCastException exception) {
                return false;
            }
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[" + in.getName() + "->" + out.getName() + "]";
        }

    }
}
