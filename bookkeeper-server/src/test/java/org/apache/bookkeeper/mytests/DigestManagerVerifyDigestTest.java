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

import org.apache.bookkeeper.client.BKException.BKDigestMatchException;
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
public class DigestManagerVerifyDigestTest {
	private static DigestManager digestManager;
	private static int length = 52;
	private ByteBuf buffer;
	
	private int entryId;
	private ByteBufList dataReceived;
	private int ledgerId;
	private Object result;
	
	@Parameterized.Parameters
	public static Collection<Object[]> DigestManagerParameters() throws Exception {
		return Arrays.asList(new Object[][] {
			
			//ledgerId, dataReceived, entryId, result
			
			//Suite minimale
			{-1, null, -1, NullPointerException.class},
			{0, generateEntryWithDigest(length, 1, 1, DigestType.MAC), 0, BKDigestMatchException.class},
			{1, generateEntryWithDigest(length, 1, 0, DigestType.MAC), 0, BKDigestMatchException.class},
			{0, generateEntryWithDigest(length, 0, 0, DigestType.MAC), 1, BKDigestMatchException.class},
			{1, generateEntryWithDigest(length, 0, 1, DigestType.MAC), 1, BKDigestMatchException.class},
			
			//Aggiunti dopo miglioramento della test suite
			{1, generateBadEntryWithDigest(length, 0, 1), 1, BKDigestMatchException.class},
			{1, generateEntryWithDigest(length, 0, 1, DigestType.CRC32C), 1, BKDigestMatchException.class},
			
			//Mutation
			{1, generateEntryWithDigest(length, 1, 1, DigestType.CRC32C), 1, BKDigestMatchException.class}
		});
	}

	public DigestManagerVerifyDigestTest(int ledgerId, ByteBufList dataReceived, int entryId, Object result) {
		this.ledgerId = ledgerId;
		this.dataReceived = dataReceived;
		this.entryId = entryId;
		this.result = result;
	}


	@Before
	public void beforeTest() throws GeneralSecurityException {
		digestManager = DigestManager.instantiate(ledgerId, "testPassword".getBytes(), DigestType.MAC, UnpooledByteBufAllocator.DEFAULT, false);
		buffer = generateEntry(length);
	}
	
	@Test
	public void testVerifyDigestData() throws GeneralSecurityException {

		try {
			assertEquals(buffer, digestManager.verifyDigestAndReturnData(entryId, ByteBufList.coalesce(dataReceived)));
		} catch (Exception e) {
			assertEquals(result, e.getClass());
		}
	}

	//generate a valid entry and calculate the digest related to the buffer send, and return both buffer+digest
	private static ByteBufList generateEntryWithDigest(int length, int receivedLedgerId, int receivedEntryId, DigestType type) throws GeneralSecurityException {
		DigestManager digest = DigestManager.instantiate(receivedLedgerId, "testPassword".getBytes(), type, UnpooledByteBufAllocator.DEFAULT, false);
		ByteBuf byteBuf = generateEntry(length);
		ByteBufList byteBufList = digest.computeDigestAndPackageForSending(receivedEntryId, 0,  length, byteBuf);
		return byteBufList;
	}
	
	private static ByteBufList generateBadEntryWithDigest(int length, int receivedLedgerId, int receivedEntryId) {
		ByteBuf byteBuf = generateEntry(length);
		ByteBuf badHeader = Unpooled.buffer(length);
		return ByteBufList.get(badHeader, byteBuf);
	}

	private static ByteBuf generateEntry(int length) {
		//creates a ByteBuf with "length" bytes dimension, and writes "data" bytes on it
		byte[] data = "AnotherTwentyBytes!!".getBytes();
		ByteBuf bb = Unpooled.buffer(length);
		bb.writeBytes(data);
		return bb;
	}

} 