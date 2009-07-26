/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.aws.maven;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;

/**
 * Support for sending messages to Maven session listeners. Automates the
 * collection of listeners and the iteration over that collection when an event
 * is fired.
 * 
 * @author Ben Hale
 */
class SessionListenerSupport {

	private Wagon wagon;

	private Set<SessionListener> listeners = new HashSet<SessionListener>();

	/**
	 * Creates a new instance
	 * @param wagon The wagon that events will come from
	 */
	public SessionListenerSupport(Wagon wagon) {
		this.wagon = wagon;
	}

	/**
	 * Adds a listener to the collection
	 * @param listener The listener to add
	 */
	public void addListener(SessionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener from the collection
	 * @param listener The listener to remove
	 */
	public void removeListener(SessionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Whether the collection already contains a listener
	 * @param listener The listener to check for
	 * @return Whether the collection contains a listener
	 */
	public boolean hasListener(SessionListener listener) {
		return listeners.contains(listener);
	}

	/**
	 * Sends a session opening event to all listeners
	 * @see SessionEvent#SESSION_OPENING
	 */
	public void fireSessionOpening() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_OPENING);
		for (SessionListener listener : listeners) {
			listener.sessionOpening(event);
		}
	}

	/**
	 * Sends a session opened event to all listeners
	 * @see SessionEvent#SESSION_OPENED
	 */
	public void fireSessionOpened() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_OPENED);
		for (SessionListener listener : listeners) {
			listener.sessionOpened(event);
		}
	}

	/**
	 * Sends a session disconnecting event to all listeners
	 * @see SessionEvent#SESSION_DISCONNECTING
	 */
	public void fireSessionDisconnecting() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_DISCONNECTING);
		for (SessionListener listener : listeners) {
			listener.sessionDisconnecting(event);
		}
	}

	/**
	 * Sends a session disconnected event to all listeners
	 * @see SessionEvent#SESSION_DISCONNECTED
	 */
	public void fireSessionDisconnected() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_DISCONNECTED);
		for (SessionListener listener : listeners) {
			listener.sessionDisconnected(event);
		}
	}

	/**
	 * Sends a session connection refused event to all listeners
	 * @see SessionEvent#SESSION_CONNECTION_REFUSED
	 */
	public void fireSessionConnectionRefused() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_CONNECTION_REFUSED);
		for (SessionListener listener : listeners) {
			listener.sessionConnectionRefused(event);
		}
	}

	/**
	 * Sends a session logged in event to all listeners
	 * @see SessionEvent#SESSION_LOGGED_IN
	 */
	public void fireSessionLoggedIn() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_LOGGED_IN);
		for (SessionListener listener : listeners) {
			listener.sessionLoggedIn(event);
		}
	}

	/**
	 * Sends a session logged off event to all listeners
	 * @see SessionEvent#SESSION_LOGGED_OFF
	 */
	public void fireSessionLoggedOff() {
		SessionEvent event = new SessionEvent(wagon, SessionEvent.SESSION_LOGGED_OFF);
		for (SessionListener listener : listeners) {
			listener.sessionLoggedOff(event);
		}
	}

	/**
	 * Sends a session error event to all listeners
	 * @param e The session error
	 */
	public void fireSessionError(Exception e) {
		SessionEvent event = new SessionEvent(wagon, e);
		for (SessionListener listener : listeners) {
			listener.sessionError(event);
		}
	}
}
