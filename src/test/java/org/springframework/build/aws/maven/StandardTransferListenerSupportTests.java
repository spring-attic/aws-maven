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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.build.aws.maven.matchers.Matchers.eq;

import java.io.IOException;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public final class StandardTransferListenerSupportTests {

    private static final int REQUEST_TYPE = TransferEvent.REQUEST_GET;

    private final Wagon wagon = mock(Wagon.class);

    private final Resource resource = mock(Resource.class);

    private final TransferListener transferListener = mock(TransferListener.class);

    private final TransferListenerSupport transferListenerSupport = new StandardTransferListenerSupport(this.wagon);

    @Before
    public void addTransferListener() {
        this.transferListenerSupport.addTransferListener(this.transferListener);
    }

    @Test
    public void transferListenerManagement() {
        assertTrue(this.transferListenerSupport.hasTransferListener(this.transferListener));
        this.transferListenerSupport.removeTransferListener(this.transferListener);
        assertFalse(this.transferListenerSupport.hasTransferListener(this.transferListener));
        this.transferListenerSupport.addTransferListener(null);
    }

    @Test
    public void fireTransferInitiated() {
        this.transferListenerSupport.fireTransferInitiated(this.resource, REQUEST_TYPE);
        verify(this.transferListener).transferInitiated(
            eq(new TransferEvent(this.wagon, this.resource, TransferEvent.TRANSFER_INITIATED, REQUEST_TYPE)));
    }

    @Test
    public void fireTransferStarted() {
        this.transferListenerSupport.fireTransferStarted(this.resource, REQUEST_TYPE);
        verify(this.transferListener).transferStarted(eq(new TransferEvent(this.wagon, this.resource, TransferEvent.TRANSFER_STARTED, REQUEST_TYPE)));
    }

    @Test
    public void fireTransferProgress() {
        byte[] buffer = new byte[0];
        int length = 0;
        this.transferListenerSupport.fireTransferProgress(this.resource, REQUEST_TYPE, buffer, length);
        verify(this.transferListener).transferProgress(
            eq(new TransferEvent(this.wagon, this.resource, TransferEvent.TRANSFER_PROGRESS, REQUEST_TYPE)), Matchers.eq(buffer), Matchers.eq(length));
    }

    @Test
    public void fireTransferCompleted() {
        this.transferListenerSupport.fireTransferCompleted(this.resource, REQUEST_TYPE);
        verify(this.transferListener).transferCompleted(
            eq(new TransferEvent(this.wagon, this.resource, TransferEvent.TRANSFER_COMPLETED, REQUEST_TYPE)));
    }

    @Test
    public void fireTransferError() {
        IOException exception = new IOException();
        this.transferListenerSupport.fireTransferError(this.resource, REQUEST_TYPE, exception);
        verify(this.transferListener).transferError(eq(new TransferEvent(this.wagon, this.resource, exception, REQUEST_TYPE)));
    }

}
