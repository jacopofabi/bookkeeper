/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.bookkeeper.client.api.DigestType;
import org.apache.bookkeeper.proto.checksum.DigestManager;
import org.apache.bookkeeper.util.ByteBufList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

@RunWith(Parameterized.class)
public class DigestManagerComputeDigestTest {
	private static DigestManager digestManager;
	private static long ledgerId = 1;
	private ByteBuf testEntry;
	
	private static long lastAddConfirmed;
	private static long entryId;
	private long length;
	private boolean useV2Protocol;
	private ByteBuf data;
	private Object result;
	

	@Parameterized.Parameters
	public static Collection<Object[]> BufferedChannelParameters() throws Exception {
		return Arrays.asList(new Object[][] {
			
			//lastAddConfirmed, entryId, length, data, useV2Protocol, result
			
			//Suite minimale, migliorata tramite l'aggiunta del parametro useV2Protocol
			{-1,-1,0,null,false,NullPointerException.class},
			{0,1,0,generateEntry(0),true,null},
			{1,2,1,generateEntry(1),false,null},

			//Aggiunti dopo miglioramento della test suite
			{0,1,0,generateWrappedEntry(0),true,null},
			{0,1,0,generateCompositeByteBufEntry(0),true,null}
		});
	}

	public DigestManagerComputeDigestTest(long lastAddConfirmed, long entryId, long length, ByteBuf data, boolean useV2Protocol, Object result) {
		this.lastAddConfirmed = lastAddConfirmed;
		this.entryId = entryId;
		this.length = length;
		this.data = data;
		this.useV2Protocol = useV2Protocol;
		this.result = result;
	}

	@Before
	public void configure() throws GeneralSecurityException {
		digestManager = DigestManager.instantiate(ledgerId, "testPassword".getBytes(), DigestType.MAC, UnpooledByteBufAllocator.DEFAULT, useV2Protocol);
		testEntry = generateEntry((int)length);
	}

	@Test
	public void testComputeDigestData() {

		try {
			// Assert that the buffer of data contained in the byteBuf is equal to what sent
			ByteBufList byteBuf = digestManager.computeDigestAndPackageForSending(entryId, lastAddConfirmed, length, data);
			assertEquals(testEntry.readLong(), byteBuf.getBuffer(1).readLong());
		} catch (Exception e) {
			assertEquals(result, e.getClass());
		}
	}

	//method used to generate a buffer related to the single entry, filled with the informations of the entry like indicated in the documentation
	private static ByteBuf generateEntry(int length) {
		byte[] data = new byte[length];
		ByteBuf byteBuffer = Unpooled.buffer(1024);
		byteBuffer.writeLong(ledgerId); // Ledger
		byteBuffer.writeLong(entryId); // Entry
		byteBuffer.writeLong(lastAddConfirmed); // LAC
		byteBuffer.writeLong(length); // Length
		byteBuffer.writeBytes(data);
		return byteBuffer;
	}

	//method used to wrap a byte buffer into a new buffer
	private static ByteBuf generateWrappedEntry(int length) {
		byte[] data = new byte[length];
		ByteBuf byteBuffer = Unpooled.buffer(1024);
		byteBuffer.writeLong(ledgerId); // Ledger
		byteBuffer.writeLong(entryId); // Entry
		byteBuffer.writeLong(lastAddConfirmed); // LAC
		byteBuffer.writeLong(length); // Length
		byteBuffer.writeBytes(data);
		return Unpooled.wrappedBuffer(byteBuffer);
	}
	
	//method used to create a CompositeByteBuf to increase code coverage
	private static ByteBuf generateCompositeByteBufEntry(int length) {
		byte[] data = new byte[length];
		ByteBuf byteBuffer = Unpooled.compositeBuffer();
		byteBuffer.writeLong(ledgerId); // Ledger
		byteBuffer.writeLong(entryId); // Entry
		byteBuffer.writeLong(lastAddConfirmed); // LAC
		byteBuffer.writeLong(length); // Length
		byteBuffer.writeBytes(data);
		return byteBuffer;
	}
}