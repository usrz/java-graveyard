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


public class Sibling extends Key {

    private final String vtag;

    public Sibling(Key key, String vtag) {
        super(key);
        if (vtag == null) throw new NullPointerException("Null vtag");
        this.vtag = vtag;
    }

    /* ====================================================================== */

    public final String getVtag() {
        return vtag;
    }

    /* ====================================================================== */

    @Override
    public String getLocation() {
        return super.getLocation() + "?vtag=" + vtag;
    }

    /* ====================================================================== */

    @Override
    public boolean equals(Object object) {
        if (super.equals(object)) try {
            final Sibling sibling = (Sibling) object;
            return vtag.equals(sibling.vtag);
        } catch (ClassCastException exception) {
            /* Ignore and return false */
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ vtag.hashCode();
    }

}
