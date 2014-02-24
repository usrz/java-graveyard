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

import org.usrz.libs.riak.requests.BasicQuorumRequest;
import org.usrz.libs.riak.requests.BucketedRequest;
import org.usrz.libs.riak.requests.ConditionalRequest;
import org.usrz.libs.riak.requests.KeyedRequest;
import org.usrz.libs.riak.requests.ReadQuorumRequest;
import org.usrz.libs.riak.requests.SiblingsRequest;

public interface FetchRequest<T>
extends Request<T>,
        KeyedRequest<T, FetchRequest<T>>,
        BucketedRequest<T, FetchRequest<T>>,
        SiblingsRequest<T, FetchRequest<T>>,
        ReadQuorumRequest<T, FetchRequest<T>>,
        BasicQuorumRequest<T, FetchRequest<T>>,
        ConditionalRequest<T, FetchRequest<T>> {

}
