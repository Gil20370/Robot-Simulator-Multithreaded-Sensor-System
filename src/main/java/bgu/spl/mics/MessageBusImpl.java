package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private final ConcurrentHashMap<String, LinkedBlockingQueue<Message>>microServicesTasks;
	private final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> subscribedEvents;
	private final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> subscribedBroadcasts;
	private final ConcurrentHashMap<Event<?>, Future<?>> futuresEvents;
	private static MessageBusImpl instance = null;

	public synchronized static MessageBusImpl getInstance() {
		if(instance == null){
			instance = new MessageBusImpl();
		}
		return instance;
	}


	private MessageBusImpl() {
		microServicesTasks = new ConcurrentHashMap<>();
		subscribedEvents = new ConcurrentHashMap<>();
		subscribedBroadcasts = new ConcurrentHashMap<>();
		futuresEvents = new ConcurrentHashMap<>();
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		subscribedEvents.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
	}
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribedBroadcasts.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
	}
	@Override
	public <T> void complete(Event<T> e, T result) {
		if (futuresEvents.containsKey(e)) {
			Future<T> toBeReturnedFuture = (Future<T>) futuresEvents.get(e);
			toBeReturnedFuture.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> subscribers = subscribedBroadcasts.get(b.getClass());
		synchronized(subscribers){//in case someone is unregistering while we are looping
			if (subscribers != null) {
				for (MicroService microservice : subscribers) {
					LinkedBlockingQueue<Message> messageQueue = microServicesTasks.get(microservice.getName());
					if (messageQueue != null) {
						messageQueue.offer(b);
					}
				}
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if(e==null)
			return null;
		synchronized (e.getClass()) { //to ensure round robin
			ConcurrentLinkedQueue<MicroService> subscribers = subscribedEvents.get(e.getClass());
			if (subscribers != null && !subscribers.isEmpty()) {
				MicroService selectedMicroService = subscribers.poll(); // Remove the first subscriber
				if (selectedMicroService != null) {
					subscribers.offer(selectedMicroService); // Re-add the subscriber to the end of the queue for round-robin scheduling

					LinkedBlockingQueue<Message> messageQueue = microServicesTasks.get(selectedMicroService.getName());
					if (messageQueue != null) {
						messageQueue.offer(e); // Add the event to the selected MicroService's message queue
					}

					Future<T> future = new Future<>();
					futuresEvents.put(e, future); // Associate the event with a future
					return future;
				}
			}
		}
		return null; // No subscribers available for the event
	}


	@Override
	public void register(MicroService m) {
		microServicesTasks.putIfAbsent(m.getName(), new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		LinkedBlockingQueue<Message> removedQueue = microServicesTasks.remove(m.getName());
		if (removedQueue != null) {
			synchronized(subscribedEvents){
				for (ConcurrentLinkedQueue<MicroService> queue : subscribedEvents.values()) {
					queue.remove(m);
				}
			}
			synchronized(subscribedEvents){
				for (ConcurrentLinkedQueue<MicroService> queue : subscribedBroadcasts.values()) {
					queue.remove(m);
				}
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		LinkedBlockingQueue<Message> queue = microServicesTasks.get(m.getName());
		if (queue == null) {
			throw new IllegalStateException("MicroService not registered: " + m.getName());
		}
		return queue.take(); // Blocking call, no need for explicit synchronization
	}


	// public synchronized boolean noEventMessages(MicroService m) {
	// 	boolean noMessages = true;
	// 	if (microServicesTasks.containsKey(m.getName())) {
	// 		LinkedBlockingQueue<Message> queue = microServicesTasks.get(m.getName());
	// 		for (Message message : queue) {
	// 			if (message instanceof Event)
	// 				noMessages = false;

	// 		}
	// 	}
	// 	return noMessages;
	// }


	// unit testing purposes only
	public ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> getSubscribedBroadcasts() {
		return subscribedBroadcasts;
	}
	// unit testing purposes only
	public ConcurrentHashMap<Event<?>, Future<?>> getFuturesEvents() {
		return futuresEvents;
	}
	// unit testing purposes only
	public ConcurrentHashMap<String, LinkedBlockingQueue<Message>> getMicroServicesTasks() {
		return microServicesTasks;
	}
	// unit testing purposes only
	public ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> getSubscribedEvents() {
		return subscribedEvents;
	}
}