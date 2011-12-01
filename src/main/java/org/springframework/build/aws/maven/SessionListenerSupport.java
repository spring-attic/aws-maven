/*
 * Copyright 2010 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.build.aws.maven;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;

/**
 * Support for sending messages to Maven session listeners. Automates the collection of listeners and the iteration over
 * that collection when an event is fired.
 * 
 * @author Ben Hale
 */
final class SessionListenerSupport {

    private final Wagon wagon;

    private final Set<SessionListener> listeners = new HashSet<SessionListener>();

    /**
     * Creates a new instance
     * 
     * @param wagon The wagon that events will come from
     */
    SessionListenerSupport(Wagon wagon) {
        this.wagon = wagon;
    }

    /**
     * Adds a listener to the collection
     * 
     * @param listener The listener to add
     */
    void addListener(SessionListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener from the collection
     * 
     * @param listener The listener to remove
     */
    void removeListener(SessionListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Whether the collection already contains a listener
     * 
     * @param listener The listener to check for
     * @return Whether the collection contains a listener
     */
    boolean hasListener(SessionListener listener) {
        return this.listeners.contains(listener);
    }

    /**
     * Sends a session opening event to all listeners
     * 
     * @see SessionEvent#SESSION_OPENING
     */
    void fireSessionOpening() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_OPENING);
        for (SessionListener listener : this.listeners) {
            listener.sessionOpening(event);
        }
    }

    /**
     * Sends a session opened event to all listeners
     * 
     * @see SessionEvent#SESSION_OPENED
     */
    void fireSessionOpened() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_OPENED);
        for (SessionListener listener : this.listeners) {
            listener.sessionOpened(event);
        }
    }

    /**
     * Sends a session disconnecting event to all listeners
     * 
     * @see SessionEvent#SESSION_DISCONNECTING
     */
    void fireSessionDisconnecting() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTING);
        for (SessionListener listener : this.listeners) {
            listener.sessionDisconnecting(event);
        }
    }

    /**
     * Sends a session disconnected event to all listeners
     * 
     * @see SessionEvent#SESSION_DISCONNECTED
     */
    void fireSessionDisconnected() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTED);
        for (SessionListener listener : this.listeners) {
            listener.sessionDisconnected(event);
        }
    }

    /**
     * Sends a session connection refused event to all listeners
     * 
     * @see SessionEvent#SESSION_CONNECTION_REFUSED
     */
    void fireSessionConnectionRefused() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_CONNECTION_REFUSED);
        for (SessionListener listener : this.listeners) {
            listener.sessionConnectionRefused(event);
        }
    }

    /**
     * Sends a session logged in event to all listeners
     * 
     * @see SessionEvent#SESSION_LOGGED_IN
     */
    void fireSessionLoggedIn() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_IN);
        for (SessionListener listener : this.listeners) {
            listener.sessionLoggedIn(event);
        }
    }

    /**
     * Sends a session logged off event to all listeners
     * 
     * @see SessionEvent#SESSION_LOGGED_OFF
     */
    void fireSessionLoggedOff() {
        SessionEvent event = new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_OFF);
        for (SessionListener listener : this.listeners) {
            listener.sessionLoggedOff(event);
        }
    }

    /**
     * Sends a session error event to all listeners
     * 
     * @param e The session error
     */
    void fireSessionError(Exception e) {
        SessionEvent event = new SessionEvent(this.wagon, e);
        for (SessionListener listener : this.listeners) {
            listener.sessionError(event);
        }
    }
}
