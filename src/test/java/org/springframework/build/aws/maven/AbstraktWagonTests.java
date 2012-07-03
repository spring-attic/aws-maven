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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import org.junit.Test;

public final class AbstraktWagonTests {

    private final SessionListenerSupport sessionListenerSupport = mock(SessionListenerSupport.class);

    private final SessionListener sessionListener = mock(SessionListener.class);

    private final TransferListenerSupport transferListenerSupport = mock(TransferListenerSupport.class);

    private final TransferListener transferListener = mock(TransferListener.class);

    private final Repository repository = mock(Repository.class);

    private final AuthenticationInfo authenticationInfo = mock(AuthenticationInfo.class);

    private final ProxyInfoProvider proxyInfoProvider = mock(ProxyInfoProvider.class);

    private final ProxyInfo proxyInfo = mock(ProxyInfo.class);

    private final StubWagon wagon = spy(new StubWagon(true, this.sessionListenerSupport, this.transferListenerSupport));

    @Test
    public void addSessionListener() {
        this.wagon.addSessionListener(this.sessionListener);
        verify(this.sessionListenerSupport).addSessionListener(this.sessionListener);
    }

    @Test
    public void hasSessionListener() {
        this.wagon.hasSessionListener(this.sessionListener);
        verify(this.sessionListenerSupport).hasSessionListener(this.sessionListener);
    }

    @Test
    public void removeSessionListener() {
        this.wagon.removeSessionListener(this.sessionListener);
        verify(this.sessionListenerSupport).removeSessionListener(this.sessionListener);
    }

    @Test
    public void addTransferListener() {
        this.wagon.addTransferListener(this.transferListener);
        verify(this.transferListenerSupport).addTransferListener(this.transferListener);
    }

    @Test
    public void hasTransferListener() {
        this.wagon.hasTransferListener(this.transferListener);
        verify(this.transferListenerSupport).hasTransferListener(this.transferListener);
    }

    @Test
    public void removeTransferListener() {
        this.wagon.removeTransferListener(this.transferListener);
        verify(this.transferListenerSupport).removeTransferListener(this.transferListener);
    }

    @Test
    public void interactive() {
        this.wagon.setInteractive(true);
        assertTrue(this.wagon.isInteractive());
    }

    @Test
    public void connectRepository() throws ConnectionException, AuthenticationException {
        this.wagon.connect(this.repository);

        assertEquals(this.repository, this.wagon.getRepository());
        verify(this.sessionListenerSupport).fireSessionOpening();
        verify(this.wagon).connectToRepository(this.repository, null, null);
        verify(this.sessionListenerSupport).fireSessionLoggedIn();
        verify(this.sessionListenerSupport).fireSessionOpened();
    }

    @Test
    public void connectRepositoryProxyInfo() throws ConnectionException, AuthenticationException {
        this.wagon.connect(this.repository, this.proxyInfo);

        assertEquals(this.repository, this.wagon.getRepository());
        verify(this.sessionListenerSupport).fireSessionOpening();
        verify(this.wagon).connectToRepository(eq(this.repository), (AuthenticationInfo) isNull(), any(NullProtectingProxyInfoProvider.class));
        verify(this.sessionListenerSupport).fireSessionLoggedIn();
        verify(this.sessionListenerSupport).fireSessionOpened();
    }

    @Test
    public void connectRepositoryAuthenticationInfo() throws ConnectionException, AuthenticationException {
        this.wagon.connect(this.repository, this.authenticationInfo);

        assertEquals(this.repository, this.wagon.getRepository());
        verify(this.sessionListenerSupport).fireSessionOpening();
        verify(this.wagon).connectToRepository(this.repository, this.authenticationInfo, null);
        verify(this.sessionListenerSupport).fireSessionLoggedIn();
        verify(this.sessionListenerSupport).fireSessionOpened();
    }

