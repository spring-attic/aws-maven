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

abstract class AbstractWagon implements Wagon {

    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    private boolean interactive = false;

    private int readTimeout = DEFAULT_READ_TIMEOUT;

    private Repository repository = null;

    private final boolean supportsDirectoryCopy;

    private final SessionListenerSupport sessionListenerSupport;

    private final TransferListenerSupport transferListenerSupport;

    protected AbstractWagon(boolean supportsDirectoryCopy) {
        this.supportsDirectoryCopy = supportsDirectoryCopy;
        this.sessionListenerSupport = new StandardSessionListenerSupport(this);
        this.transferListenerSupport = new StandardTransferListenerSupport(this);
    }

    protected AbstractWagon(boolean supportsDirectoryCopy, SessionListenerSupport sessionListenerSupport,
        TransferListenerSupport transferListenerSupport) {
        this.supportsDirectoryCopy = supportsDirectoryCopy;
        this.sessionListenerSupport = sessionListenerSupport;
        this.transferListenerSupport = transferListenerSupport;
    }

    @Override
    public final void addSessionListener(SessionListener sessionListener) {
        this.sessionListenerSupport.addSessionListener(sessionListener);
    }

    @Override
    public final boolean hasSessionListener(SessionListener sessionListener) {
        return this.sessionListenerSupport.hasSessionListener(sessionListener);
    }

    @Override
    public final void removeSessionListener(SessionListener sessionListener) {
        this.sessionListenerSupport.removeSessionListener(sessionListener);
    }

    @Override
    public final void addTransferListener(TransferListener transferListener) {
        this.transferListenerSupport.addTransferListener(transferListener);
    }

    @Override
    public final boolean hasTransferListener(TransferListener transferListener) {
        return this.transferListenerSupport.hasTransferListener(transferListener);
    }

    @Override
    public final void removeTransferListener(TransferListener transferListener) {
        this.transferListenerSupport.removeTransferListener(transferListener);
    }

    @Override
    public final Repository getRepository() {
        return this.repository;
    }

    @Override
    public final boolean isInteractive() {
        return this.interactive;
    }

    @Override
    public final void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    @Override
    public final void connect(Repository source) throws ConnectionException, AuthenticationException {
        connect(source, null, (ProxyInfoProvider) null);
    }

    @Override
    public final void connect(Repository source, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
        connect(source, null, proxyInfo);
    }

