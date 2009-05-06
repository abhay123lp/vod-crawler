package br.ufmg.dcc.vod.ncrawler.queue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


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

	public static final int MAX_ENTRY_SIZE = 255;
	
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
	private boolean compress = true;

	public MemoryMappedFIFOQueue(File f, Serializer<T> mqs, int sizeInBytes, boolean compress) throws FileNotFoundException, IOException {
		if (sizeInBytes < (INT_SIZE * 3) + 2) { //Header + sizeInfo (1b) + entry (at least 1b)
			throw new QueueServiceException("Size must be at least: Header (12bytes) + sizeInfo (1byte) + entry (at least 1byte)");
		}
		
		this.f = f;
		this.mqs = mqs;
		this.sizeInBytes = sizeInBytes;
		this.created = false;
		this.open = false;
		this.compress = compress;
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
		
		if (compress) {
		    checkpointData = compactData(checkpointData);
		}
		
		if (checkpointData.length > MAX_ENTRY_SIZE) {
			throw new QueueServiceException("Entry size is larger tham maximum allowed");
		}
		
		if (map.position() + checkpointData.length + 1 > map.limit()) {
			throw new QueueServiceException("This queue file cannot support any more data!");
		}
		
		ByteBuffer wrap = ByteBuffer.wrap(checkpointData);
		map.put(signed(wrap.capacity()));
		map.put(checkpointData);

		//Updating tail pointer
		end = map.position();
		map.position(waterMarkEndPos);
		map.putInt(end);
		
		//Updating size pointer
		map.position(waterMarkSizePos);
		size ++;
		map.putInt(size);
	}

	private byte[] compactData(byte[] checkpointData) {
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		
		compressor.setInput(checkpointData);
		compressor.finish();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(checkpointData.length);
		byte[] buf = new byte[MAX_ENTRY_SIZE];
		
		while (!compressor.finished()) {
		    int count = compressor.deflate(buf);
		    bos.write(buf, 0, count);
		}
		
		try {
			bos.close();
		} catch (IOException e) {
		}
		
		// Get the compressed data
		checkpointData = bos.toByteArray();
		return checkpointData;
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
		
		if (compress) {
		    data = uncompactData(data);
		}
		
		return mqs.interpret(data);
	}

	private byte[] uncompactData(byte[] data) {
		Inflater decompressor = new Inflater();
		decompressor.setInput(data);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		
		byte[] buf = new byte[MAX_ENTRY_SIZE];
		while (!decompressor.finished()) {
		    try {
		        int count = decompressor.inflate(buf);
		        bos.write(buf, 0, count);
		    } catch (DataFormatException e) {
		    	throw new QueueServiceException("Unknow error! " + e);
		    }
		}
		
		try {
		    bos.close();
		} catch (IOException e) {
		}
		
		data = bos.toByteArray();
		return data;
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