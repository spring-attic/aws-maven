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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.AWSCredentials;

/**
 * An implementation of the Maven Wagon interface that allows you to access the Amazon S3 service. URLs that reference
 * the S3 service should be in the form of <code>s3://bucket.name</code>. As an example
 * <code>s3://static.springframework.org</code> would put files into the <code>static.springframework.org</code> bucket
 * on the S3 service.
 * <p/>
 * This implementation uses the <code>username</code> and <code>passphrase</code> portions of the server authentication
 * metadata for credentials.
 * 
 * @author Ben Hale
 */
public final class SimpleStorageServiceWagon extends AbstractWagon {

    private S3Service service;

    private String bucket;

    private String basedir;

    public SimpleStorageServiceWagon() {
        super(false);
    }

    @Override
    protected void connectToRepository(Repository source, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider)
        throws AuthenticationException {
        try {
            Jets3tProperties jets3tProperties = new Jets3tProperties();
            if (proxyInfoProvider != null) {
                ProxyInfo proxyInfo = proxyInfoProvider.getProxyInfo("http");
                if (proxyInfo != null) {
                    jets3tProperties.setProperty("httpclient.proxy-autodetect", "false");
                    jets3tProperties.setProperty("httpclient.proxy-host", proxyInfo.getHost());
                    jets3tProperties.setProperty("httpclient.proxy-port", new Integer(proxyInfo.getPort()).toString());
                }
            }
            this.service = new RestS3Service(getCredentials(authenticationInfo), "mavens3wagon", null, jets3tProperties);
        } catch (S3ServiceException e) {
            throw new AuthenticationException("Cannot authenticate with current credentials", e);
        }
        this.bucket = source.getHost();
        this.basedir = getBaseDir(source);
    }

    @Override
    protected boolean doesRemoteResourceExist(String resourceName) {
        try {
            this.service.getObjectDetails(this.bucket, this.basedir + resourceName);
        } catch (ServiceException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void disconnectFromRepository() {
        // Nothing to do for S3
    }

    @Override
    protected void getResource(String resourceName, File destination, TransferProgress progress) throws ResourceDoesNotExistException,
        S3ServiceException, IOException {
        S3Object object;
        try {
            object = this.service.getObject(this.bucket, this.basedir + resourceName);
        } catch (S3ServiceException e) {
            throw new ResourceDoesNotExistException("Resource " + resourceName + " does not exist in the repository", e);
        }

        if (!destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            try {
                in = object.getDataInputStream();
            } catch (ServiceException se) {
                throw new IllegalStateException(se);
            }
            out = new TransferProgressFileOutputStream(destination, progress);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing possible at this point
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Nothing possible at this point
                }
            }
        }
    }

    @Override
    protected boolean isRemoteResourceNewer(String resourceName, long timestamp) throws ServiceException {
        StorageObject object = this.service.getObjectDetails(this.bucket, this.basedir + resourceName);
        return object.getLastModifiedDate().compareTo(new Date(timestamp)) < 0;
    }

    @Override
    protected List<String> listDirectory(String directory) throws Exception {
        S3Object[] objects = this.service.listObjects(this.bucket, this.basedir + directory, "");
        List<String> fileNames = new ArrayList<String>(objects.length);
        for (S3Object object : objects) {
            fileNames.add(object.getKey());
        }
        return fileNames;
    }

    @Override
    protected void putResource(File source, String destination, TransferProgress progress) throws S3ServiceException, IOException {
        buildDestinationPath(getDestinationPath(destination));
        S3Object object = new S3Object(this.basedir + destination);
        object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        object.setDataInputFile(source);
        object.setContentLength(source.length());

        InputStream in = null;
        try {
            this.service.putObject(this.bucket, object);

            in = new FileInputStream(source);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                progress.notify(buffer, length);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing possible at this point
                }
            }
        }
    }

    private void buildDestinationPath(String destination) throws S3ServiceException {
        S3Object object = new S3Object(this.basedir + destination + "/");
        object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
        object.setContentLength(0);
        this.service.putObject(this.bucket, object);
        int index = destination.lastIndexOf('/');
        if (index != -1) {
            buildDestinationPath(destination.substring(0, index));
        }
    }

    private String getDestinationPath(String destination) {
        return destination.substring(0, destination.lastIndexOf('/'));
    }

    private String getBaseDir(Repository source) {
        StringBuilder sb = new StringBuilder(source.getBasedir());
        sb.deleteCharAt(0);
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        return sb.toString();
    }

    private AWSCredentials getCredentials(AuthenticationInfo authenticationInfo) throws AuthenticationException {
        if (authenticationInfo == null) {
            return null;
        }
        String accessKey = authenticationInfo.getUserName();
        String secretKey = authenticationInfo.getPassphrase();
        if (accessKey == null || secretKey == null) {
            throw new AuthenticationException("S3 requires a username and passphrase to be set");
        }
        return new AWSCredentials(accessKey, secretKey);
    }
}
