package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.junit.Test;

import br.ufmg.dcc.vod.ncrawler.queue.MemoryMappedQueue;
import br.ufmg.dcc.vod.ncrawler.queue.Serializer;


public class MemoryMappedQueueTest extends TestCase {

	@Test
	public void testQueuePutGet() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(1, q.size());
		assertEquals("a", q.take());
		assertEquals(0, q.size());
		
		f.delete();
	}

	@Test
	public void testQueuePutGet2() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		
		assertEquals(12, q.getStart());
		assertEquals(12, q.getEnd());
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(12, q.getStart());
		assertEquals(14, q.getEnd()); //17 because the size is also written. 1 bytes containing the size of the information and another containing the informatation
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(12, q.getStart());
		assertEquals(16, q.getEnd());
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(12, q.getStart());
		assertEquals(18, q.getEnd());
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(12, q.getStart());
		assertEquals(20, q.getEnd());
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(12, q.getStart());
		assertEquals(22, q.getEnd());
		assertEquals(5, q.size());
		
		assertEquals("a", q.take());
		assertEquals(4, q.size());
		assertEquals("b", q.take());
		assertEquals(3, q.size());
		assertEquals("c", q.take());
		assertEquals(2, q.size());
		assertEquals("d", q.take());
		assertEquals(1, q.size());
		assertEquals("e", q.take());
		assertEquals(0, q.size());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet3() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);

		try {
			q.take();
			fail();
		} catch (NullPointerException e) {}
		
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(1, q.size());
		assertEquals("a", q.take());
		assertEquals(0, q.size());
		
		try {
			q.take();
			fail();
		} catch (NullPointerException e) {}
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGet4() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		
		assertEquals(12, q.getStart());
		assertEquals(12, q.getEnd());
		assertEquals(0, q.size());
		q.put("a");
		assertEquals(12, q.getStart());
		assertEquals(14, q.getEnd()); //17 because the size is also written. 1 bytes containing the size of the information and another containing the informatation
		assertEquals(1, q.size());
		
		q.put("b");
		assertEquals(12, q.getStart());
		assertEquals(16, q.getEnd());
		assertEquals(2, q.size());
		
		q.put("c");
		assertEquals(12, q.getStart());
		assertEquals(18, q.getEnd());
		assertEquals(3, q.size());
		
		q.put("d");
		assertEquals(12, q.getStart());
		assertEquals(20, q.getEnd());
		assertEquals(4, q.size());
		
		q.put("e");
		assertEquals(12, q.getStart());
		assertEquals(22, q.getEnd());
		assertEquals(5, q.size());
		
		assertEquals("a", q.take());
		assertEquals(4, q.size());
		assertEquals("b", q.take());
		assertEquals(3, q.size());
		
		q.put("f");
		q.put("g");
		
		assertEquals(5, q.size());
		assertEquals("c", q.take());
		assertEquals("d", q.take());
		assertEquals("e", q.take());
		assertEquals("f", q.take());
		assertEquals("g", q.take());
		assertEquals(0, q.size());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure1() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		q.put("avcl");
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(1, wrap.getInt());
		assertEquals(1, q.size());
		
		assertEquals(12, wrap.getInt()); //starts at 12 bytes
		assertEquals(12, q.getStart()); 
		
		assertEquals("avcl".getBytes().length + 12 + 1, wrap.getInt()); 
		assertEquals("avcl".getBytes().length + 12 + 1, q.getEnd());
		
		assertEquals(4, wrap.get()); //size of info
		
		byte[] r = new byte[4];
		wrap.get(r);
		assertEquals("avcl", new String(r));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure2() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		q.put("a");
		
		q.take();
		q.shutdownAndSync();
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(0, wrap.getInt());
		assertEquals(0, q.size());
		
		int e = "a".getBytes().length + 12 + 1;
		assertEquals(e, wrap.getInt()); //starts at 14 bytes, element deleted but remains on file (2bytes)
		assertEquals(e, q.getStart()); 
		
		assertEquals(e, wrap.getInt()); 
		assertEquals(e, q.getEnd());
		
		//Garbage data
		assertEquals(1, wrap.get());
		assertEquals("a", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure3() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		q.put("a");
		q.put("v");
		q.put("c");
		q.put("l");
		
		q.take();
		q.take();
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(2, wrap.getInt());
		assertEquals(2, q.size());
		
		assertEquals(16, wrap.getInt()); //starts at 16 bytes
		assertEquals(16, q.getStart()); 
		
		assertEquals(20, wrap.getInt()); 
		assertEquals(20, q.getEnd());
		
		//Garbage
		assertEquals(1, wrap.get()); //size of info
		assertEquals("a", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("v", new String(new byte[]{wrap.get()}));
		
		//Real Data
		assertEquals(1, wrap.get()); //size of info
		assertEquals("c", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("l", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure4() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		q.put("a");
		q.put("v");
		q.put("c");
		q.put("l");
		
		q.take();
		q.take();
		q.put("z");
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(3, wrap.getInt());
		assertEquals(3, q.size());
		
		assertEquals(16, wrap.getInt()); //starts at 16 bytes
		assertEquals(16, q.getStart()); 
		
		assertEquals(22, wrap.getInt()); 
		assertEquals(22, q.getEnd());
		
		//Garbage
		assertEquals(1, wrap.get()); //size of info
		assertEquals("a", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("v", new String(new byte[]{wrap.get()}));
		
		//Real Data
		assertEquals(1, wrap.get()); //size of info
		assertEquals("c", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("l", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("z", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	@Test
	public void testQueuePutGetFileStructure5() throws FileNotFoundException, IOException {
		File f = File.createTempFile("temp", "test");
		f.deleteOnExit();
		
		SS ss = new SS();
		MemoryMappedQueue<String> q = new MemoryMappedQueue<String>(f, ss, 1024 * 1024);
		
		q.put("a");
		q.put("v");
		q.put("c");
		q.put("l");
		
		q.take();
		q.take();
		q.put("z");
		q.put("e");
		q.put("b");
		q.shutdownAndSync();
		
		
		FileInputStream fs = new FileInputStream(f);
		byte[] b = new byte[1024 * 1024];
		fs.read(b);
		fs.close();
		
		ByteBuffer wrap = ByteBuffer.wrap(b);
		assertEquals(5, wrap.getInt());
		assertEquals(5, q.size());
		
		assertEquals(16, wrap.getInt()); //starts at 16 bytes
		assertEquals(16, q.getStart()); 
		
		assertEquals(26, wrap.getInt()); 
		assertEquals(26, q.getEnd());
		
		//Garbage
		assertEquals(1, wrap.get()); //size of info
		assertEquals("a", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("v", new String(new byte[]{wrap.get()}));
		
		//Real Data
		assertEquals(1, wrap.get()); //size of info
		assertEquals("c", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("l", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("z", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("e", new String(new byte[]{wrap.get()}));
		assertEquals(1, wrap.get()); //size of info
		assertEquals("b", new String(new byte[]{wrap.get()}));
		
		while (wrap.limit() != wrap.position())
			assertEquals(0, wrap.get());
		
		f.delete();
	}
	
	private class SS implements Serializer<String> {
		@Override
		public byte[] checkpointData(String t) {
			return t.getBytes();
		}

		@Override
		public String interpret(byte[] checkpoint) {
			return new String(checkpoint);
		}
	}
}