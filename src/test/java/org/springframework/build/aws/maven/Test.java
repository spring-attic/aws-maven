/*
 * Copyright 2013 the original author or authors.
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

import java.util.List;

import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.repository.Repository;

public class Test {

    public static void main(String[] args) throws WagonException {
        SimpleStorageServiceWagon wagon = new SimpleStorageServiceWagon();
        Repository repository = new Repository("spring-release", "s3://aws-maven.test.bucket/");
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        authenticationInfo.setUserName("0SCA5K7NAW330XGHMT02");
        authenticationInfo.setPassphrase("voXvAIMUsYCus5DymV7AS0NkqPU3UAvGktjD7hTs");

        wagon.connectToRepository(repository, null, null);

        List<String> files = wagon.getFileList("");
        System.out.println(files);
    }
}
