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

package org.springframework.build.aws.maven;

import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;

interface TransferListenerSupport {

    /**
     * Add a {@link TransferListener} to be notified
     * 
     * @param transferListener The {@link TransferListener} to be notified
     */
    void addTransferListener(TransferListener transferListener);

    /**
     * Remove a {@link TransferListener} so that it is no longer notified
     * 
     * @param transferListener The {@link TransferListener} that should no longer be notified
     */
    void removeTransferListener(TransferListener transferListener);

    /**
     * Returns whether a {@link TransferListener} is already in the collection of {@link TransferListener}s to be
     * notified
     * 
     * @param transferListener the {@link TransferListener} to look for
     * @return {@code true} if the {@link TransferListener} is already in the collection of {@link TransferListener}s to
     *         be notified, otherwise {@code false}
     */
    boolean hasTransferListener(TransferListener transferListener);

    /**
     * Notify {@link TransferListener}s that a transfer is being initiated
     * 
     * @param resource The resource being transfered
     * @param requestType The type of request to be executed
     * 
     * @see org.apache.maven.wagon.events.TransferEvent#TRANSFER_INITIATED
     */
    void fireTransferInitiated(Resource resource, int requestType);

    /**
     * Notify {@link TransferListener}s that a transfer has started successfully
     * 
     * @param resource The resource being transfered
     * @param requestType The type of request being executed
     * 
     * @see org.apache.maven.wagon.events.TransferEvent#TRANSFER_STARTED
     */
    void fireTransferStarted(Resource resource, int requestType);

    /**
     * Notify {@link TransferListener}s about the progress of a transfer
     * 
     * @param resource The resource being transfered
     * @param requestType The type of request being executed
     * @param buffer The buffer of bytes being transfered
     * @param length The length of the data in the buffer
     * 
     * @see org.apache.maven.wagon.events.TransferEvent#TRANSFER_PROGRESS
     */
    void fireTransferProgress(Resource resource, int requestType, byte[] buffer, int length);

    /**
     * Notify {@link TransferListener}s that the transfer was completed successfully
     * 
     * @param resource The resource being transfered
     * @param requestType The type of request executed
     * 
     * @see org.apache.maven.wagon.events.TransferEvent#TRANSFER_COMPLETED
     */
    void fireTransferCompleted(Resource resource, int requestType);

    /**
     * Notify {@link TransferListener}s that an error occurred during the transfer
     * 
     * @param resource The resource being transfered
     * @param requestType The type of the request being executed
     * @param exception The error that occurred
     */
    void fireTransferError(Resource resource, int requestType, Exception exception);
}
