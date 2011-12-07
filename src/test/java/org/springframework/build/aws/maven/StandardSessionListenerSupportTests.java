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
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;
import org.junit.Before;
import org.junit.Test;

public final class StandardSessionListenerSupportTests {

    private final Wagon wagon = mock(Wagon.class);

    private final SessionListener sessionListener = mock(SessionListener.class);

    private final StandardSessionListenerSupport sessionListenerSupport = new StandardSessionListenerSupport(this.wagon);

    @Before
    public void addSessionListener() {
        this.sessionListenerSupport.addSessionListener(this.sessionListener);
    }

    @Test
    public void sessionListenerManagement() {
        assertTrue(this.sessionListenerSupport.hasSessionListener(this.sessionListener));
        this.sessionListenerSupport.removeSessionListener(this.sessionListener);
        assertFalse(this.sessionListenerSupport.hasSessionListener(this.sessionListener));
    }

    @Test
    public void fireSessionOpening() {
        this.sessionListenerSupport.fireSessionOpening();
        verify(this.sessionListener).sessionOpening(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_OPENING)));
    }

    @Test
    public void fireSessionOpened() {
        this.sessionListenerSupport.fireSessionOpened();
        verify(this.sessionListener).sessionOpened(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_OPENED)));
    }

    @Test
    public void fireSessionDisconnecting() {
        this.sessionListenerSupport.fireSessionDisconnecting();
        verify(this.sessionListener).sessionDisconnecting(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTING)));
    }

    @Test
    public void fireSessionDisonnected() {
        this.sessionListenerSupport.fireSessionDisconnected();
        verify(this.sessionListener).sessionDisconnected(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTED)));
    }

    @Test
    public void fireSessionConnectionRefused() {
        this.sessionListenerSupport.fireSessionConnectionRefused();
        verify(this.sessionListener).sessionConnectionRefused(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_CONNECTION_REFUSED)));
    }

    @Test
    public void fireSessionLoggedIn() {
        this.sessionListenerSupport.fireSessionLoggedIn();
        verify(this.sessionListener).sessionLoggedIn(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_IN)));
    }

    @Test
    public void fireSessionLoggedOff() {
        this.sessionListenerSupport.fireSessionLoggedOff();
        verify(this.sessionListener).sessionLoggedOff(eq(new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_OFF)));
    }

    @Test
    public void fireSessionError() {
        IOException exception = new IOException();
        this.sessionListenerSupport.fireSessionError(exception);
        verify(this.sessionListener).sessionError(eq(new SessionEvent(this.wagon, exception)));
    }

}
