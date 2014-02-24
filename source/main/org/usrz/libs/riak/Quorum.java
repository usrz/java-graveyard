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
package org.usrz.libs.riak;

public enum Quorum {

    ALL("all"), QUORUM("quorum"), ONE("one");

    private final String parameter;

    private Quorum(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }

    public static String getParameter(Object object) {
        if (object == null) throw new NullPointerException("Null quorum parameter");
        if (object instanceof Quorum) return ((Quorum) object).getParameter();
        if (object instanceof Number) return Integer.toString(((Number) object).intValue());
        if (object instanceof String) try {
            final String string = ((String) object).trim().toUpperCase();
            try {
                return Quorum.valueOf(string).getParameter();
            } catch (IllegalArgumentException exception) {
                /* Not a valid Quorum keyword, try with numbers */
                return Integer.toString(Integer.parseInt(string));
            }
        } catch (NumberFormatException exception) {
            /* Not a valid quorum number, nothing we can do */
        }

        /* Not a known type, or wrong string format */
        throw new IllegalArgumentException("Invalid quorum parameter \"" + object.toString() + "\"");
    }
}
