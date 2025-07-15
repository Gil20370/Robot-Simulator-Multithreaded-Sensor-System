import bgu.spl.mics.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the MessageBusImpl class.
 */
public class MessageBusTest {

    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MockMicroService("MicroService1");
        microService2 = new MockMicroService("MicroService2");
        messageBus.register(microService1);
        messageBus.register(microService2);
        while (!messageBus.getMicroServicesTasks().get(microService1.getName()).isEmpty()) {
            messageBus.getMicroServicesTasks().get(microService1.getName()).poll();
        }
        while (!messageBus.getMicroServicesTasks().get(microService2.getName()).isEmpty()) {
            messageBus.getMicroServicesTasks().get(microService2.getName()).poll();
        }
    }

    /** checks if noEventMessages return false if there is an event
     * @pre: microService1 is registered.
     * @pre: TestEvent class is a class that implements Event<string>
     * @pre: microService1 is subscribed to TestEvent.class
     * @pre: messageBus send event.
     * @pre: microService1 task Queue is empty.
     * @post: The event is added to the microservice's queue.
     * @post: the microService queue.size()==1
     * @post: messageBus.noEventMessage(microservice1)==false
     * Invariant: without changing the tasks queue returns false if there is messages in queue, and true otherwise.
     */
    @Test
    public void testNoEventMessages() {
        class TestEvent implements Event<String> {}
        messageBus.subscribeEvent(TestEvent.class, microService1);
        messageBus.sendEvent(new TestEvent());

        assertTrue(messageBus.getMicroServicesTasks().containsKey(microService1.getName()));
        assertEquals(1,messageBus.getMicroServicesTasks().get(microService1.getName()).size());
        //assertFalse(messageBus.noEventMessages(microService1));
    }

    /**
     * Will add the object (TestEvent) to the subscribedEvents map.
     * @pre: microService1 is registered
     * @pre microservice1 task queue is empty
     * @post microservice1 task queue is empty (AND NOT NULL)
     * @post: the subscribedEvents map contains TestEvent key :subscribedEvents.containsKey(TestEvent.class)==true.
     * @post: the queue<microservice> should contain microservice 1: subscribedEvents.get(TestEvent.class).contains(microservice1)=true
     * @INV: Subscribedevents map should reflect all subscriptions accurately,the subscribed microservice should be contained in the map according to the correspond key
     */
    @Test
    public void testSubscribeEvent() {
        class TestEvent implements Event<String> {}
        messageBus.subscribeEvent(TestEvent.class, microService1);

        assertTrue(messageBus.getMicroServicesTasks().get(microService1.getName()).isEmpty());
        assertTrue(messageBus.getSubscribedEvents().containsKey(TestEvent.class), "Event subscription should exist.");
        assertTrue(messageBus.getSubscribedEvents().get(TestEvent.class).contains(microService1), "MicroService1 should be subscribed to TestEvent.");
    }

    /**
     * Will add the object (TestBroadcast) to the subscribedBroadcasts map.
     * @pre: microService1 is registered.
     * @pre: microservice1 task queue is empty
     * @post: microservice1 task queue is empty
     * @post: the queue<microservice> should contain microservice 1: subscribedBroadcasts.get(TestBroadcast.class).contains(microservice1)=true
     * @INV: subscribedBroadcasts map should reflect all subscriptions accurately,the subscribed microservice should be contained in the map according to the correspond key
     */
    @Test
    public void testSubscribeBroadcast() {
        class TestBroadcast implements Broadcast {}
        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);

        assertTrue(messageBus.getMicroServicesTasks().get(microService1.getName()).isEmpty());
        assertTrue(messageBus.getSubscribedBroadcasts().containsKey(TestBroadcast.class));
        assertTrue(messageBus.getSubscribedBroadcasts().get(TestBroadcast.class).contains(microService1), "MicroService1 should be subscribed to TestBroadcast.");
    }

    /**
     * Will return a future<string>
     * @pre: microservice1 is registered.
     * @pre: a TestEvent obj  event is constructed implements Event<string>.
     * @pre: microservice1 is subscribed to testEvent.class
     * @pre: microservice1 task queue is empty
     * @pre: messageBus send event
     * @post: microservice task queue is of size 1
     * @post: messageBus.getMicroServicesTasks().containsKey(microservice1.getName())==true;
     * @post event exists last in microservice1 taskQueue: messageBus.getMicroServicesTasks().get(microservice1.getName()).last().equals(testEvent)
     * @post: future fields and the returned output should be a similar objects with a matching fields.
     * Invariant: The event should be added to the microservice's task queue correctly.
     */
    @Test
    public void test1SendEvent() {
        class TestEvent implements Event<String> {}
        TestEvent event = new TestEvent();
        Future<String> future = new Future<>();
        messageBus.subscribeEvent(TestEvent.class, microService1);
        messageBus.sendEvent(event);

        assertTrue(messageBus.getMicroServicesTasks().containsKey(microService1.getName()));
        assertEquals(1,messageBus.getMicroServicesTasks().get(microService1.getName()).size());
        Message last=null;
        for (Message m:messageBus.getMicroServicesTasks().get(microService1.getName())){
            last=m;
        }
        assertEquals(event,last);
        assertEquals(future.isDone(), messageBus.sendEvent(event).isDone());
        assertNull(messageBus.sendEvent(event).get(1000, TimeUnit.MILLISECONDS));
    }

    /**
     *
     * A null event is sent.
     * @pre: microservice 1 is registered.
     * @pre: a TestEvent obj  event is constructed implements Event<string>.
     * @pre: microservice1 is subscribed to testEvent.class
     * @post: The method returns null.
     * Invariant: Null events should not alter the state of the system.
     */
    @Test
    public void testSendEventNull() {
        class TestEvent implements Event<String> {}
        TestEvent event = new TestEvent();
        assertEquals(null, messageBus.sendEvent(null));
    }

    /**
     * Send broadcastmessage
     * @pre: microservice 1 is registered.
     * @pre: microservice 2 is registered
     * @pre: a TestBroadcast obj event is constructed implements broadcast.
     * @pre: microservice1 is subscribed to TestBroadcast.class
     * @pre: microservices is subscribed to TestBroadcast.class
     * @pre: messageBus send a broadcast.
     * @post: microservice1 and microservice 2 task queues are initialize in the map: messageBus.getMicroServicesTasks().containsKey(microservice1.getName())==true;
     * @post broadcast exists last in microservice1 and microservice 2 taskQueue: messageBus.getMicroServicesTasks().get(microservice1.getName()).last().equals(testEvent)
     * @post: future fields and the returned output should be a similar objects with a matching fields.
     * Invariant: The broadcast should be added to the microservice's task queue correctly(last).
     */
    @Test
    public void testSendBroadcast() {
        class TestBroadcast implements Broadcast {}
        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, microService2);
        TestBroadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);

        assertTrue(messageBus.getMicroServicesTasks().containsKey(microService1.getName()));
        assertTrue(messageBus.getMicroServicesTasks().containsKey(microService2.getName()));
        Message last=null;
        for (Message m:messageBus.getMicroServicesTasks().get(microService1.getName())) {
            last = m;
        }
        assertEquals(broadcast,last);
        for (Message m:messageBus.getMicroServicesTasks().get(microService2.getName())) {
            last = m;
        }
        assertEquals(broadcast,last);
    }

    /**
     * @pre: TestEvent is a class that implements Event<string>
     * @pre: create TestEvent event
     * @pre: object event exists in futuresEvents.
     * @post: output should be a Future object and should be resolve (isDone==true and result =result input).
     * @post: event should exists in futuresEvents
     * Invariant: Future objects should sould be resolved.
     */
    @Test
    public void testComplete() {
        class TestEvent implements Event<String> {}
        TestEvent event = new TestEvent();
        Future<String> future = new Future<>();
        messageBus.getFuturesEvents().put(event, future);

        messageBus.complete(event, "Result");
        assertTrue(messageBus.getFuturesEvents().containsKey(event));
        assertTrue(future.isDone(), "Future should be resolved.");
        assertEquals("Result", future.get(1000, TimeUnit.MILLISECONDS), "Future result should match.");
    }

    /**
     * will add a taskqueue named after microservice3 in the table
     * @Pre: obj microservice3 isn't registered.
     * @post: microservice3 is registered, has a task queue :(messageBus.getMicroServicesTasks().containsKey(microService3.getName())==true
     * Invariant: All registered microservices should have a task queue.
     */
    @Test
    public void testRegister() {
        MicroService microService3 = new MockMicroService("MicroService3");
        messageBus.register(microService3);
        assertTrue(messageBus.getMicroServicesTasks().containsKey(microService3.getName()));
    }

    /**
     * will remove the correspond task queue of microservice2 from the table
     * @Pre: obj microservice2 is registered.
     * @Post: microservice2 isn't registered, doesnt have a task queue: .
     * Invariant: Unregistering an unregistered microservice should not affect the system.
     */
    @Test
    public void testUnregister() {
        messageBus.unregister(microService2);
        assertFalse(messageBus.getMicroServicesTasks().containsKey(microService2.getName()));
        MicroService microservice3 = new MockMicroService("MicroService3");
    }

    /**
     * return the first message of a service queue.
     * @pre: microService1 is register.
     * @pre: microservice1 taskQueue is empty
     * @pre: TestEvent implements Event<String>
     * @pre: build 3 objects of type TestEvent.
     * @post: retrieve event1
     * @post: microService1 taskQueue size is 2
     * Invariant: retrive first message and dont damage the rest of the queue.
     */
    @Test
    public void testAwaitMessage() throws InterruptedException {
        class TestEvent implements Event<String> {}
        messageBus.subscribeEvent(TestEvent.class, microService1);
        assertTrue(messageBus.getMicroServicesTasks().get(microService1.getName()).isEmpty());
        TestEvent event1 = new TestEvent();
        TestEvent event2 = new TestEvent();
        TestEvent event3 = new TestEvent();
        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);
        messageBus.sendEvent(event3);

        assertEquals(messageBus.getMicroServicesTasks().get(microService1.getName()).peek(), messageBus.awaitMessage(microService1));
        assertEquals(2,messageBus.getMicroServicesTasks().get(microService1.getName()).size());
    }

    /**
     * MockMicroService is a simple implementation of the MicroService class for testing purposes.
     */
    private static class MockMicroService extends MicroService {
        protected MockMicroService(String name) {
            super(name);
        }

        @Override
        protected void initialize() {
            // No initialization required for the mock
        }
    }
}
