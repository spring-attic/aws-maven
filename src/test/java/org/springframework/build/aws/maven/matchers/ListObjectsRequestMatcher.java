/*
 * Copyright 2011 the original author or authors.
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

package org.springframework.build.aws.maven.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.amazonaws.services.s3.model.ListObjectsRequest;

final class ListObjectsRequestMatcher extends BaseMatcher<ListObjectsRequest> {

    private final ListObjectsRequest listObjectsRequest;

    ListObjectsRequestMatcher(ListObjectsRequest listObjectsRequest) {
        this.listObjectsRequest = listObjectsRequest;
    }

    // CHECKSTYLE:OFF

    @Override
    public boolean matches(Object obj) {
        if (this.listObjectsRequest == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (ListObjectsRequest.class != obj.getClass()) {
            return false;
        }
        ListObjectsRequest other = (ListObjectsRequest) obj;
        if (this.listObjectsRequest.getBucketName() == null) {
            if (other.getBucketName() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getBucketName().equals(other.getBucketName())) {
            return false;
        }
        if (this.listObjectsRequest.getPrefix() == null) {
            if (other.getPrefix() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getPrefix().equals(other.getPrefix())) {
            return false;
        }
        if (this.listObjectsRequest.getDelimiter() == null) {
            if (other.getDelimiter() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getDelimiter().equals(other.getDelimiter())) {
            return false;
        }
        if (this.listObjectsRequest.getMarker() == null) {
            if (other.getMarker() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getMarker().equals(other.getMarker())) {
            return false;
        }
        if (this.listObjectsRequest.getMaxKeys() == null) {
            if (other.getMaxKeys() != null) {
                return false;
            }
        } else if (!this.listObjectsRequest.getMaxKeys().equals(other.getMaxKeys())) {
            return false;
        }
        return true;
    }

    // CHECKSTYLE:ON

    @Override
    public void describeTo(Description description) {
        description.appendValue(this.listObjectsRequest);
    }

}