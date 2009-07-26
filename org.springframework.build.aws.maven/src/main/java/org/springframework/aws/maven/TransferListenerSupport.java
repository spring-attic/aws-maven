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
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;

/**
 * Support for sending messages to Maven transfer listeners. Automates the
 * collection of listeners and the iteration over that collection when an event
 * is fired.
 * 
 * @author Ben Hale
 */
class TransferListenerSupport {

	private Wagon wagon;

	private Set<TransferListener> listeners = new HashSet<TransferListener>();

	/**
	 * Creates a new instance
	 * @param wagon The wagon that events will come from
	 */
	public TransferListenerSupport(Wagon wagon) {
		this.wagon = wagon;
	}

	/**
	 * Adds a listener to the collection
	 * @param listener The listener to add
	 */
	public void addListener(TransferListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener from the collection
	 * @param listener The listener to remove
	 */
	public void removeListener(TransferListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Whether the collection already contains a listener
	 * @param listener The listener to check for
	 * @return whether the collection contains the listener
	 */
	public boolean hasListener(TransferListener listener) {
		return listeners.contains(listener);
	}

	/**
	 * Sends a transfer initated event to all listeners
	 * @param resource The resource being transfered
	 * @param requestType GET or PUT request
	 * @see TransferEvent#TRANSFER_INITIATED
	 */
	public void fireTransferInitiated(Resource resource, int requestType) {
		TransferEvent event = new TransferEvent(wagon, resource, TransferEvent.TRANSFER_INITIATED, requestType);
		for (TransferListener listener : listeners) {
			listener.transferInitiated(event);
		}
	}

	/**
	 * Sends a transfer started event to all listeners
	 * @param resource The resource being transfered
	 * @param requestType GET or PUT request
	 * @see TransferEvent#TRANSFER_STARTED
	 */
	public void fireTransferStarted(Resource resource, int requestType) {
		TransferEvent event = new TransferEvent(wagon, resource, TransferEvent.TRANSFER_STARTED, requestType);
		for (TransferListener listener : listeners) {
			listener.transferStarted(event);
		}
	}

	public void fireTransferProgress(Resource resource, int requestType, byte[] buffer, int length) {
		TransferEvent event = new TransferEvent(wagon, resource, TransferEvent.TRANSFER_PROGRESS, requestType);
		for (TransferListener listener : listeners) {
			listener.transferProgress(event, buffer, length);
		}
	}

	/**
	 * Sends a transfer completed event to all listeners
	 * @param resource The resource being transfered
	 * @param requestType GET or PUT request
	 * @see TransferEvent#TRANSFER_COMPLETED
	 */
	public void fireTransferCompleted(Resource resource, int requestType) {
		TransferEvent event = new TransferEvent(wagon, resource, TransferEvent.TRANSFER_COMPLETED, requestType);
		for (TransferListener listener : listeners) {
			listener.transferCompleted(event);
		}
	}

	/**
	 * Sends a transfer error event to all listeners
	 * @param resource The resource being transfered
	 * @param requestType GET or PUT request
	 * @param e The transfer error
	 */
	public void fireTransferError(Resource resource, int requestType, Exception e) {
		TransferEvent event = new TransferEvent(wagon, resource, e, requestType);
		for (TransferListener listener : listeners) {
			listener.transferError(event);
		}
	}
}
