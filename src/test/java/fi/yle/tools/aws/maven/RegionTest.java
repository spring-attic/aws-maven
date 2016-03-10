/*
 * Copyright 2010-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.yle.tools.aws.maven;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class RegionTest {

    @Test
    public void test() {
        assertEndpoint("US", "s3.amazonaws.com");
        assertEndpoint("us-west-2", "s3-us-west-2.amazonaws.com");
        assertEndpoint("us-west-1", "s3-us-west-1.amazonaws.com");
        assertEndpoint("EU", "s3-eu-west-1.amazonaws.com");
        assertEndpoint("ap-southeast-1", "s3-ap-southeast-1.amazonaws.com");
        assertEndpoint("ap-southeast-2", "s3-ap-southeast-2.amazonaws.com");
        assertEndpoint("ap-northeast-1", "s3-ap-northeast-1.amazonaws.com");
        assertEndpoint("sa-east-1", "s3-sa-east-1.amazonaws.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLocationConstraint() {
        Region.fromLocationConstraint("foo");
    }

    private void assertEndpoint(String locationConstraint, String endpoint) {
        assertEquals(endpoint, Region.fromLocationConstraint(locationConstraint).getEndpoint());
    }

}
