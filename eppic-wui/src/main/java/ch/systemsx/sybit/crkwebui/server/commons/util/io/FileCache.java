package ch.systemsx.sybit.crkwebui.server.commons.util.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread-safe caching of files.
 * 
 * <p>Conceptually this can be thought of as a simple key-value cache, where
 * the unique key happens to be the path to a file and the value can be returned
 * as a String, Reader, File handle, etc. Behind the hood, values are cached to
 * memory (for a limited number of hits) and to disk.
 * 
 * <p>All paths referenced in the FileCache should be considered to be owned by
 * the cache. Although the {@link #getFile(String, Callable)} method provides
 * access to the underlying object, this should not be used to write or delete
 * the file (use {@link #delete(String)} instead). This also means that only a
 * single FileCache should be used with each path. To enforce this, it is
 * recommended that the {@link #getInstance() singleton instance} be used
 * rather than constructing a cache explicitly.
 * @author Spencer Bliven
 */
public class FileCache {
	private static final Logger logger = LoggerFactory.getLogger(FileCache.class);

	/**
	 * Represents the cached contents of a file. Only one CacheFile should exist
	 * for any given path. Also, it is assumed that this class owns the underlying
	 * file (e.g. it will not be modified by other processes during the life of
	 * the CacheFile object.
	 * 
	 * <p>This class is not internally synchronized, so external synchronization
	 * (e.g. on FileCache.memcache) is required for all access.
	 * @author Spencer Bliven
	 *
	 */
	protected static class CacheFile implements Comparable<CacheFile> {
		
		// auto-incrementing global serial number. Synchronize on the class object for access.
		private static long nextSerial = 0;
		// serial number at last time of access
		private long serial;
		
		private final Future<String> contents;
		private final Future<?> writer;
		private final File file;
		public CacheFile(File file, Callable<String> contents, ExecutorService executor) {
			// assign next serial number
			synchronized (CacheFile.class) {
				serial = nextSerial;
				nextSerial++;
			}
			this.file = file;

			// if already cached, don't recalculate
			if(fileExists(file)) {
				contents = () -> FileContentReader.readContentOfFile(file, false);
			}
			// begin reading contents
			this.contents = executor.submit(contents);
			// begin writing contents
			if(!fileExists(file)) {
				Runnable write = () -> {
					try
					{
						String str = this.contents.get();
						try ( FileWriter writer = new FileWriter(file) ) {
							writer.write(str);
						}
					} catch (IOException|InterruptedException|ExecutionException e) {
						logger.error("Error writing {}",file,e);
					}
				};
				this.writer = executor.submit(write);
			} else {
				this.writer = null;
			}
		}
		@Override
		public int compareTo(CacheFile o) {
			return Long.compare(this.serial, o.serial);
		}
		/**
		 * @return The contents of this file
		 * @throws CancellationException if the computation was cancelled
		 * @throws ExecutionException if the computation threw an exception
		 * @throws InterruptedException if the current thread was interrupted while waiting
		 */
		public String get() throws CancellationException, InterruptedException, ExecutionException {
			touch();
			return contents.get();
		}
		public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			touch();
			return contents.get(timeout, unit);
		}
		public File getFile() throws InterruptedException, ExecutionException {
			if(writer != null) {
				writer.get();
			}
			assert file.exists();//File should have been written before here
			return file;
		}
		public String getFilename() {
			return file.toString();
		}
		public void delete() {
			if(writer != null)
				writer.cancel(true);
			contents.cancel(true);
			file.delete();
		}
		/** Marks this file as recently accessed for purposes of cache recency
		 */
		public void touch() {
			synchronized (CacheFile.class) {
				if( serial == nextSerial) {
					return; // already newest file
				}
				serial = nextSerial;
				nextSerial++;
			}
		}
		
		/**
		 * Wait for the file to finish writing before finalizing
		 * @throws Throwable
		 */
		@Override
		protected void finalize() throws Throwable {
			boolean completed = writer == null || writer.isDone();
			while(!completed) {
				logger.info("Waiting to finish writing {}",this.file);
				try {
					writer.get(10, TimeUnit.SECONDS);
					completed = true;
				} catch(TimeoutException e) {}
			}
			super.finalize();
		}
		/**
		 * Get whether the contents have finished writing to disk
		 * @return
		 */
		public boolean isSynced() {
			if(writer != null && writer.isDone() && !writer.isCancelled() ) {
				return true;
			}
			return file.exists() && file.length()>0;
		}
	}
		

	private ExecutorService executor;

	// access the following 3 members are synchronized on memcache
	// map of all files currently in memory
	private Map<String, CacheFile> memcache;
	// queue of memcache values, to allow easy expiration of oldest entries
	private PriorityQueue<CacheFile> queue;
	// Set of "safe" files, used to implement purging. null indicates all are safe.
	private Set<String> notpurged = null;
	
	private int capacity;
	
	/**
	 * For most situations, use {@link #getInstance()} instead
	 */
	public FileCache() { // package visibility for testing only
		executor = Executors.newCachedThreadPool();
		memcache = new HashMap<>();
		queue = new PriorityQueue<>();
		setCapacity(16);
	}
	
	// Singleton instance
	private static FileCache instance = null;
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static FileCache getInstance() {
		if(instance == null) {
			instance = new FileCache();
		}
		return instance;
	}
	
	/**
	 * Checks if the path has been calculated, either in memory on on disk
	 * @param path
	 * @return
	 */
	public boolean isCached(String path) {
		synchronized(memcache) {
			if( memcache.containsKey(path) )
				return true;
			File file = new File(path);
			if(fileExists(file) ) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Checks if the path has been calculated and is still in memory
	 * @param path
	 * @return
	 */
	public boolean isCachedInMemory(String path) {
		synchronized(memcache) {
			return memcache.containsKey(path);
		}

	}
	
	private static boolean fileExists(File file) {
		return file.exists() && file.length()>0;
	}

	protected void removeExpired() {
		synchronized( memcache ) {
			List<CacheFile> stillwriting = new LinkedList<>();
			while(queue.size() > capacity) {
				CacheFile expired = queue.remove(); //lowest serial
				if( !expired.isSynced()) {
					// save actively writing cases to be pushed back on the queue
					stillwriting.add(expired);
				} else {
					memcache.remove(expired.getFilename());
				}
			}
			//re-add cases that are still writing
			//Note that this may cause memcache to be temporarily over-capacity
			queue.addAll(stillwriting);
		}
	}
	
	protected CacheFile getCacheFile(String path, Callable<String> contents) {
		CacheFile cacheFile;
		if( memcache.containsKey(path) ) {
			cacheFile = memcache.get(path);
		} else {
			File file = new File(path);
			cacheFile = new CacheFile(file, contents, executor);
			memcache.put(path, cacheFile);
			queue.add(cacheFile);
			if(notpurged != null) {
				notpurged.add(path);
			}
		}
		return cacheFile;
	}
	/**
	 * Get the String associated with a particular key, either by computing it
	 * or by getting the cached value (from memory or disk).
	 * <p>
	 * The <tt>contents</tt> parameter should be idempotent with respect to the
	 * <tt>path</tt> (e.g. multiple calls with the same path should provide
	 * {@link Callable}s that would produce the same strings). Otherwise the cache may
	 * be out of date.
	 * <p>
	 * This function blocks while the result is calculated.
	 * @param path Path to the cache location
	 * @param contents Method to generate the contents, if needed
	 * @return A (possibly cached) value equivalent to calling <tt>contents.call()</tt>
	 * @throws ExecutionException if the computation threw an exception
	 * @throws InterruptedException if the computation thread was interrupted while waiting
	 */
	public String getString(String path, Callable<String> contents) throws InterruptedException, ExecutionException {
		synchronized( memcache ) {
			CacheFile cacheFile = getCacheFile(path, contents);
			String contentsStr = cacheFile.get();
			removeExpired();
			return contentsStr;
		}
	}
	/**
	 * Get the String associated with a particular key, either by computing it
	 * or by getting the cached value (from memory or disk).
	 * <p>
	 * The <tt>contents</tt> parameter should be idempotent with respect to the
	 * <tt>path</tt> (e.g. multiple calls with the same path should provide
	 * {@link Callable}s that would produce the same strings). Otherwise the cache may
	 * be out of date.
	 * <p>
	 * This function blocks while the result is calculated.
	 * @param path Path to the cache location
	 * @return A cached value for the paths contents, or null if the contents aren't cached.
	 * @throws ExecutionException if the computation threw an exception
	 * @throws InterruptedException if the computation thread was interrupted while waiting
	 */
	public String getString(String path) throws InterruptedException, ExecutionException {
		synchronized( memcache) {
			if( !memcache.containsKey(path) ) {
				return null;
			}
			CacheFile cacheFile = memcache.get(path);
			String contents = cacheFile.get();
			return contents;
		}
	}

	/**
	 * Get a file for the specified path location. Blocks until the contents
	 * are fully written.
	 * 
	 * <p>Paths should never be directly used as filenames (<tt>new File(path)</tt>)
	 * since no guarentees can be made in that case about the existence or contents
	 * of the file. Likewise, the returned File object should not be written or
	 * deleted (use {@see #delete(String)} for that).
	 * @param path
	 * @param contents
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public File getFile(String path, Callable<String> contents) throws InterruptedException, ExecutionException {
		synchronized( memcache ) {
			CacheFile cacheFile = getCacheFile(path, contents);
			File contentsFile = cacheFile.getFile();
			removeExpired();
			return contentsFile;
		}
	}
	public File getFile(String path) throws InterruptedException, ExecutionException {
		synchronized( memcache) {
			File file;
			if( memcache.containsKey(path) ) {
				CacheFile cacheFile = memcache.get(path);
				file = cacheFile.getFile();
			} else {
				file = new File(path);
			}
			if(fileExists(file)) {
				if(notpurged != null) {
					notpurged.add(path);
				}
				return file;
			}
		}
		return null;
	}
	
	/**
	 * Ensure that all files are synced to disk
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void flush() throws InterruptedException, ExecutionException {
		synchronized(memcache) {
			for( CacheFile f : memcache.values()) {
				f.getFile();
			}
		}
	}
	/**
	 * Get the maximum number of recent files to keep in memory
	 * @return
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Set the number of recent files to keep in memory
	 * @param capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	/**
	 * 
	 * @return The number of files cached in memory
	 */
	int size() {
		return memcache.size();
	}
	
	/**
	 * Clears the in-memory cache. Files on disk are still retained and will be
	 * reused.
	 * @see #purge()
	 */
	public void clear() {
		synchronized(memcache) {
			memcache.clear();
			queue.clear();
		}
	}
	
	/**
	 * Purge a particular file from the cache, both on disk and in memory
	 * @param path
	 */
	public void delete(String path) {
		synchronized(memcache) {
			if(memcache.containsKey(path)) {
				CacheFile file = memcache.get(path);
				file.delete();
				memcache.remove(path);
				queue.remove(file);
			} else {
				File file = new File(path);
				file.delete();
			}
			notpurged.remove(path);
		}
	}
	
	/**
	 * Marks all files as out-of-date and in need of recomputation.
	 * Files currently cached in memory will be deleted, but older items may
	 * need to be cleaned up manually on disk.
	 * 
	 * <p>Call purge() on an empty FileCache to enable tracking of all files.
	 * This guarantees that subsequent calls to purge will remove all files.
	 */
	public void purge() {
		synchronized(memcache) {
			// delete known files
			for( CacheFile cacheFile : memcache.values()) {
				cacheFile.delete();
			}
			if(notpurged != null) {
				for( String filename : notpurged) {
					File file = new File(filename);
					file.delete();
				}
			}
			memcache.clear();
			queue.clear();
			notpurged = new HashSet<>();
		}
	}


}
