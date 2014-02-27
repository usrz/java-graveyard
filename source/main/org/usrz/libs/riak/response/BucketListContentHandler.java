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

import java.util.List;

import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.PartialResponse;
import org.usrz.libs.riak.utils.Puttable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BucketListContentHandler extends ChunkedContentHandler<Bucket, BucketListContentHandler> {

    public BucketListContentHandler(ObjectMapper mapper, Puttable<Bucket> puttable) {
        super(mapper, puttable, BucketListChunk.class);
    }

    public static final class BucketListChunk extends Chunk<Bucket, BucketListContentHandler> {

        private final List<String> names;

        public BucketListChunk(@JsonProperty("buckets") List<String> names) {
            this.names = names;
        }

        @Override
        public boolean putAll(PartialResponse<Boolean> partial, BucketListContentHandler handler) {
            for (String name: names) {
                if (handler.put(partial.getRiakClient().getBucket(name))) continue;
                return false;
            }
            return true;
        }
    }
}
