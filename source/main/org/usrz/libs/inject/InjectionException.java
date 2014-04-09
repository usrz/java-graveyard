/* ========================================================================== *
In * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
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
package org.usrz.libs.inject;

/**
 * An (internal) exception thrown whenever an error was produced injecting
 * an object.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class InjectionException extends RuntimeException {

    public InjectionException(String message, Key<?> key) {
        super(prepareMessage(message, key));
    }

    public InjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    private static String prepareMessage(String message, Key<?> key) {
        final StringBuilder builder = new StringBuilder(message);
        if (key == null) return builder.toString();
        return builder.append("\n  Key: " + key).toString();
    }
}
