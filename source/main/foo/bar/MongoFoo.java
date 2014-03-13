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

import java.io.IOException;

import org.bson.types.ObjectId;
import org.testng.annotations.Test;
import org.usrz.libs.mongodb.mapper.Generator;
import org.usrz.libs.testing.AbstractTest;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MongoFoo extends AbstractTest {

    @Test
    public void testMapping()
    throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.reg
        //mapper.registerModule(new MongoModule());
        final Bean bean = new Bean();
        mapper.writeValue(new Generator(mapper), bean);
        //mapper.readValue(new Parser(), Bean.class);
    }

    @JsonIdentityInfo(generator=PropertyGenerator.class,property="@id")
    public class Bean {

        private final ObjectId id = new ObjectId();
        private String foo;

        public void setFoo(String foo) {
            this.foo = foo;
        }

        @JsonProperty("@id")// @JsonIgnore
        public ObjectId getIdentifier() {
            return id;
        }

        public String getFoo() {
            return foo;
        }

//        @JsonIdentityReference(alwaysAsId=false)
//        public Bean2 getBean2() {
//            return new Bean2();
//        }
    }

    @JsonIdentityInfo(generator=MongoObjectIdGenerator.class,property="_id")
    public class Bean2 {

        private final ObjectId id = new ObjectId();

        @JsonProperty("_id")// @JsonIgnore
        public ObjectId getIdentifier() {
            return id;
        }

        public String getField() {
            return "gogogogog";
        }
    }

    public void testFoo()
    throws IOException {
        MongoClient client = new MongoClient( "localhost" );
        DB db = client.getDB("test");
        DBCollection collection = db.getCollection("foo");

        final BasicDBObject object = new BasicDBObject().append("foo", new String[] { "bar", "baz" });

        final WriteResult result = collection.insert(object);
        final CommandResult command = result.getLastError();
        System.err.println(result);
        System.err.println(command);

        System.err.println("CLASS => " + result.getClass());
        System.err.println("  _id => " + result.getField("_id"));
        System.err.println("o._id => " + object.get("_id"));
        System.err.println("o._id => " + object.get("_id").getClass());

        BasicDBObject query = new BasicDBObject("_id", object.get("_id"));
        DBObject fetched = collection.findOne(query);
        System.err.println(fetched);

        System.err.println(fetched.get("foo"));
        System.err.println(fetched.get("foo").getClass());

    }

}
