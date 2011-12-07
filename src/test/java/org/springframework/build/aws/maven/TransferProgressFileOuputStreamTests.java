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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

public final class TransferProgressFileOuputStreamTests {

    private static final int END_POSITION = 30;

    private static final int START_POSITION = 10;

    private static final int BIG_SIZE = 1024;

    private static final int SIZE = 20;

    private final File file = new File("target/test.txt");

    private final StubTransferProgress transferProgress = new StubTransferProgress();

    private final TransferProgressFileOutputStream outputStream;

    public TransferProgressFileOuputStreamTests() throws FileNotFoundException {
        this.outputStream = new TransferProgressFileOutputStream(this.file, this.transferProgress);
    }

    @After
    public void closeStream() {
        IoUtils.closeQuietly(this.outputStream);
    }

    @Test
    public void write() throws IOException {
        this.outputStream.write(1);
        assertArrayEquals(new byte[] { (byte) 1 }, this.transferProgress.getBuffer());
        assertEquals(1, this.transferProgress.getLength());
    }

    @Test
    public void readByteArray() throws IOException {
        byte[] buffer = new byte[SIZE];
        Arrays.fill(buffer, (byte) 1);
        this.outputStream.write(buffer);

        assertArrayEquals(buffer, this.transferProgress.getBuffer());
        assertEquals(SIZE, this.transferProgress.getLength());
    }

    @Test
    public void readyByteArrayLength() throws IOException {
        byte[] buffer = new byte[SIZE];
        Arrays.fill(buffer, (byte) 1);
        this.outputStream.write(buffer, 0, SIZE);

        assertArrayEquals(buffer, this.transferProgress.getBuffer());
        assertEquals(SIZE, this.transferProgress.getLength());
    }

    @Test
    public void readyByteArrayOffsetLength() throws IOException {
        byte[] buffer = new byte[BIG_SIZE];
        Arrays.fill(buffer, START_POSITION, END_POSITION, (byte) 1);
        this.outputStream.write(buffer, START_POSITION, SIZE);

        byte[] expected = new byte[SIZE];
        System.arraycopy(buffer, START_POSITION, expected, 0, SIZE);

        assertArrayEquals(expected, this.transferProgress.getBuffer());
        assertEquals(SIZE, this.transferProgress.getLength());
    }
}