    @Test
    public void connectRepositoryProxyInfoProvider() throws ConnectionException, AuthenticationException {
        this.wagon.connect(this.repository, this.proxyInfoProvider);

        assertEquals(this.repository, this.wagon.getRepository());
        verify(this.sessionListenerSupport).fireSessionOpening();
        verify(this.wagon).connectToRepository(this.repository, null, this.proxyInfoProvider);
        verify(this.sessionListenerSupport).fireSessionLoggedIn();
        verify(this.sessionListenerSupport).fireSessionOpened();
    }

    @Test
    public void connectRepositoryAuthenticationProxyInfo() throws ConnectionException, AuthenticationException {
        this.wagon.connect(this.repository, this.authenticationInfo, this.proxyInfo);

        assertEquals(this.repository, this.wagon.getRepository());
        verify(this.sessionListenerSupport).fireSessionOpening();
        verify(this.wagon).connectToRepository(eq(this.repository), eq(this.authenticationInfo), any(NullProtectingProxyInfoProvider.class));
        verify(this.sessionListenerSupport).fireSessionLoggedIn();
        verify(this.sessionListenerSupport).fireSessionOpened();

    }

    @Test
    public void connectRepositoryAuthenticationInfoProxyInfoProvider() throws ConnectionException, AuthenticationException {
        this.wagon.connect(this.repository, this.authenticationInfo, this.proxyInfoProvider);

        assertEquals(this.repository, this.wagon.getRepository());
        verify(this.sessionListenerSupport).fireSessionOpening();
        verify(this.wagon).connectToRepository(this.repository, this.authenticationInfo, this.proxyInfoProvider);
        verify(this.sessionListenerSupport).fireSessionLoggedIn();
        verify(this.sessionListenerSupport).fireSessionOpened();
    }

    @Test
    public void connectConnectionException() throws ConnectionException, AuthenticationException {
        doThrow(new ConnectionException("")).when(this.wagon).connectToRepository(this.repository, this.authenticationInfo, this.proxyInfoProvider);

        try {
            this.wagon.connect(this.repository, this.authenticationInfo, this.proxyInfoProvider);
            fail();
        } catch (ConnectionException e) {
            assertEquals(this.repository, this.wagon.getRepository());
            verify(this.sessionListenerSupport).fireSessionOpening();
            verify(this.sessionListenerSupport).fireSessionConnectionRefused();
        }
    }

    @Test
    public void connectAuthenticationException() throws ConnectionException, AuthenticationException {
        doThrow(new AuthenticationException("")).when(this.wagon).connectToRepository(this.repository, this.authenticationInfo,
            this.proxyInfoProvider);

        try {
            this.wagon.connect(this.repository, this.authenticationInfo, this.proxyInfoProvider);
            fail();
        } catch (AuthenticationException e) {
            assertEquals(this.repository, this.wagon.getRepository());
            verify(this.sessionListenerSupport).fireSessionOpening();
            verify(this.sessionListenerSupport).fireSessionConnectionRefused();
        }
    }

    @Test
    public void disconnect() throws ConnectionException {
        this.wagon.disconnect();

        verify(this.sessionListenerSupport).fireSessionDisconnecting();
        verify(this.wagon).disconnectFromRepository();
        verify(this.sessionListenerSupport).fireSessionLoggedOff();
        verify(this.sessionListenerSupport).fireSessionDisconnected();
    }

    @Test
    public void disconnectConnectionException() throws ConnectionException {
        doThrow(new ConnectionException("")).when(this.wagon).disconnectFromRepository();

        try {
            this.wagon.disconnect();
            fail();
        } catch (ConnectionException e) {
            verify(this.sessionListenerSupport).fireSessionDisconnecting();
            verify(this.sessionListenerSupport).fireSessionConnectionRefused();

        }
    }

    @Test
    public void get() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        this.wagon.get("foo", new File("bar"));

