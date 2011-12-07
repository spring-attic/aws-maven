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

import com.amazonaws.services.s3.model.PutObjectRequest;

final class PutObjectRequestMatcher extends BaseMatcher<PutObjectRequest> {

    private final PutObjectRequest putObjectRequest;

    PutObjectRequestMatcher(PutObjectRequest putObjectRequest) {
        this.putObjectRequest = putObjectRequest;
    }

    // CHECKSTYLE:OFF

    @Override
    public boolean matches(Object obj) {
        if (this.putObjectRequest == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (PutObjectRequest.class != obj.getClass()) {
            return false;
        }
        PutObjectRequest other = (PutObjectRequest) obj;
        if (this.putObjectRequest.getBucketName() == null) {
            if (other.getBucketName() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getBucketName().equals(other.getBucketName())) {
            return false;
        }
        if (this.putObjectRequest.getKey() == null) {
            if (other.getKey() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getKey().equals(other.getKey())) {
            return false;
        }
        if (this.putObjectRequest.getFile() == null) {
            if (other.getFile() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getFile().equals(other.getFile())) {
            return false;
        }
        if (this.putObjectRequest.getInputStream() == null) {
            if (other.getInputStream() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getInputStream().equals(other.getInputStream())) {
            return false;
        }
        if (this.putObjectRequest.getMetadata() == null) {
            if (other.getMetadata() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getMetadata().equals(other.getMetadata())) {
            return false;
        }
        if (this.putObjectRequest.getCannedAcl() == null) {
            if (other.getCannedAcl() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getCannedAcl().equals(other.getCannedAcl())) {
            return false;
        }
        if (this.putObjectRequest.getStorageClass() == null) {
            if (other.getStorageClass() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getStorageClass().equals(other.getStorageClass())) {
            return false;
        }
        if (this.putObjectRequest.getProgressListener() == null) {
            if (other.getProgressListener() != null) {
                return false;
            }
        } else if (!this.putObjectRequest.getProgressListener().equals(other.getProgressListener())) {
            return false;
        }
        return true;
    }

    // CHECKSTYLE:ON

    @Override
    public void describeTo(Description description) {
        description.appendValue(this.putObjectRequest);
    }

}