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

import java.io.IOException;

import org.usrz.libs.riak.PartialResponse;

public class SuccessContentHandler extends NullContentHandler<Boolean> {

    public SuccessContentHandler() {
        /* Nothing to do */
    }

    @Override
    protected Boolean call(PartialResponse<Boolean> partial)
    throws IOException {
        System.err.println("CALLED WITH " + partial.isSuccessful());
        return partial.isSuccessful();
    }

}
