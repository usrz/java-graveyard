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
package org.usrz.libs.riak.annotations;


import static org.usrz.libs.riak.IndexType.BINARY;
import static org.usrz.libs.riak.IndexType.INTEGER;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;
import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.FakeClient;
import org.usrz.libs.riak.IndexMap;
import org.usrz.libs.riak.IndexMapBuilder;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.LinksMap;
import org.usrz.libs.riak.LinksMapBuilder;
import org.usrz.libs.riak.Metadata;
import org.usrz.libs.riak.MetadataBuilder;
import org.usrz.libs.riak.RiakClient;
import org.usrz.libs.testing.AbstractTest;

public class RiakIntrospectorTest extends AbstractTest {

    private final RiakClient client = new FakeClient();
    private final RiakIntrospector introspector = new RiakIntrospector(client);

    /* ====================================================================== */

    @Test
    public void testEmptyObject() {
        final Object object = new Object();
        assertNull(introspector.getKeyName(object));
        assertNull(introspector.getBucket(object));
        assertTrue(introspector.getIndexMap(object).isEmpty());
        assertTrue(introspector.getLinksMap(object).isEmpty());
        assertTrue(introspector.getMetadata(object).isEmpty());
    }

    /* ====================================================================== */

    @Test
    public void testReadIntrospectableObject() {

        final IntrospectableObject object = new IntrospectableObject();

        assertEquals(introspector.getKeyName(object), "myKey");
        assertEquals(introspector.getBucketName(object), "myBucket");
        assertEquals(introspector.getKey(object), new Key(client, "myBucket", "myKey"));

        final IndexMap index = introspector.getIndexMap(object);
        assertEquals(index.size(), 4);

        assertTrue(index.get("autostringindex", BINARY).contains("myAutoString"));
        assertTrue(index.get("autonumberindex", INTEGER).contains("123"));
        assertTrue(index.get("multipleValues", BINARY).contains("hello"));
        assertTrue(index.get("multipleValues", BINARY).contains("world"));
        assertTrue(index.get("multipleValues", BINARY).contains("foo"));
        assertTrue(index.get("multipleValues", BINARY).contains("bar"));
        assertTrue(index.get("multipleValues", BINARY).contains("1"));
        assertTrue(index.get("multipleValues", BINARY).contains("2"));
        assertTrue(index.get("multipleValues", BINARY).contains("3"));
        assertTrue(index.get("multipleValues", BINARY).contains("4"));
        assertTrue(index.get("multipleValues", INTEGER).contains("100"));
        assertTrue(index.get("multipleValues", INTEGER).contains("200"));
        assertTrue(index.get("multipleValues", INTEGER).contains("300"));
        assertTrue(index.get("multipleValues", INTEGER).contains("400"));

        final Metadata metadata = introspector.getMetadata(object);
        assertEquals(metadata.size(), 3);

        assertTrue(metadata.get("myMetadata").contains("metadataValue1"));
        assertTrue(metadata.get("anotherField").contains("metadataValue2"));
        assertTrue(metadata.get("multipleValues").contains("hello"));
        assertTrue(metadata.get("multipleValues").contains("world"));
        assertTrue(metadata.get("multipleValues").contains("foo"));
        assertTrue(metadata.get("multipleValues").contains("bar"));
        assertTrue(metadata.get("multipleValues").contains("1"));
        assertTrue(metadata.get("multipleValues").contains("2"));
        assertTrue(metadata.get("multipleValues").contains("3"));
        assertTrue(metadata.get("multipleValues").contains("4"));
        assertTrue(metadata.get("multipleValues").contains("100"));
        assertTrue(metadata.get("multipleValues").contains("200"));
        assertTrue(metadata.get("multipleValues").contains("300"));
        assertTrue(metadata.get("multipleValues").contains("400"));

        final LinksMap links = introspector.getLinksMap(object);
        assertEquals(links.size(), 6);

        assertTrue(links.containsValue("keylink",             new Key(client, "myLinkedBucket1", "myLinkedKey1")));
        assertTrue(links.containsValue("keyoverridden",       new Key(client, "myLinkedBucket2", "myLinkedKey2")));
        assertTrue(links.containsValue("newstringlink",       new Key(client, "myLinkedBucket3", "myLinkedKey3")));
        assertTrue(links.containsValue("oldstringlink",       new Key(client, "myLinkedBucket4", "myLinkedKey4")));
        assertTrue(links.containsValue("introspectedlink",    new Key(client, "myBucket",        "myKey")));
        assertTrue(links.containsValue("multiple",            new Key(client, "b1",              "k1")));
        assertTrue(links.containsValue("multiple",            new Key(client, "b2",              "k2")));
        assertTrue(links.containsValue("multiple",            new Key(client, "b3",              "k3")));
        assertTrue(links.containsValue("multiple",            new Key(client, "b4",              "k4")));
        assertTrue(links.containsValue("multiple",            new Key(client, "b5",              "k5")));
        assertTrue(links.containsValue("multiple",            new Key(client, "b6",              "k6")));


    }

    private class IntrospectableObject {

        @RiakKey
        private final String key = "myKey";

        @RiakBucket
        private final String bucket = "myBucket";

        /* ================================================================== */

        @RiakIndex
        private final long getAutoNumberIndex() { return 123; }

