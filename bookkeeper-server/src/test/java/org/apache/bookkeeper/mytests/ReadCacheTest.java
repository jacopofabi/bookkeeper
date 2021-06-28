package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;


@RunWith(Parameterized.class)
public class ReadCacheTest {
    private static final ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
    private static final int entrySize = 1024;
	private static final int numEntries = 10;
    private static final int cacheSize = numEntries * entrySize;
    
	private static ReadCache cache;
	private long ledgerId;
	private long entryId;
	private ByteBuf entry;
	private int expected;
	private Class<? extends Exception> expectedException;
	
	
	public ReadCacheTest(long ledgerId, long entryId, ByteBuf entry, int expected, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.entry = entry;
		this.expected = expected;
		this.expectedException = expectedException;
        if(expectedException != null) {
        	//test is successful when "expectedException" is thrown
        	//test fails if a different or no exception is thrown (even if the assertEquals is correct)
        	exceptionRule.expect(this.expectedException);
        }
	}
	
    @Parameters
    public static Collection<Object[]> data() {
    	ByteBuf validEntry = allocator.buffer(entrySize);
    	ByteBuf invalidEntry = allocator.buffer(cacheSize+1);
    	validEntry.writerIndex(validEntry.capacity());
    	invalidEntry.writerIndex(invalidEntry.capacity());
    	
        return Arrays.asList(new Object[][] {
        		// ledgerId, entryId, entry, expectedNumberEntries, exception
	            {0, -1, null, 0, NullPointerException.class},
	            {-1, 0, validEntry, 0, IllegalArgumentException.class},
	            {1, 0, validEntry, 1, null},
	            {1, 1, invalidEntry, 0, IndexOutOfBoundsException.class}
        });
    }
    
    //rule that allows to verify that the code throws a specific exception
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
	
	@Before
	public void configure() {
		cache = new ReadCache(allocator, cacheSize);
	}
    
	@Test
	public void putTest() {
		System.out.println("------PUT--------");
		System.out.println("LedgerID: " + ledgerId + " entryID: " + entryId);
		System.out.println("Entry: " + entry);
		
		//start with no one entry, so that the total dimension given by the all entries is 0
        assertEquals(0, cache.count());
        assertEquals(0, cache.size());
        
        //put an entry into the cache, depending on the parameters we will have different behaviors
        cache.put(ledgerId, entryId, entry);
        assertEquals(cache.count(), expected);
	}
	
	@Test
	public void getTest() {
		System.out.println("------GET--------");
		
        if(expectedException != null) {
        	exceptionRule.expect(expectedException);
        }
        
        cache.put(ledgerId, entryId, entry);
		System.out.println("LedgerID: "+ledgerId+ " entryID: "+entryId);
		System.out.println("Entry: "+entry);
		System.out.println("CacheGet: "+cache.get(ledgerId, entryId));
		assertEquals(entry, cache.get(ledgerId, entryId));
	}
	
	@After
	public void cleanUp() {
		cache.close();
	}
}