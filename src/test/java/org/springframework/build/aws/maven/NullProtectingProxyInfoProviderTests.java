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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.wagon.proxy.ProxyInfo;
import org.junit.Test;

public final class NullProtectingProxyInfoProviderTests {

    private final ProxyInfo proxyInfo = mock(ProxyInfo.class);

    private final NullProtectingProxyInfoProvider proxyInfoProvider = new NullProtectingProxyInfoProvider(this.proxyInfo);

    @Test
    public void getProxyInfoNullProtocol() {
        assertSame(this.proxyInfo, this.proxyInfoProvider.getProxyInfo(null));
    }

    @Test
    public void getProxyInfoNullProxy() {
        assertNull(new NullProtectingProxyInfoProvider(null).getProxyInfo("foo"));
    }

    @Test
    public void getProxyInfoMatchingProtocol() {
        when(this.proxyInfo.getType()).thenReturn("FOO");
        assertSame(this.proxyInfo, this.proxyInfoProvider.getProxyInfo("foo"));
    }

    @Test
    public void getProxyInfoNonMatchingProtocol() {
        when(this.proxyInfo.getType()).thenReturn("FOO");
        assertNull(this.proxyInfoProvider.getProxyInfo("bar"));
    }

}