        @RiakIndex
        private final String getAutoStringIndex() { return "myAutoString"; }

        /* Index as a collection of auto-detected values */
        @RiakIndex("multipleValues") @RiakMetadata("multipleValues")
        private final Collection<?> nonStandard1() { return Arrays.asList(new Object[]{ "hello", 200, "world"}); }

        /* Index as an array of auto-detected values */
        @RiakIndex("multipleValues") @RiakMetadata("multipleValues")
        private final Object[] nonStandard2() { return new Object[]{ "foo", 100, "bar" }; }

        /* Force integer for strings */
        @RiakMetadata("multipleValues")
        @RiakIndex(name="multipleValues", type=RiakIndex.Type.INTEGER)
        private final String[] nonStandard3() { return new String[]{ "300", "400" }; }

        /* Force binary for integers */
        @RiakMetadata("multipleValues")
        @RiakIndex(name="multipleValues", type=RiakIndex.Type.BINARY)
        private final int[] nonStandard4() { return new int[]{ 1, 2, 3 , 4 }; }

        @RiakMetadata
        private final String getMyMetadata() { return "metadataValue1"; };

        @RiakMetadata("anotherField")
        private final String getThisIsHidden() { return "metadataValue2"; };

        @RiakLink
        private final Key getKeyLink() { return new Key(client, "myLinkedBucket1", "myLinkedKey1"); }

        @RiakLink("keyOverridden")
        private final Key getKeyLink2() { return new Key(client, "myLinkedBucket2", "myLinkedKey2"); }

        @RiakLink
        private final String getNewStringLink() { return "/buckets/myLinkedBucket3/keys/myLinkedKey3"; }

        @RiakLink
        private final String getOldStringLink() { return "/riak/myLinkedBucket4/myLinkedKey4"; }

        @RiakLink
        private final Object getIntrospectedLink() { return this; }

        @RiakLink("multiple")
        private final Collection<?> multiple1() { return Arrays.asList(new Object[]{ new Key(client, "b1", "k1"), "/buckets/b2/keys/k2/" }); }

        @RiakLink("multiple")
        private final Object[] multiple2() { return new Object[]{ "/riak/b3/k3/", "/riak/b4/keys/k4" }; }

        @RiakLink("multiple")
        private final Object[] multiple3() { return new Object[]{
                                                 "/riak/b5/keys/k5",
                                                 new Object() {
                                                     @RiakKey private final String key = "k6";
                                                     @RiakBucket private final String bucket = "b6";
                                                 }
                                             };
                                         }
    }

    /* ====================================================================== */

    private final Key key = new Key(client, "myBucket", "myKey");
    private final LinksMap linksMap = new LinksMapBuilder(client).add("linkTag1", new Key(client, "linkBucket1", "linkKey1"))
                                                                 .add("linkTag2", new Key(client, "linkBucket2", "linkKey2"))
                                                                 .build();
    private final IndexMap indexMap = new IndexMapBuilder().add("binary_idx", BINARY, "value")
                                                           .add("integer_idx", INTEGER, "-12")
                                                           .build();
    private final Metadata metadata = new MetadataBuilder().add("field1", "value1")
                                                           .add("field2", "value2")
                                                           .build();

    @Test
    public void testKey() {
        final KeyObject object = new KeyObject();
        assertNull(object.key);
        introspector.setKey(object, key);
        assertSame(object.key, key);
        final Key gotten = introspector.getKey(object);
        assertSame(gotten, key);
    }

    private class KeyObject {
        @RiakKey private Key key;
    }

    /* ====================================================================== */

    @Test
    public void testEmptyInstrumentation() {
        /* Thou shall not explode! */
        final Object object = new Object();
        introspector.setKey(object, key);
        introspector.setIndexMap(object, indexMap);
        introspector.setLinksMap(object, linksMap);
        introspector.setMetadata(object, metadata);
    }

    @Test
    public void testSimpleInstrumentationKB() {
        final SimpleInstrumentableKB object = new SimpleInstrumentableKB();
        introspector.setKey(object, key);
        assertEquals(object.k, key.getName());
        assertEquals(object.b.getName(), key.getBucketName());
    }

    private class SimpleInstrumentableKB {
        private String k; @RiakKey    public void setK(String k) { this.k = k; };
        private Bucket b; @RiakBucket public void setB(Bucket b) { this.b = b; };
    }

    @Test
    public void testSimpleInstrumentationKBS() {
        final SimpleInstrumentableKBS object = new SimpleInstrumentableKBS();
        introspector.setKey(object, key);
        assertEquals(object.k, key.getName());
        assertEquals(object.b, key.getBucketName());
    }

    private class SimpleInstrumentableKBS {
        private String k; @RiakKey    public void setK(String k) { this.k = k; };
        private String b; @RiakBucket public void setB(String b) { this.b = b; };
    }

    @Test
    public void testSimpleInstrumentationK() {
        final SimpleInstrumentableK object = new SimpleInstrumentableK();
        introspector.setKey(object, key);
        assertEquals(object.k, key);
        assertEquals(object.b, key.getBucket());
    }

    private class SimpleInstrumentableK {
        private Key    k; @RiakKey    public void setK(Key    k) { this.k = k; };
        private Bucket b; @RiakBucket public void setB(Bucket b) { this.b = b; };
    }
}