    @Override
    public final void connect(Repository source, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {
        connect(source, authenticationInfo, (ProxyInfoProvider) null);
    }

    @Override
    public final void connect(Repository source, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {
        connect(source, null, proxyInfoProvider);
    }

    @Override
    public final void connect(Repository source, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException,
        AuthenticationException {
        connect(source, authenticationInfo, new NullProtectingProxyInfoProvider(proxyInfo));
    }

    @Override
    public final void connect(Repository source, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider)
        throws ConnectionException, AuthenticationException {
        this.repository = source;
        this.sessionListenerSupport.fireSessionOpening();
        try {
            connectToRepository(source, authenticationInfo, proxyInfoProvider);
            this.sessionListenerSupport.fireSessionLoggedIn();
            this.sessionListenerSupport.fireSessionOpened();
        } catch (ConnectionException e) {
            this.sessionListenerSupport.fireSessionConnectionRefused();
            throw e;
        } catch (AuthenticationException e) {
            this.sessionListenerSupport.fireSessionConnectionRefused();
            throw e;
        }
    }

    @Override
    public final void disconnect() throws ConnectionException {
        this.sessionListenerSupport.fireSessionDisconnecting();
        try {
            disconnectFromRepository();
            this.sessionListenerSupport.fireSessionLoggedOff();
            this.sessionListenerSupport.fireSessionDisconnected();
        } catch (ConnectionException e) {
            this.sessionListenerSupport.fireSessionConnectionRefused();
            throw e;
        }
    }

    @Override
    public final void get(String resourceName, File destination) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        Resource resource = new Resource(resourceName);
        this.transferListenerSupport.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
        this.transferListenerSupport.fireTransferStarted(resource, TransferEvent.REQUEST_GET);

        try {
            getResource(resourceName, destination, new StandardTransferProgress(resource, TransferEvent.REQUEST_GET, this.transferListenerSupport));
            this.transferListenerSupport.fireTransferCompleted(resource, TransferEvent.REQUEST_GET);
        } catch (TransferFailedException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (ResourceDoesNotExistException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (AuthorizationException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        }
    }

    @Override
    public final List<String> getFileList(String destinationDirectory) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        try {
            return listDirectory(destinationDirectory);
        } catch (TransferFailedException e) {
            this.transferListenerSupport.fireTransferError(new Resource(destinationDirectory), TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (ResourceDoesNotExistException e) {
            this.transferListenerSupport.fireTransferError(new Resource(destinationDirectory), TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (AuthorizationException e) {
            this.transferListenerSupport.fireTransferError(new Resource(destinationDirectory), TransferEvent.REQUEST_GET, e);
            throw e;
        }
    }

    @Override
    public final boolean getIfNewer(String resourceName, File destination, long timestamp) throws TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(resourceName);
        try {
            if (isRemoteResourceNewer(resourceName, timestamp)) {
                get(resourceName, destination);
                return true;
            }

            return false;
        } catch (TransferFailedException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (ResourceDoesNotExistException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (AuthorizationException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
            throw e;
        }
    }

    @Override
    public final void openConnection() {
        // Nothing to do here (never called by the wagon manager)
    }

    @Override
    public final void put(File source, String destination) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(destination);
        this.transferListenerSupport.fireTransferInitiated(resource, TransferEvent.REQUEST_PUT);
        this.transferListenerSupport.fireTransferStarted(resource, TransferEvent.REQUEST_PUT);

        try {
            putResource(source, destination, new StandardTransferProgress(resource, TransferEvent.REQUEST_PUT, this.transferListenerSupport));
            this.transferListenerSupport.fireTransferCompleted(resource, TransferEvent.REQUEST_PUT);
        } catch (TransferFailedException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
            throw e;
        } catch (ResourceDoesNotExistException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
            throw e;
        } catch (AuthorizationException e) {
            this.transferListenerSupport.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
            throw e;
        }
    }

    @Override
    public final void putDirectory(File sourceDirectory, String destinationDirectory) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        for (File f : sourceDirectory.listFiles()) {
            put(f, destinationDirectory + "/" + f.getName());
        }
    }

    @Override
    public final boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
        try {
            return doesRemoteResourceExist(resourceName);
        } catch (AuthorizationException e) {
            this.transferListenerSupport.fireTransferError(new Resource(resourceName), TransferEvent.REQUEST_GET, e);
            throw e;
        } catch (TransferFailedException e) {
            this.transferListenerSupport.fireTransferError(new Resource(resourceName), TransferEvent.REQUEST_GET, e);
            throw e;
        }
    }

    @Override
    public final boolean supportsDirectoryCopy() {
        return this.supportsDirectoryCopy;
    }

    @Override
    public final int getReadTimeout() {
        return this.readTimeout;
    }

    @Override
    public final void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public final int getTimeout() {
        return this.connectionTimeout;
    }

    @Override
    public final void setTimeout(int timeout) {
        this.connectionTimeout = timeout;
    }

    protected abstract void connectToRepository(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider)
        throws ConnectionException, AuthenticationException;

    protected abstract boolean doesRemoteResourceExist(String resourceName) throws TransferFailedException, AuthorizationException;

    protected abstract void disconnectFromRepository() throws ConnectionException;

    protected abstract void getResource(String resourceName, File destination, TransferProgress transferProgress) throws TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException;

    protected abstract boolean isRemoteResourceNewer(String resourceName, long timestamp) throws TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException;

    protected abstract List<String> listDirectory(String directory) throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException;

    protected abstract void putResource(File source, String destination, TransferProgress transferProgress) throws TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException;

}
