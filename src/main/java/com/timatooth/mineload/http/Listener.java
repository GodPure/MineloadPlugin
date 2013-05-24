package com.timatooth.mineload.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class listens on a specified port accepting new connections. New connections
 * are started in a new thread.
 *
 * Ideas: - Set global limit of running connections/threads.
 *
 * @author Tim Sullivan
 */
public class Listener extends Thread {
    
    /* Keep track of currently running HTTP instance threads */
    private int threadCount;
    /* Java server socket it's listening on */
    private ServerSocket serverSocket;
    /* Will keep running listener thread while running flag is true */
    private boolean keepRunning;
    /* Keep a thread limit to restrict system resource usage */
    private int connectionLimit;

    /**
     * Create the Http Listener on specified port. Creates Http runtime threads
     * for each connection.
     *
     * @param port The specified port to listen on.
     */
    public Listener(int port) {
        this.connectionLimit = 200;
        /* set name of thread */
        this.setName("Mineload HTTP Listener Thread");
        /* set status to be a background daemon thread */
        this.setDaemon(true);

        try {
            serverSocket = new ServerSocket(port);
            threadCount++;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        //set initial state to running.
        keepRunning = true;
    }

    /**
     * Set the state of the listener thread. Listener thread should terminate
     * when ready and no longer accept new connections.
     *
     * @param state set to false to disable listener.
     */
    public synchronized void setRunning(boolean state) {
        this.keepRunning = state;
    }

    /**
     * Main listener loop. Will keep running until running state is set to
     * false. Creates new http runtime threads which generate Request objects.
     */
    @Override
    public void run() {
        while (keepRunning) {
            /* Only accept new connections if limit hasn't been reached */
            if (threadCount < connectionLimit) {
                try {
                    System.out.println("Got new connection");
                    Socket connection = serverSocket.accept();
                    Runner run = new Runner(connection);
                    Thread newThread = new Thread(run);
                    newThread.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    /**
     * Set the limit of how many connections are allowed to HTTP server.
     * @param limit Limit to set
     */
    public synchronized void setConnectionLimit(int limit) {
        this.connectionLimit = limit;
    }

    /**
     * Called by Runner threads once they're done working.
     * Decreases the thread count to make way for new connections.
     */
    public synchronized void updateConnectionCount() {
        this.threadCount--;
    }
}