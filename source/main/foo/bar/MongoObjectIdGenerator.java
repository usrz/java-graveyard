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
package foo.bar;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

public class MongoObjectIdGenerator extends ObjectIdGenerator<ObjectId> {

    private final Class<?> scope;

    public MongoObjectIdGenerator() {
        scope = null;
    }

    public MongoObjectIdGenerator(Class<?> scope) {
        this.scope = scope;
    }

    @Override
    public Class<?> getScope() {
        return scope;
    }

    @Override
    public boolean canUseFor(ObjectIdGenerator<?> generator) {
        System.err.println("CAN USE FOR " + generator);
        if (generator == this) return true;
        if (generator.getClass().equals(this.getClass())) {
            return scope == ((MongoObjectIdGenerator) generator).scope;
        }
        return false;
    }

    @Override
    public ObjectIdGenerator<ObjectId> forScope(Class<?> scope) {
        System.err.println("SCOPE IS " + scope.getName());
        if (scope == this.scope) return this;
        return new MongoObjectIdGenerator(scope);
    }

    @Override
    public ObjectIdGenerator<ObjectId> newForSerialization(Object context) {
        return this;
    }

    @Override
    public ObjectIdGenerator.IdKey key(Object key) {
        return new IdKey(getClass(), scope, key);
    }

    @Override
    public ObjectId generateId(Object forPojo) {
        new Exception("FOO").printStackTrace();
        // TODO Auto-generated method stub
        return null;
    }

}
