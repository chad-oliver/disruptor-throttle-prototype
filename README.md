Demo that shows a LMAX Disruptor working with an ArrayBlockingQueue.  The ArrayBlockingQueue can be used to pause and 
restart the LMAX Disruptor, which means that we will be able to use a LMAX Disruptor to throttle the 
pre-generation of XYZ (artifacts, i.e. something created using a CPU heavy algorithm).

This design allows us to process in the background time consuming operations, but limit the number of
XYZ.  A consumer can read the off the ArrayBlockingQueue a XYZ artifact that is ready for consumption.  
When the ArrayBlockingQueue has less artifacts than its size then the LMAX Disruptor wakes up.  

To Run the program, execute: com.xyz.disruptor.App

In the App you will see the following constants that are used to change the behavior of the App.

public static final int NUM_XYZ_TO_CREATE = 1000;  // how many XYZ should this demo 'create'
public static final int BLOCK_QUEUE_SIZE = 10; // what is the size of the ArrayBlockingQueue
public static final int THREAD_POOL_SIZE = 5; // how many threads should be used by the Disruptor
public static final int RING_BUFFER_SIZE = 4; // what is the size of the ring buffer
public static final int MAX_PAUSE_BETWEEN_EXTERNAL_REQUEST = 100; // fake pause when reading generated XYZs



  