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

import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;

final class NullProtectingProxyInfoProvider implements ProxyInfoProvider {

    private final ProxyInfo proxyInfo;

    NullProtectingProxyInfoProvider(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    @Override
    public ProxyInfo getProxyInfo(String protocol) {
        if ((protocol == null) || (this.proxyInfo == null) || protocol.equalsIgnoreCase(this.proxyInfo.getType())) {
            return this.proxyInfo;
        } else {
            return null;
        }
    }
}