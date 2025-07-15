package bgu.spl.mics;

import java.io.FileNotFoundException;

/**
 * a callback is a function designed to be called when a message is received.
 */
public interface Callback<T> {

    public void call(T c) throws FileNotFoundException, InterruptedException;

}
