package br.ufmg.dcc.vod.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import br.ufmg.dcc.vod.common.Pair;

/**
 * A synchronized queue of objects. This is a thread safe queue in which objects
 * retrieved by a <code>peek</code> operation are counted on a current work
 * atomic integer. In order to remove objects from this buffers, users of this class
 * must manually inform that have processed whatever information was needed from
 * the taken object (this is done using the <code>done</done> method. When an object
 * is removed the atomic integer is decreased, if it reaches 0 it means that no thread
 * has objects acquired.
 * <br>
 * This is done in order to monitor the state of queues in a way that if a thread is
 * processing a given object taken from a queue, we cannot say that the work is done 
 * because that thread may insert work again on this queue or other queues.
 * <br>
 * When using queues for communication it is important to only add an element to a
 * communication queue after calling the <code>done</code> method on the queue
 * the current thread is consuming.
 * 
 * @param <T>
 *            Type of objects to store
 */
class MonitoredSyncQueue<T> {

	private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>();

	/*
	 * Monitors state
	 */
	private final AtomicInteger workHandle = new AtomicInteger(0);
	private final AtomicInteger timeStamp = new AtomicInteger(0);
	private final ReentrantLock monitorLock = new ReentrantLock();

	public void put(T t) {
		try {
			monitorLock.lock();
			this.workHandle.incrementAndGet();
			this.timeStamp.incrementAndGet();
		} finally {
			monitorLock.unlock();
		}
		this.queue.add(t);
	}

	public T claim() throws InterruptedException {
		return this.queue.take();
	}

	public void done(T claimed) {
		try {
			monitorLock.lock();
			this.workHandle.decrementAndGet();
		}
		finally {
			monitorLock.unlock();
		}
	}

	public int size() {
		return queue.size();
	}

	public Pair<Integer, Integer> synchronizationData() {
		this.monitorLock.lock();
		try {
			return new Pair<Integer, Integer>(this.workHandle.get(), this.timeStamp.get());
		} finally {
			this.monitorLock.unlock();
		}
	}
}