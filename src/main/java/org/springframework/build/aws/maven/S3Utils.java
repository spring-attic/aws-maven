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
import org.apache.maven.wagon.repository.Repository;

import com.amazonaws.ClientConfiguration;

final class S3Utils {

    private S3Utils() {
    }

    static String getBucketName(Repository repository) {
        return repository.getHost();
    }

    static String getBaseDirectory(Repository repository) {
        StringBuilder sb = new StringBuilder(repository.getBasedir()).deleteCharAt(0);

        if ((sb.length() != 0) && (sb.charAt(sb.length() - 1) != '/')) {
            sb.append('/');
        }

        return sb.toString();
    }

    static ClientConfiguration getClientConfiguration(ProxyInfoProvider proxyInfoProvider) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        if (proxyInfoProvider != null) {
            ProxyInfo proxyInfo = proxyInfoProvider.getProxyInfo("s3");
            if (proxyInfo != null) {
                clientConfiguration.withProxyHost(proxyInfo.getHost()).withProxyPort(proxyInfo.getPort());
            }
        }

        return clientConfiguration;
    }
}
