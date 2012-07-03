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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.junit.Test;

import com.amazonaws.ClientConfiguration;

public final class S3UtilsTests {

    private static final int PORT = 100;

    private final ProxyInfoProvider proxyInfoProvider = mock(ProxyInfoProvider.class);

    private final ProxyInfo proxyInfo = mock(ProxyInfo.class);

    @Test
    public void getBucketName() {
        assertEquals("dist.springsource.com", S3Utils.getBucketName(createRepository("/")));
    }

    @Test
    public void getBaseDirectory() {
        assertEquals("", S3Utils.getBaseDirectory(createRepository("")));
        assertEquals("", S3Utils.getBaseDirectory(createRepository("/")));
        assertEquals("foo/", S3Utils.getBaseDirectory(createRepository("/foo")));
        assertEquals("foo/", S3Utils.getBaseDirectory(createRepository("/foo/")));
        assertEquals("foo/bar/", S3Utils.getBaseDirectory(createRepository("/foo/bar")));
        assertEquals("foo/bar/", S3Utils.getBaseDirectory(createRepository("/foo/bar/")));
    }

    @Test
    public void getClientConfiguration() {
        when(this.proxyInfoProvider.getProxyInfo("s3")).thenReturn(this.proxyInfo);
        when(this.proxyInfo.getHost()).thenReturn("foo");
        when(this.proxyInfo.getPort()).thenReturn(PORT);

        ClientConfiguration clientConfiguration = S3Utils.getClientConfiguration(this.proxyInfoProvider);
        assertEquals("foo", clientConfiguration.getProxyHost());
        assertEquals(100, clientConfiguration.getProxyPort());
    }

    @Test
    public void getClientConfigurationNoProxyInfoProvider() {
        ClientConfiguration clientConfiguration = S3Utils.getClientConfiguration(null);
        assertNull(clientConfiguration.getProxyHost());
        assertEquals(-1, clientConfiguration.getProxyPort());
    }

    @Test
    public void getClientConfigurationNoProxyInfo() {
        ClientConfiguration clientConfiguration = S3Utils.getClientConfiguration(this.proxyInfoProvider);
        assertNull(clientConfiguration.getProxyHost());
        assertEquals(-1, clientConfiguration.getProxyPort());
    }

    private Repository createRepository(String path) {
        return new Repository("foo", String.format("s3://dist.springsource.com%s", path));
    }
}
