package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;
import org.junit.Before;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;


public class ReadCacheTest1 {
	ReadCache cache;
	ByteBuf entry;
	
	@Before
	public  void configure() {
		cache = new ReadCache(UnpooledByteBufAllocator.DEFAULT, 10 * 1024);
		entry = Unpooled.wrappedBuffer(new byte[1024]);
	}
	
	@Test
	public void initializeReadCacheTest() {
        assertEquals(0, cache.count());
        assertEquals(0, cache.size());
	}
	
	@Test
	public void putTest() {
        cache.put(1, 0, entry);				// Inseriamo una entry in cache con LEDGER_ID = 1, ENTRY_ID = 0
        assertEquals(1, cache.count());		// Vediamo che abbiamo esattamente un'entry
        assertEquals(1024, cache.size());	// Vediamo che la dimensione totale è data dalla dimensione dell'unica entry aggiunta
        
        //TODO fare ciclo con insieme di entry passate con parametrized
	}
}