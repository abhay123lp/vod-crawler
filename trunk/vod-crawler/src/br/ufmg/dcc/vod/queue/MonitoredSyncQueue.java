package br.ufmg.dcc.vod.queue;

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

	private int workHandle = 0;
	private int timeStamp = 0;
	
	private final String label;
	private final EventQueue<T> e;


	public MonitoredSyncQueue(String label, EventQueue<T> e) {
		this.label = label;
		this.e = e;
	}

	public synchronized void put(T t) {
		this.workHandle++;
		this.timeStamp++;
		e.put(t);
		notify();
	}

	public synchronized T claim() throws InterruptedException  {
		if (e.size() == 0) {
			wait();
		}
		
		return e.take();
	}

	public synchronized void done(T claimed) {
		this.workHandle--;
	}

	public synchronized int size() {
		return e.size();
	}

	public synchronized Pair<Integer, Integer> synchronizationData() {
		return new Pair<Integer, Integer>(workHandle, timeStamp);
	}
	
	@Override
	public String toString() {
		return label;
	}
}