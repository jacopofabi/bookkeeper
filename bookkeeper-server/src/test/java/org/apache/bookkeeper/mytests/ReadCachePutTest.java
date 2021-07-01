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
public class ReadCachePutTest {
	private static ReadCache cache;
	
	//Cache data
    private static final ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
    private static final int ENTRY_SIZE = 1024;
	private static final int NUM_ENTRIES = 10;
    private static final int CACHE_SIZE = NUM_ENTRIES * ENTRY_SIZE;
    
    //Test parameters
	private long ledgerId;
	private long entryId;
	private ByteBuf entry;
	private int expected;
	private Class<? extends Exception> expectedException;
	
	//Test entries
	private static ByteBuf validEntry;
	private static ByteBuf invalidEntry;
	private static ByteBuf nullEntry;
	
	
	public ReadCachePutTest(long ledgerId, long entryId, ByteBuf entry, int expected, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.entry = entry;
		this.expected = expected;
		this.expectedException = expectedException;
		
    	//test is successful when "expectedException" is thrown
    	//test fails if a different or no exception is thrown (even if the assertEquals is correct)
        if(expectedException != null) {
        	exceptionRule.expect(this.expectedException);
        }
	}
	
    @Parameters
    public static Collection<Object[]> data() {
    	validEntry = allocator.buffer(ENTRY_SIZE);
    	invalidEntry = allocator.buffer(CACHE_SIZE+1);
    	nullEntry = null;
    	
    	validEntry.writerIndex(validEntry.capacity());
    	invalidEntry.writerIndex(invalidEntry.capacity());
    	
        return Arrays.asList(new Object[][] {
    		// ledgerId, entryId, entry, expectedNumberEntries, exception
    	
    		//Suite minimale
            {0, -1, nullEntry, 0, NullPointerException.class},
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
		cache = new ReadCache(allocator, CACHE_SIZE);
	}
    
	@Test
	public void putTest() {
        //put an entry into the cache, depending on the parameters we will have different behaviors
        cache.put(ledgerId, entryId, entry);
        assertEquals(cache.count(), expected);
	}
	
	@After
	public void cleanUp() {
		cache.close();
	}
}