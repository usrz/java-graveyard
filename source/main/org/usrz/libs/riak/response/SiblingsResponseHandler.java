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
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.ResponseHandler;
import org.usrz.libs.riak.SiblingsException;

public class SiblingsResponseHandler<T> extends ResponseHandler<T> {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final Key key;

    public SiblingsResponseHandler(Key key) {
        if (key == null) throw new NullPointerException("Null key");
        this.key = key;
    }

    @Override
    protected T call(InputStream input) throws Exception {
        final Scanner scanner = new Scanner(input, "UTF8");
        final Set<String> siblings = new HashSet<String>();
        try {
            scanner.useDelimiter(WHITESPACE);
            while (scanner.hasNext()) {
                final String sibling = scanner.next();
                if ("Siblings:".equalsIgnoreCase(sibling)) continue;
                siblings.add(sibling);
            }
            throw new SiblingsException(key, siblings);
        } finally {
            scanner.close();
        }
    }

}
