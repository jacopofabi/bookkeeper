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
	private static int length = 5;
	private ByteBuf buffer;
	
	private int entryId;
	private ByteBufList dataReceived;
	private int ledgerId;
	private Object result;
	
	@Parameterized.Parameters
	public static Collection<Object[]> DigestManagerParameters() throws Exception {
		return Arrays.asList(new Object[][] {
			
			//entryId, dataReceived, ledgerId, result
			
			//Suite minimale
			{-1, null, -1, NullPointerException.class},
			{0, generateDataWithDigest(1, 1), 0, BKDigestMatchException.class},
			{1, generateDataWithDigest(1, 0), 0, BKDigestMatchException.class},
			{0, generateDataWithDigest(0, 0), 1, BKDigestMatchException.class},
			{1, generateDataWithDigest(0, 1), 1, BKDigestMatchException.class}
		});
	}

	public DigestManagerVerifyDigestTest(int entryId, ByteBufList dataReceived, int ledgerId, Object result) {
		this.entryId = entryId;
		this.dataReceived = dataReceived;
		this.ledgerId = ledgerId;
		this.result = result;
	}


	@Before
	public void beforeTest() throws GeneralSecurityException {
		digestManager = DigestManager.instantiate(ledgerId, "testPassword".getBytes(), DigestType.MAC, UnpooledByteBufAllocator.DEFAULT, false);
		buffer = generateEntryMutationBranch(length);
	}
	
	@Test
	public void testVerifyDigestData() throws GeneralSecurityException{

		try {
			assertEquals(buffer, digestManager.verifyDigestAndReturnData(entryId, dataReceived.coalesce(dataReceived)));
		} catch (Exception e) {
			assertEquals(result, e.getClass());
		}
	}

	private static ByteBufList generateDataWithDigest(int receivedLedgerId, int receivedEntryId) throws GeneralSecurityException {
		DigestManager digest = DigestManager.instantiate(receivedLedgerId, "testPassword".getBytes(), DigestType.MAC, UnpooledByteBufAllocator.DEFAULT, false);
		ByteBuf byteBuf = generateEntryMutationBranch(length);
		ByteBufList byteBufList = digest.computeDigestAndPackageForSending(receivedEntryId, 0,  length, byteBuf);
		return byteBufList;
	}
	
	private static ByteBufList generateBadDataWithDigest(int receivedLedgerId, int receivedEntryId, DigestType receivedType) throws GeneralSecurityException {
		ByteBuf byteBuf = generateEntryMutationBranch(length);
		ByteBuf badHeader = Unpooled.buffer(length);
		return ByteBufList.get(badHeader, byteBuf);
	}

	private static ByteBuf generateEntryMutationBranch(int length) {
		byte[] data = "testString".getBytes();
		ByteBuf bb = Unpooled.buffer(27+length);
		bb.writeBytes(data);
		return bb;
	}

} 