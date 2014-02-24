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
package org.usrz.libs.riak.utils;

import java.nio.charset.Charset;

public final class RiakUtils {

    private static final boolean[] VALID_CHARACTERS = new boolean[128];
    private static final Charset UTF8 = Charset.forName("UTF8");
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    static {
        for (int x = 0; x < VALID_CHARACTERS.length; x ++) VALID_CHARACTERS[x] = false;
        // RFC2616 allowed characters: "!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ^_`abcdefghijklmnopqrstuvwxyz|~"
        for (char c: "!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ^_`abcdefghijklmnopqrstuvwxyz|~".toCharArray()) {
            VALID_CHARACTERS[c] = true;
        }
    }

    private RiakUtils() {
        throw new IllegalStateException("Do not construct");
    }

    public static final String validate(String token) {
        for (char c: token.toCharArray()) {
            if ((c < VALID_CHARACTERS.length) && VALID_CHARACTERS[c]) continue;
            throw new IllegalArgumentException("Invalid token: " + token);
        }
        return token;
    }

    public static final String encode(String key) {
        return encode(key, false);
    }

    public static final String encode(String key, boolean isNullOk) {
        if (key == null) {
            if (isNullOk) return null;
            throw new NullPointerException("Null key");
        }

        /*
         * As per java.net.URLEncoder:
         *
         * - The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
         * - The special characters ".", "-", "*", and "_" remain the same.
         * - The space character " " is converted into a plus sign "+".
         */

        final StringBuilder encoded = new StringBuilder(key.length());
        for (byte b: key.getBytes(UTF8)) {
            if ((b == '.') || (b == '-') || (b == '*') || (b == '_') ||
                ((b >= 'a') && (b <= 'z')) ||
                ((b >= 'A') && (b <= 'Z')) ||
                ((b >= '0') && (b <= '9'))) {
                encoded.append((char) b);
            } else if (b == ' ') {
                encoded.append('+');
            } else {
                encoded.append('%').append(HEX[(b >> 4) & 0x0F]).append(HEX[b & 0x0F]);
            }
        }

        return encoded.toString();
    }

    public static final String encode(Iterable<String> keys) {
        final StringBuilder builder = new StringBuilder();
        for (String key: keys) builder.append(", ").append(encode(key));
        return builder.length() > 2 ? builder.toString() : "";
    }


    public static final String decode(String key) {
        return decode(key, false);
    }

    public static final String decode(String key, boolean isNullOk) {
        if (key == null) {
            if (isNullOk) return null;
            throw new NullPointerException("Null key");
        }

        /* Decode "%XX" sequences, starting from UTF8 */
        final byte[] encoded = key.getBytes(UTF8);
        final byte[] decoded = new byte[encoded.length];
        int y = 0;
        try {
            for (int x = 0; x < encoded.length; x ++) {
                final byte b = encoded[x];
                if (b == '+') { /* Plus-to-space */
                    decoded[y ++] = ' ';
                } else if (b != '%') { /* Simple copy-byte operation */
                    decoded[y ++] = b;
                } else { /* Decode a %XX sequence */
                    int value = 0;
                    switch (encoded[++ x]) {
                        case '0': break;
                        case '1': value |= 0x10; break;
                        case '2': value |= 0x20; break;
                        case '3': value |= 0x30; break;
                        case '4': value |= 0x40; break;
                        case '5': value |= 0x50; break;
                        case '6': value |= 0x60; break;
                        case '7': value |= 0x70; break;
                        case '8': value |= 0x80; break;
                        case '9': value |= 0x90; break;
                        case 'A': case 'a': value |= 0xA0; break;
                        case 'B': case 'b': value |= 0xB0; break;
                        case 'C': case 'c': value |= 0xC0; break;
                        case 'D': case 'd': value |= 0xD0; break;
                        case 'E': case 'e': value |= 0xE0; break;
                        case 'F': case 'f': value |= 0xF0; break;
                        default: throw new IllegalArgumentException("Invalid %XX sequence in \"" + key + "\"");
                    }
                    switch (encoded[++ x]) {
                        case '0': break;
                        case '1': value |= 0x01; break;
                        case '2': value |= 0x02; break;
                        case '3': value |= 0x03; break;
                        case '4': value |= 0x04; break;
                        case '5': value |= 0x05; break;
                        case '6': value |= 0x06; break;
                        case '7': value |= 0x07; break;
                        case '8': value |= 0x08; break;
                        case '9': value |= 0x09; break;
                        case 'A': case 'a': value |= 0x0A; break;
                        case 'B': case 'b': value |= 0x0B; break;
                        case 'C': case 'c': value |= 0x0C; break;
                        case 'D': case 'd': value |= 0x0D; break;
                        case 'E': case 'e': value |= 0x0E; break;
                        case 'F': case 'f': value |= 0x0F; break;
                        default: throw new IllegalArgumentException("Invalid %XX sequence in \"" + key + "\"");
                    }

                    decoded[y++] = (byte) value;
                }
            }
        } catch (IndexOutOfBoundsException exception) {
            throw new IllegalArgumentException("Unterminated %XX sequence in \"" + key + "\"");
        }

        /* All done */
        return new String(decoded, 0, y, UTF8);
    }
}
