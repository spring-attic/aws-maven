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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

final class TransferProgressFileOutputStream extends FileOutputStream {

    private final TransferProgress transferProgress;

    TransferProgressFileOutputStream(File file, TransferProgress transferProgress) throws FileNotFoundException {
        super(file);
        this.transferProgress = transferProgress;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        this.transferProgress.notify(new byte[] { (byte) b }, 1);
    }

    @Override
    public void write(byte b[]) throws IOException {
        super.write(b);
        this.transferProgress.notify(b, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        super.write(b, off, len);
        if (off == 0) {
            this.transferProgress.notify(b, len);
        } else {
            byte[] bytes = new byte[len];
            System.arraycopy(b, off, bytes, 0, len);
            this.transferProgress.notify(bytes, len);
        }
    }
}
