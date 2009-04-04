package br.ufmg.dcc.vod.queue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import br.ufmg.dcc.vod.common.Pair;

/**
 * QueueServices are used to create MonitoredSyncQueues, add objects to these
 * queues and register threads which will consume objects from queues. This
 * class is thread safe.
 * 
 * @param <T> Type of objects in a queue
 */
public class QueueService<T> {

	private final Map<QueueHandle, MonitoredSyncQueue<T>> ids = new HashMap<QueueHandle, MonitoredSyncQueue<T>>();
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final ReentrantLock lock = new ReentrantLock();
	
	private int i = 0;

	/**
	 * Creates a new message queue
	 * 
	 * @return A queue handle
	 */
	public QueueHandle createMessageQueue() {
		return createMessageQueue("");
	}


	/**
	 * Creates a new message queue with a label
	 *
	 * @param label Label
	 * @return A queue handle
	 */
	public QueueHandle createMessageQueue(String label) {
		try {
			lock.lock();
			QueueHandle h = new QueueHandle(i++);
			this.ids.put(h, new MonitoredSyncQueue<T>(label, new SimpleEventQueue<T>()));
			return h;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Creates a new message queue that is stored on disk
	 *
	 * @param label Label
	 * @param f File to use
	 * @param serializer To serialize object
	 * @param bytes amount of bytes to allocate on file
	 * 
	 * @return A queue handle
	 * 
	 * @throws IOException In case and io error occurs 
	 * @throws FileNotFoundException  In case the file does not exist
	 */
	public QueueHandle createPersistentMessageQueue(File f, Serializer<T> serializer, int bytes) throws FileNotFoundException, IOException {
		return createPersistentMessageQueue("", f, serializer, bytes);
	}


	/**
	 * Creates a new message queue that is stored on disk with a label
	 *
	 * @param label Label
	 * @param f File to use
	 * @param serializer To serialize object
	 * @param bytes amount of bytes to allocate on file
	 * 
	 * @return A queue handle
	 * 
	 * @throws IOException In case and io error occurs 
	 * @throws FileNotFoundException  In case the file does not exist
	 */
	public QueueHandle createPersistentMessageQueue(String label, File f, Serializer<T> serializer, int bytes) throws FileNotFoundException, IOException {
		try {
			lock.lock();
			QueueHandle h = new QueueHandle(i++);
			MemoryMappedQueue<T> memoryMappedQueue = new MemoryMappedQueue<T>(f, serializer, bytes);
			this.ids.put(h, new MonitoredSyncQueue<T>(label, memoryMappedQueue));
			return h;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Starts a QueueProcessor on a new Thread. It will consume the queue with
	 * the given handle.
	 * 
	 * @param h Handle identifying the queue
	 * @param p QueueProcessor object which will process
	 */
	public void startProcessor(QueueHandle h, QueueProcessor<T> p) {
		try {
			lock.lock();
			if (!this.ids.containsKey(h)) {
				throw new QueueServiceException("Unknown handle");
			}
		
			executor.execute(new WorkerRunnable(this.ids.get(h), p));
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Insert an object to a given queue so it can be processed by a QueueProcessor.
	 * 
	 * @param h The handle of the queue
	 * @param t The object to insert
	 */
	public void sendObjectToQueue(QueueHandle h, T t) {
		try {
			lock.lock();
			
			if (!this.ids.containsKey(h)) {
				throw new QueueServiceException("Unknown handle");
			}
		
			this.ids.get(h).put(t);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Waits until multiple queues are empty. The algorithm does two passes on the
	 * queues, one to see if they are empty and the other to assure that the work
	 * that was being done by the queues did not add any objects on the other queues. 
	 * <br>
	 * When using queues for communication it is important to only add an element to a
	 * communication queue after calling the <code>done</code> method on the queue the
	 * current thread is consuming. This is done by the <code>WorkerRunnable</code> class. 
	 * 
	 * @param secondsBetweenChecks - Seconds between verifications
	 */
	public void waitUntilWorkIsDoneAndStop(int secondsBetweenChecks) {
		boolean someoneIsWorking = false;
		do {
			try {
				lock.lock();
				
				//Debug info
				System.err.println(new Date());
				for (MonitoredSyncQueue<?> m : ids.values()) {
					System.err.println("Queue " + m + " size " + m.size());
					
				}
				
				someoneIsWorking = false;
				
				//Acquiring time stamps
				int[] stamps = new int[ids.size()];
				int i = 0;
				for (MonitoredSyncQueue<?> m : ids.values()) {
					Pair<Integer, Integer> sizeAndTimeStamp = m.synchronizationData();
					if (sizeAndTimeStamp.first != 0) {
						someoneIsWorking = true;
						break;
					} else {
						stamps[i] = sizeAndTimeStamp.second;
					}
					i++;
				}
	
				//Verifying if stamps changed
				i = 0;
				if (!someoneIsWorking) {
					for (MonitoredSyncQueue<?> m : ids.values()) {
						Pair<Integer, Integer> sizeAndTimeStamp = m.synchronizationData();
						if (sizeAndTimeStamp.first != 0 || stamps[i] != sizeAndTimeStamp.second) {
							someoneIsWorking = true;
							break;
						}
						i++;
					}
				}
				
				//Shutting Down!!!!
				if (!someoneIsWorking) {
					executor.shutdownNow();
				}
			} finally {
				lock.unlock();
				if (someoneIsWorking) {
					try {
						Thread.sleep(secondsBetweenChecks * 1000);
					} catch (InterruptedException e) {
					}
				}
			}
		} while (someoneIsWorking);
	}
	
	/**
	 * A worker runnable guarantees that the done method of the queue is called. 
	 */
	private class WorkerRunnable extends Thread {
		
		private final MonitoredSyncQueue<T> q;
		private final QueueProcessor<T> p;
		
		public WorkerRunnable(MonitoredSyncQueue<T> q, QueueProcessor<T> p) {
			super("WorkerRunnable: " + p.getName());
			this.q = q;
			this.p = p;
		}
		
		@Override
		public void run() {
			while (true) {
				boolean interrupted = false;
				T take = null;
				try {
					take = q.claim();
					p.process(take);
				} catch (InterruptedException e) {
					interrupted = true;
				} finally {
					if (!interrupted) q.done(take);
				}
			}
		}
	}
	
	protected MonitoredSyncQueue<T> getMessageQueue(QueueHandle handle) {
		return this.ids.get(handle);
	}
}
