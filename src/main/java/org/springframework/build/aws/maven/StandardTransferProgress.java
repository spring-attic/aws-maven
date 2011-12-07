/*
 * Copyright 2010-2011 the original author or authors.
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

package org.springframework.build.aws.maven;

import org.apache.maven.wagon.resource.Resource;

final class StandardTransferProgress implements TransferProgress {

    private final Resource resource;

    private final int requestType;

    private final TransferListenerSupport transferListenerSupport;

    StandardTransferProgress(Resource resource, int requestType, TransferListenerSupport transferListenerSupport) {
        this.resource = resource;
        this.requestType = requestType;
        this.transferListenerSupport = transferListenerSupport;
    }

    @Override
    public void notify(byte[] buffer, int length) {
        this.transferListenerSupport.fireTransferProgress(this.resource, this.requestType, buffer, length);
    }

}
