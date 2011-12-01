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

import java.io.File;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

/**
 * An abstract implementation of the Wagon interface. This implementation manages listener and other common behaviors.
 * 
 * @author Ben Hale
 * @since 1.1
 */
abstract class AbstractWagon implements Wagon {

    private boolean interactive;

    private Repository repository;

    private final boolean supportsDirectoryCopy;

    private final SessionListenerSupport sessionListeners = new SessionListenerSupport(this);

    private final TransferListenerSupport transferListeners = new TransferListenerSupport(this);

    private int timeout;

    AbstractWagon(boolean supportsDirectoryCopy) {
        this.supportsDirectoryCopy = supportsDirectoryCopy;
    }

    public final void addSessionListener(SessionListener listener) {
        this.sessionListeners.addListener(listener);
    }

    public final boolean hasSessionListener(SessionListener listener) {
        return this.sessionListeners.hasListener(listener);
    }

    public final void removeSessionListener(SessionListener listener) {
        this.sessionListeners.removeListener(listener);
    }

    public final void addTransferListener(TransferListener listener) {
        this.transferListeners.addListener(listener);
    }

    public final boolean hasTransferListener(TransferListener listener) {
        return this.transferListeners.hasListener(listener);
    }

    public final void removeTransferListener(TransferListener listener) {
        this.transferListeners.removeListener(listener);
    }

    public final Repository getRepository() {
        return this.repository;
    }

    public final boolean isInteractive() {
        return this.interactive;
    }

    public final void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public final void connect(Repository source) throws ConnectionException, AuthenticationException {
        connect(source, null, (ProxyInfoProvider) null);
    }

    public final void connect(Repository source, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        connect(source, null, proxyInfo);
    }

    public final void connect(Repository source, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {
        connect(source, authenticationInfo, (ProxyInfoProvider) null);
    }

    public final void connect(Repository source, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        connect(source, null, proxyInfoProvider);
    }

    public final void connect(Repository source, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException,
        AuthenticationException {
        final ProxyInfo proxy = proxyInfo;
        connect(source, authenticationInfo, new ProxyInfoProvider() {

            public ProxyInfo getProxyInfo(String protocol) {
                if (protocol == null || proxy == null || protocol.equalsIgnoreCase(proxy.getType())) {
                    return proxy;
                } else {
                    return null;
                }
            }
        });
    }

    public final void connect(Repository source, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider)
        throws ConnectionException, AuthenticationException {
        this.repository = source;
        this.sessionListeners.fireSessionOpening();
        try {
            connectToRepository(source, authenticationInfo, proxyInfoProvider);
        } catch (ConnectionException e) {
            this.sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (AuthenticationException e) {
            this.sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (Exception e) {
            this.sessionListeners.fireSessionConnectionRefused();
            throw new ConnectionException("Could not connect to repository", e);
        }
        this.sessionListeners.fireSessionLoggedIn();
        this.sessionListeners.fireSessionOpened();
    }

    public final void disconnect() throws ConnectionException {
        this.sessionListeners.fireSessionDisconnecting();
        try {
            disconnectFromRepository();
        } catch (ConnectionException e) {
            this.sessionListeners.fireSessionConnectionRefused();
            throw e;
        } catch (Exception e) {
            this.sessionListeners.fireSessionConnectionRefused();
            throw new ConnectionException("Could not disconnect from repository", e);
        }
        this.sessionListeners.fireSessionLoggedOff();
        this.sessionListeners.fireSessionDisconnected();
    }

    public final void get(String resourceName, File destination) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        Resource resource = new Resource(resourceName);
        this.transferListeners.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        this.transferListeners.fireTransferStarted(resource, TransferEvent.REQUEST_GET);

        try {
            getResource(resourceName, destination, new TransferProgress(resource, TransferEvent.REQUEST_GET, this.transferListeners));
            this.transferListeners.fireTransferCompleted(resource, TransferEvent.REQUEST_GET);
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            this.transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        }
    }

    public final List<String> getFileList(String destinationDirectory) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        try {
            return listDirectory(destinationDirectory);
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            this.sessionListeners.fireSessionError(e);
            throw new TransferFailedException("Listing of directory " + destinationDirectory + "failed", e);
        }
    }

    public final boolean getIfNewer(String resourceName, File destination, long timestamp) throws TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(resourceName);
        try {
            if (isRemoteResourceNewer(resourceName, timestamp)) {
                get(resourceName, destination);
                return true;
            } else {
                return false;
            }
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            this.transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        }
    }

    public final void openConnection() throws ConnectionException, AuthenticationException {
        // Nothing to do here (never called by the wagon manager)
    }

    public final void put(File source, String destination) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(destination);
        this.transferListeners.fireTransferInitiated(resource, TransferEvent.REQUEST_PUT);
        this.transferListeners.fireTransferStarted(resource, TransferEvent.REQUEST_PUT);

        try {
            putResource(source, destination, new TransferProgress(resource, TransferEvent.REQUEST_PUT, this.transferListeners));
            this.transferListeners.fireTransferCompleted(resource, TransferEvent.REQUEST_PUT);
        } catch (TransferFailedException e) {
            throw e;
        } catch (ResourceDoesNotExistException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            this.transferListeners.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
            throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        }
    }

    public final void putDirectory(File sourceDirectory, String destinationDirectory) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        for (File f : sourceDirectory.listFiles()) {
            put(f, destinationDirectory + "/" + f.getName());
        }
    }

    public final boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
        try {
            return doesRemoteResourceExist(resourceName);
        } catch (TransferFailedException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            this.sessionListeners.fireSessionError(e);
            throw new TransferFailedException("Listing of resource " + resourceName + "failed", e);
        }
    }

    public final boolean supportsDirectoryCopy() {
        return this.supportsDirectoryCopy;
    }

    public final int getTimeout() {
        return this.timeout;
    }

    public final void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Subclass must implement with specific connection behavior
     * 
     * @param source The repository connection information
     * @param authenticationInfo Authentication information, if any
     * @param proxyInfo Proxy information, if any
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void connectToRepository(Repository source, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfo)
        throws Exception;

    /**
     * Subclass must implement with specific detection behavior
     * 
     * @param resourceName The remote resource to detect
     * @return true if the remote resource exists
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract boolean doesRemoteResourceExist(String resourceName) throws Exception;

    /**
     * Subclasses must implement with specific disconnection behavior
     * 
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void disconnectFromRepository() throws Exception;

    /**
     * Subclass must implement with specific get behavior
     * 
     * @param resourceName The name of the remote resource to read
     * @param destination The local file to write to
     * @param progress A progress notifier for the upload. It must be used or hashes will not be calculated correctly
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void getResource(String resourceName, File destination, TransferProgress progress) throws Exception;

    /**
     * Subclass must implement with newer detection behavior
     * 
     * @param resourceName The name of the resource being compared
     * @param timestamp The timestamp to compare against
     * @return true if the current version of the resource is newer than the timestamp
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract boolean isRemoteResourceNewer(String resourceName, long timestamp) throws Exception;

    /**
     * Subclass must implement with specific directory listing behavior
     * 
     * @param directory The directory to list files in
     * @return A collection of file names
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract List<String> listDirectory(String directory) throws Exception;

    /**
     * Subclasses must implement with specific put behavior
     * 
     * @param source The local source file to read from
     * @param destination The name of the remote resource to write to
     * @param progress A progress notifier for the upload. It must be used or hashes will not be calculated correctly
     * @throws Exception Implementations can throw any exception and it will be handled by the base class
     */
    protected abstract void putResource(File source, String destination, TransferProgress progress) throws Exception;

}
