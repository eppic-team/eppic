package ch.systemsx.sybit.crkwebui.server.commons.util.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFileCache {
	private static final Logger logger = LoggerFactory.getLogger(TestFileCache.class);

	//	private class BooleanBean {
	//		public boolean val;
	//		public BooleanBean(boolean initial) {
	//			val = initial;
	//		}
	//	}
	//	
	private class SimpleCallable implements Callable<String> {
		public final String val;
		public boolean calculated;
		public SimpleCallable(String val) {
			this.val = val;
			this.calculated = false;
		}

		@Override
		public String call() throws Exception {
			logger.info("Computing '{}'",val);
			calculated = true;
			return val;
		}
	}

	@Test
	public void testCaching() throws InterruptedException, ExecutionException, IOException {
		// create new instance to avoid singleton interactions
		FileCache cache = new FileCache();
		cache.setCapacity(1); // One at a time
		cache.purge(); // purge any pre-existing cache files

		String val;
		SimpleCallable computer;
		String result;

		// First item should be calculated
		val = "one";
		String one = tmp(val);
		computer = new SimpleCallable(val);
		result = cache.getString(one, computer);
		assertEquals("Wrong result",val,result);
		assertTrue("Should have been calculated",computer.calculated);

		// Get cached result
		computer = new SimpleCallable(val);
		result = cache.getString(one, computer);
		assertEquals("Wrong result",val,result);
		assertFalse("Should have been cached",computer.calculated);

		// Test expiration
		val = "two";
		String two = tmp(val);
		computer = new SimpleCallable(val);
		result = cache.getString(two, computer);
		assertEquals("Wrong result",val,result);
		assertTrue("Should have been calculated",computer.calculated);
		assertEquals("Wrong cache size",1,cache.size());

		// Read one from disk
		val = "one";
		computer = new SimpleCallable(val);
		result = cache.getString(one, computer);
		assertEquals("Wrong result",val,result);
		assertFalse("Should have been cached",computer.calculated);
		assertEquals("Wrong cache size",1,cache.size());

		// delete two
		val = "two";
		cache.delete(two);
		assertFalse( two+" exists", new File(two).exists());

		// Recalculate two
		computer = new SimpleCallable(val);
		result = cache.getString(two, computer);
		assertEquals("Wrong result",val,result);
		assertTrue("Should have been calculated",computer.calculated);
		assertEquals("Wrong cache size",1,cache.size());

		// Test clearing
		// Use cache.getFile() initially to wait for writing to complete
		assertTrue( one+" exists", cache.getFile(one).exists());
		assertTrue( two+" exists", cache.getFile(two).exists());
		cache.clear();
		assertEquals("Wrong cache size",0,cache.size());
		assertTrue( one+" exists", new File(one).exists());
		assertTrue( two+" exists", new File(two).exists());

		// Test purging
		cache.purge();
		assertEquals("Wrong cache size",0,cache.size());
		assertFalse( one+" exists", new File(one).exists());
		assertFalse( two+" exists", new File(two).exists());

	}

	private String tmp(String s) throws IOException {
		File tmp = File.createTempFile("TestFileCache-"+s, ".txt");
		tmp.deleteOnExit();
		return tmp.toString();
	}
}
