package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.bookkeeper.bookie.storage.ldb.EntryLocationIndexTest;
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
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;


@RunWith(Parameterized.class)
public class ReadCacheTest {
    private static final ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
    private static final int entrySize = 1024;
    private static final int cacheCapability = 10 * entrySize;
    
	private static ReadCache cache;
	private long ledgerId;
	private long entryId;
	private ByteBuf entry;
	private boolean expected;
	private Class<? extends Exception> expectedException;
	
	
	public ReadCacheTest(long ledgerId, long entryId, ByteBuf entry, boolean expected, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.entry = entry;
		this.expected = expected;
		this.expectedException = expectedException;
	}
	
	@Before
	public  void configure() {
		cache = new ReadCache(allocator, cacheCapability);
	}
	
    @Parameters
    public static Collection<Object[]> data() {
    	ByteBuf validEntry = allocator.buffer(entrySize);
    	ByteBuf invalidEntry = allocator.buffer(11*entrySize);
    	validEntry.writerIndex(validEntry.capacity());
    	invalidEntry.writerIndex(invalidEntry.capacity());
    	
        return Arrays.asList(new Object[][] {
	            {0, -1, null, false, NullPointerException.class},
	            {-1, 0, validEntry, false, IllegalArgumentException.class},
	            {1, 0, validEntry, true, null},
	            {1, 1, invalidEntry, false, IndexOutOfBoundsException.class}
        });
    }
    
    //rule that allows you to verify that the code throws a specific exception
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
	@Test
	public void readCachePutTest() {
		//start with no one entry, so that the total dimension given by the all entries is 0
        assertEquals(0, cache.count());
        assertEquals(0, cache.size());
        
        if(expectedException != null) {
        	//test is successful when "expectedException" is thrown
        	//test fails if a different or no exception is thrown (even if the assertEquals is correct)
        	exceptionRule.expect(expectedException);
        }
        
        //put an entry into the cache, depending on the parameters we will have different behaviors
        cache.put(ledgerId, entryId, entry);
        if(cache.count() > 0) {
        	assertEquals(true, expected);
        }
        else {
        	assertEquals(false, expected);
        }
	}
	
	@After
	public void cleanUp() {
		cache.close();
	}
}