        verify(this.transferListenerSupport).fireTransferInitiated(new Resource("foo"), TransferEvent.REQUEST_GET);
        verify(this.transferListenerSupport).fireTransferStarted(new Resource("foo"), TransferEvent.REQUEST_GET);
        verify(this.wagon).getResource(eq("foo"), eq(new File("bar")), any(TransferProgress.class));
        verify(this.transferListenerSupport).fireTransferCompleted(new Resource("foo"), TransferEvent.REQUEST_GET);
    }

    @Test
    public void getTransferFailedException() throws ResourceDoesNotExistException, AuthorizationException, TransferFailedException {
        TransferFailedException exception = new TransferFailedException("");
        doThrow(exception).when(this.wagon).getResource(eq("foo"), eq(new File("bar")), any(TransferProgress.class));

        try {
            this.wagon.get("foo", new File("bar"));
            fail();
        } catch (TransferFailedException e) {
            verify(this.transferListenerSupport).fireTransferInitiated(new Resource("foo"), TransferEvent.REQUEST_GET);
            verify(this.transferListenerSupport).fireTransferStarted(new Resource("foo"), TransferEvent.REQUEST_GET);
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getResourceDoesNotExistException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        ResourceDoesNotExistException exception = new ResourceDoesNotExistException("");
        doThrow(exception).when(this.wagon).getResource(eq("foo"), eq(new File("bar")), any(TransferProgress.class));

        try {
            this.wagon.get("foo", new File("bar"));
            fail();
        } catch (ResourceDoesNotExistException e) {
            verify(this.transferListenerSupport).fireTransferInitiated(new Resource("foo"), TransferEvent.REQUEST_GET);
            verify(this.transferListenerSupport).fireTransferStarted(new Resource("foo"), TransferEvent.REQUEST_GET);
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getAuthorizationException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        AuthorizationException exception = new AuthorizationException("");
        doThrow(exception).when(this.wagon).getResource(eq("foo"), eq(new File("bar")), any(TransferProgress.class));

        try {
            this.wagon.get("foo", new File("bar"));
            fail();
        } catch (AuthorizationException e) {
            verify(this.transferListenerSupport).fireTransferInitiated(new Resource("foo"), TransferEvent.REQUEST_GET);
            verify(this.transferListenerSupport).fireTransferStarted(new Resource("foo"), TransferEvent.REQUEST_GET);
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getFileList() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        when(this.wagon.listDirectory("foo")).thenReturn(Arrays.<String> asList());

        assertEquals(Arrays.asList(), this.wagon.getFileList("foo"));
    }

    @Test
    public void getFileListTransferFailedException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        TransferFailedException exception = new TransferFailedException("");
        when(this.wagon.listDirectory("foo")).thenThrow(exception);

        try {
            this.wagon.getFileList("foo");
            fail();
        } catch (TransferFailedException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getFileResourceDoesNotExistException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        ResourceDoesNotExistException exception = new ResourceDoesNotExistException("");
        when(this.wagon.listDirectory("foo")).thenThrow(exception);

        try {
            this.wagon.getFileList("foo");
            fail();
        } catch (ResourceDoesNotExistException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getFileListAuthorizationException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        AuthorizationException exception = new AuthorizationException("");
        when(this.wagon.listDirectory("foo")).thenThrow(exception);

        try {
            this.wagon.getFileList("foo");
            fail();
        } catch (AuthorizationException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getIfNewerOlder() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        when(this.wagon.isRemoteResourceNewer("foo", 0)).thenReturn(false);
        assertFalse(this.wagon.getIfNewer("foo", new File("bar"), 0));
    }

    @Test
    public void getIfNewerNewer() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        when(this.wagon.isRemoteResourceNewer("foo", 0)).thenReturn(true);

        assertTrue(this.wagon.getIfNewer("foo", new File("bar"), 0));
        verify(this.wagon).getResource(eq("foo"), eq(new File("bar")), any(TransferProgress.class));
    }

    @Test
    public void getIfNewerTransferFailedException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        TransferFailedException exception = new TransferFailedException("");
        when(this.wagon.isRemoteResourceNewer("foo", 0)).thenThrow(exception);

        try {
            this.wagon.getIfNewer("foo", new File("bar"), 0);
            fail();
        } catch (TransferFailedException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getIfNewerResourceDoesNotExistException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        ResourceDoesNotExistException exception = new ResourceDoesNotExistException("");
        when(this.wagon.isRemoteResourceNewer("foo", 0)).thenThrow(exception);

        try {
            this.wagon.getIfNewer("foo", new File("bar"), 0);
            fail();
        } catch (ResourceDoesNotExistException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void getIfNewerAuthorizationException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        AuthorizationException exception = new AuthorizationException("");
        when(this.wagon.isRemoteResourceNewer("foo", 0)).thenThrow(exception);

        try {
            this.wagon.getIfNewer("foo", new File("bar"), 0);
            fail();
        } catch (AuthorizationException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void openConnection() {
        this.wagon.openConnection();
    }

    @Test
    public void put() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        this.wagon.put(new File("foo"), "bar");

        verify(this.transferListenerSupport).fireTransferInitiated(new Resource("bar"), TransferEvent.REQUEST_PUT);
        verify(this.transferListenerSupport).fireTransferStarted(new Resource("bar"), TransferEvent.REQUEST_PUT);
        verify(this.wagon).putResource(eq(new File("foo")), eq("bar"), any(TransferProgress.class));
        verify(this.transferListenerSupport).fireTransferCompleted(new Resource("bar"), TransferEvent.REQUEST_PUT);
    }

    @Test
    public void putTransferFailedException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        TransferFailedException exception = new TransferFailedException("");
        doThrow(exception).when(this.wagon).putResource(eq(new File("foo")), eq("bar"), any(TransferProgress.class));

        try {
            this.wagon.put(new File("foo"), "bar");
            fail();
        } catch (TransferFailedException e) {
            verify(this.transferListenerSupport).fireTransferInitiated(new Resource("bar"), TransferEvent.REQUEST_PUT);
            verify(this.transferListenerSupport).fireTransferStarted(new Resource("bar"), TransferEvent.REQUEST_PUT);
            verify(this.transferListenerSupport).fireTransferError(new Resource("bar"), TransferEvent.REQUEST_PUT, exception);
        }
    }

    @Test
    public void putResourceDoesNotExistException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        ResourceDoesNotExistException exception = new ResourceDoesNotExistException("");
        doThrow(exception).when(this.wagon).putResource(eq(new File("foo")), eq("bar"), any(TransferProgress.class));

        try {
            this.wagon.put(new File("foo"), "bar");
            fail();
        } catch (ResourceDoesNotExistException e) {
            verify(this.transferListenerSupport).fireTransferInitiated(new Resource("bar"), TransferEvent.REQUEST_PUT);
            verify(this.transferListenerSupport).fireTransferStarted(new Resource("bar"), TransferEvent.REQUEST_PUT);
            verify(this.transferListenerSupport).fireTransferError(new Resource("bar"), TransferEvent.REQUEST_PUT, exception);
        }
    }

    @Test
    public void putAuthorizationException() throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        AuthorizationException exception = new AuthorizationException("");
        doThrow(exception).when(this.wagon).putResource(eq(new File("foo")), eq("bar"), any(TransferProgress.class));

        try {
            this.wagon.put(new File("foo"), "bar");
            fail();
        } catch (AuthorizationException e) {
            verify(this.transferListenerSupport).fireTransferInitiated(new Resource("bar"), TransferEvent.REQUEST_PUT);
            verify(this.transferListenerSupport).fireTransferStarted(new Resource("bar"), TransferEvent.REQUEST_PUT);
            verify(this.transferListenerSupport).fireTransferError(new Resource("bar"), TransferEvent.REQUEST_PUT, exception);
        }
    }

    @Test
    public void putDirectory() throws IOException, TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        File directory = new File("target/test");
        directory.mkdirs();
        File file = new File(directory, "test.txt");
        file.createNewFile();

        this.wagon.putDirectory(directory, "foo");

        verify(this.transferListenerSupport).fireTransferInitiated(new Resource("foo/test.txt"), TransferEvent.REQUEST_PUT);
        verify(this.transferListenerSupport).fireTransferStarted(new Resource("foo/test.txt"), TransferEvent.REQUEST_PUT);
        verify(this.wagon).putResource(eq(new File("target/test/test.txt")), eq("foo/test.txt"), any(TransferProgress.class));
        verify(this.transferListenerSupport).fireTransferCompleted(new Resource("foo/test.txt"), TransferEvent.REQUEST_PUT);
    }

    @Test
    public void resourceExists() throws TransferFailedException, AuthorizationException {
        this.wagon.resourceExists("foo");
        verify(this.wagon).doesRemoteResourceExist("foo");
    }

    @Test
    public void resourceExistsAuthorizationException() throws TransferFailedException, AuthorizationException {
        AuthorizationException exception = new AuthorizationException("");
        when(this.wagon.doesRemoteResourceExist("foo")).thenThrow(exception);

        try {
            this.wagon.resourceExists("foo");
            fail();
        } catch (AuthorizationException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void resourceExistsTransferFailedException() throws TransferFailedException, AuthorizationException {
        TransferFailedException exception = new TransferFailedException("");
        when(this.wagon.doesRemoteResourceExist("foo")).thenThrow(exception);

        try {
            this.wagon.resourceExists("foo");
            fail();
        } catch (TransferFailedException e) {
            verify(this.transferListenerSupport).fireTransferError(new Resource("foo"), TransferEvent.REQUEST_GET, exception);
        }
    }

    @Test
    public void supportsDirectoryCopy() {
        assertTrue(this.wagon.supportsDirectoryCopy());
    }

    @Test
    public void readTimeOut() {
        assertEquals(Wagon.DEFAULT_READ_TIMEOUT, this.wagon.getReadTimeout());
        this.wagon.setReadTimeout(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, this.wagon.getReadTimeout());
    }

    @Test
    public void timeOut() {
        assertEquals(Wagon.DEFAULT_CONNECTION_TIMEOUT, this.wagon.getTimeout());
        this.wagon.setTimeout(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, this.wagon.getTimeout());
    }

    @SuppressWarnings("unused")
    @Test
    public void simpleConstructor() {
        new StubWagon(true);
    }

    private static class StubWagon extends AbstractWagon {

        protected StubWagon(boolean supportsDirectoryCopy) {
            super(supportsDirectoryCopy);
        }

        protected StubWagon(boolean supportsDirectoryCopy, SessionListenerSupport sessionListenerSupport,
            TransferListenerSupport transferListenerSupport) {
            super(supportsDirectoryCopy, sessionListenerSupport, transferListenerSupport);
        }

        @Override
        protected void connectToRepository(Repository source, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfo)
            throws ConnectionException, AuthenticationException {
        }

        @Override
        protected boolean doesRemoteResourceExist(String resourceName) throws TransferFailedException, AuthorizationException {
            return false;
        }

        @Override
        protected void disconnectFromRepository() throws ConnectionException {
        }

        @Override
        protected void getResource(String resourceName, File destination, TransferProgress progress) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        }

        @Override
        protected boolean isRemoteResourceNewer(String resourceName, long timestamp) throws TransferFailedException, ResourceDoesNotExistException,
            AuthorizationException {
            return false;
        }

        @Override
        protected List<String> listDirectory(String directory) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
            return null;
        }

        @Override
        protected void putResource(File source, String destination, TransferProgress progress) throws TransferFailedException,
            ResourceDoesNotExistException, AuthorizationException {
        }

    }

}
