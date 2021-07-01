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
public class ReadCacheGetTest {
	private static ReadCache cache;
	
	//Cache data
    private static final ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
    private static final int ENTRY_SIZE = 1024;
	private static final int NUM_ENTRIES = 10;
    private static final int CACHE_SIZE = NUM_ENTRIES * ENTRY_SIZE;
    
    //Test parameters
	private long ledgerId;
	private long entryId;
	private ByteBuf expectedEntry;
	private Class<? extends Exception> expectedException;
	
	//Test entry data
	private static final int LEDGER_ID = 1;
	private static final int ENTRY_ID = 1;
	private static final ByteBuf ENTRY = allocator.buffer(ENTRY_SIZE);
	private static final ByteBuf NULL_ENTRY = null;
	
	
	public ReadCacheGetTest(long ledgerId, long entryId, ByteBuf expectedEntry, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.expectedEntry = expectedEntry;
		this.expectedException = expectedException;
		
        if(expectedException != null) {
        	exceptionRule.expect(this.expectedException);
        }
	}
	
    @Parameters
    public static Collection<Object[]> data() {
    	ByteBuf validEntry = allocator.buffer(ENTRY_SIZE);
    	ByteBuf invalidEntry = allocator.buffer(CACHE_SIZE+1);
    	validEntry.writerIndex(validEntry.capacity());
    	invalidEntry.writerIndex(invalidEntry.capacity());
    	
        return Arrays.asList(new Object[][] {
    		// ledgerId, entryId, expectedEntry, expectedException
        	
    		//Suite minimale
        	{-1, 0, NULL_ENTRY, IllegalArgumentException.class},	//ledgerId must be greater than 0
            {0, -1, NULL_ENTRY, null},
            {1, 0, NULL_ENTRY, null},
            {1, 1, ENTRY, null}										//we found only an entry in the ReadCache, with ledgerId=1 and entryId=1
        });
    }
    
    
    //rule that allows to verify that the code throws a specific exception
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
	
    
    //configurazione dell'ambiente di esecuzione, si istanzia una ReadCache e si inserisce una entry con ledgerId=1 ed entryId=1
	@Before
	public void configure() {
		cache = new ReadCache(allocator, CACHE_SIZE);
		cache.put(LEDGER_ID, ENTRY_ID, ENTRY);
	}
	
	@Test
	public void getTest() {
		ByteBuf actualEntry = cache.get(ledgerId, entryId);
		assertEquals(expectedEntry, actualEntry);
	}
	
	@After
	public void cleanUp() {
		cache.close();
	}
}