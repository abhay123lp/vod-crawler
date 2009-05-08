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
class MemoryMappedFIFOQueue<T> implements EventQueue<T> {

	private static final int SLOT_SIZE = 255;
	
	private static final byte MORE_DATA_CHAR = '+';
	public static final String MORE_DATA_CHAR_STRING = "+";
	public static final String MORE_DATA_CHAR_ESCAPED = "\\+";
	
	private static final int INT_SIZE = Integer.SIZE / 8;
	
	private final Serializer<T> mqs;
	private final File f;
	
	private int end;
	private int start;
	private int size;
	
	private int waterMarkSizePos;
	private int waterMarkStartPos;
	private int waterMarkEndPos;
	
	private MappedByteBuffer map;
	private FileChannel channel;
	private final int sizeInBytes;
	
	private boolean open;
	private boolean created;

	public MemoryMappedFIFOQueue(File f, Serializer<T> mqs, int sizeInBytes) throws FileNotFoundException, IOException {
		if (sizeInBytes < (INT_SIZE * 3) + 2) { //Header + sizeInfo (1b) + entry (at least 1b)
			throw new QueueServiceException("Size must be at least: Header (12bytes) + sizeInfo (1byte) + entry (at least 1byte)");
		}
		
		this.f = f;
		this.mqs = mqs;
		this.sizeInBytes = sizeInBytes;
		this.created = false;
		this.open = false;
	}

	public int remaining() {
		return map.limit() - end;
	}

	/**
	 * Opens for operations. This can only be done once, subsequent calls will not change the state
	 * of the object of file mapping. This operation should be called when creating new queue files.
	 * 
	 * @throws IOException
	 */
	public void createAndOpen() throws IOException {
		if (!open && !created) {
			this.channel = new RandomAccessFile(f, "rw").getChannel();
			this.map = channel.map(MapMode.READ_WRITE, 0, sizeInBytes);
			
			this.waterMarkSizePos = 0;
			this.waterMarkStartPos = INT_SIZE;
			this.waterMarkEndPos = INT_SIZE * 2;
			
			this.start = this.end = waterMarkEndPos + INT_SIZE;
			this.size = 0;
			
			this.map.putInt(size);
			this.map.putInt(start);
			this.map.putInt(end);
			
			this.open = true;
			this.created = true;
			
			sync();
		}
	}
	
	/**
	 * Reopens a queue file for operations. This should be called when the file already exists
	 * 
	 * @throws IOException
	 */
	public void reopen() throws IOException {
		if (!open) {
			if (!f.exists()) {
				throw new IOException("File does not exist");
			}
			
			this.channel = new RandomAccessFile(f, "rw").getChannel();
			this.map = channel.map(MapMode.READ_WRITE, 0, sizeInBytes);
			
			this.waterMarkSizePos = 0;
			this.waterMarkStartPos = INT_SIZE;
			this.waterMarkEndPos = INT_SIZE * 2;
			
			this.map.position(waterMarkStartPos);
			this.start = this.map.getInt();
			
			this.map.position(waterMarkEndPos);
			this.end = this.map.getInt();

			this.map.position(waterMarkSizePos);
			this.size = this.map.getInt();

			this.open = true;
		}
	}
	
    private int unsigned(byte b) {
        return b & 0xFF;
    }
    
    private byte signed(int i) {
        return (byte) i;
    }
    
    @Override
	public void put(T t) {
    	verifyIfOpen();
    	
		//Inserting
		map.position(end);
		byte[] checkpointData = mqs.checkpointData(t);
		
//		if (checkpointData.length > MAX_ENTRY_SIZE) {
//			throw new QueueServiceException("Entry size is larger tham maximum allowed");
//		}
		
		if (map.position() + checkpointData.length + 1 > map.limit()) {
			throw new QueueServiceException("This queue file cannot support any more data!");
		}
		
		for (byte b : checkpointData) {
			if (b == MORE_DATA_CHAR) {
				throw new QueueServiceException("The char: " + MORE_DATA_CHAR_STRING + " must be escaped: " + MORE_DATA_CHAR_ESCAPED);
			}
		}

		//Inserting data into 255 bytes slots
		int amountOfSlots = checkpointData.length / SLOT_SIZE;
		int remaining = checkpointData.length;
		int srcPos = 0;
		for (int i = 0; i < amountOfSlots; i++) {
			int slotSize = Math.min(remaining, SLOT_SIZE);
			
			map.put(signed(slotSize));
			ByteBuffer wrap = ByteBuffer.wrap(checkpointData, srcPos, slotSize);
			map.put(wrap);
			
			remaining -= slotSize;
			srcPos += slotSize;
			
			if (remaining > 0) map.put(MORE_DATA_CHAR);
		}
		

		//Updating tail pointer
		end = map.position();
		map.position(waterMarkEndPos);
		map.putInt(end);
		
		//Updating size pointer
		map.position(waterMarkSizePos);
		size ++;
		map.putInt(size);
	}

    @Override
	public T take() {
    	verifyIfOpen();
    	
		if (this.size == 0) {
			throw new QueueServiceException("Queue empty");
		}
		
		//Retrieving
		map.position(start);
		int dataSize = unsigned(map.get());
		byte[] data = new byte[dataSize];
		start = map.position();
		map.get(data);

		//Updating head pointer
		start = map.position();
		map.position(waterMarkStartPos);
		map.putInt(start);
		
		//Updating size pointer
		map.position(waterMarkSizePos);
		size --;
		map.putInt(size);
		
		return mqs.interpret(data);
	}

	public void sync() {
		verifyIfOpen();
		this.map.force();
	}

	public void shutdownAndSync() throws IOException {
		verifyIfOpen();
		sync();
		this.channel.close();
		this.open = false;
		this.map = null;
	}

	public boolean shutdownAndDelete() throws IOException {
		verifyIfOpen();
		shutdownAndSync();
		this.f.deleteOnExit();
		return this.f.delete();
	}

	private void verifyIfOpen() {
		if (!open) {
			throw new QueueServiceException("This queue is not open");
		}
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

	public boolean isOpen() {
		return open;
	}

	public boolean deleteFileOnly() {
		return f.delete();
	}
}