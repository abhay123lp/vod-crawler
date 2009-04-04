package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;


/**
 * Maps a queue to a file. The file structure is the following:
 * 
 * Header:
 * size  = size of the queue
 * start = 4 bytes indicating where the head is
 * end   = 4 bytes indicating where the tail is
 * 
 * Content:
 * size  = 1 byte indicating the size of an entry (maximum size is 255)
 * #size bytes containing the entry 
 * 
 * This class receives an {@link Serializer} which is responsible
 * for interpreting byte arrays to objects.
 * 
 * No thread safe, the {@link QueueService} class guarantees that only one
 * thread will access the queue.
 * 
 * @param <T> Type of objects to store.
 */
class MemoryMappedQueue<T> implements EventQueue<T> {

	private static final int INT_SIZE = Integer.SIZE / 8;
	private static final int MAX_ENTRY_SIZE = 255;
	
	private final MappedByteBuffer map;
	private final Serializer<T> mqs;
	private final File f;
	private final FileChannel channel;
	
	private int end;
	private int start;
	private int size;
	
	private int sizePos;
	private int startPos;
	private int endPos;


	public MemoryMappedQueue(File f, Serializer<T> mqs, int sizeInBytes) throws FileNotFoundException, IOException {
		this.f = f;
		this.mqs = mqs;
		this.channel = new RandomAccessFile(f, "rw").getChannel();
		this.map = channel.map(MapMode.READ_WRITE, 0, sizeInBytes);
		
		this.sizePos = 0;
		this.startPos = INT_SIZE;
		this.endPos = INT_SIZE * 2;
		
		this.start = this.end = endPos + INT_SIZE;
		this.size = 0;
		
		this.map.putInt(size);
		this.map.putInt(start);
		this.map.putInt(end);
		
		sync();
	}
	
    private int unsigned(byte b) {
        return b & 0xFF;
    }
    
    private byte signed(int i) {
        return (byte) i;
    }
    
    @Override
	public void put(T t) {
		if (this.size == Integer.MAX_VALUE) {
			throw new NullPointerException("Queue full");
		}		

		//Inserting
		map.position(end);
		byte[] checkpointData = mqs.checkpointData(t);
		
		if (checkpointData.length > MAX_ENTRY_SIZE) {
			throw new NullPointerException("Entry size is larger tham maximum allowed");
		}
		
		ByteBuffer wrap = ByteBuffer.wrap(checkpointData);
		map.put(signed(wrap.capacity()));
		map.put(checkpointData);

		//Updating tail pointer
		end = map.position();
		map.position(endPos);
		map.putInt(end);
		
		//Updating size pointer
		map.position(sizePos);
		size ++;
		map.putInt(size);
	}

    @Override
	public T take() {
		if (this.size == 0) {
			throw new NullPointerException("Queue empty");
		}
		
		//Retrieving
		map.position(start);
		int dataSize = unsigned(map.get());
		byte[] data = new byte[dataSize];
		start = map.position();
		map.get(data);

		//Updating head pointer
		start = map.position();
		map.position(startPos);
		map.putInt(start);
		
		//Updating size pointer
		map.position(sizePos);
		size --;
		map.putInt(size);
		
		return mqs.interpret(data);
	}

	public void sync() {
		this.map.force();
	}

	public void shutdownAndSync() throws IOException {
		sync();
		this.channel.close();
	}

	public boolean shutdownAndDelete() throws IOException {
		this.channel.close();
		this.f.deleteOnExit();
		return this.f.delete();
	}

	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	@Override
	public int size() {
		return size;
	}
}