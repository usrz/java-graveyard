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

import java.util.HashSet;
import java.util.Set;

public class SiblingsException extends IllegalStateException {

    private final Set<Sibling> siblings;
    private final Key key;

    public SiblingsException(Key key, Set<String> siblings) {
        super(siblings.size() + " siblings detected in " + key.getLocation());
        this.siblings = new HashSet<>(siblings.size());
        for (String sibling: siblings) this.siblings.add(new Sibling(key, sibling));
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public Set<Sibling> getSiblings() {
        return siblings;
    }
}
