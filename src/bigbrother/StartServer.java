/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bigbrother;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 1307969
 */
public class StartServer extends Thread {

    private boolean done = false;
    private static Socket socket = null;
    private ServerSocket s = null;
    private Semaphore sem;
    private final int NumOfThreads = 100;
    private ExecutorService executor;

    public StartServer() {
        //semaphore to force exclusive entry when a user votes
        //it is passed to every new thread created so that only 1 person can 
        //vote at a time
        sem = new Semaphore(1, true);
        executor = Executors.newFixedThreadPool(NumOfThreads);
    }

    @Override
    public void run() {

        try {
            ServerSocket s = new ServerSocket(8189);
            System.out.println("Server is listening...");
            // listen for a connection request on server socket s
            // incoming is the connection socket
            while (!done) {
                Socket incoming = s.accept();
                //System.out.println("Client connected.");
                if (!AdminGUI.getConnected().contains(incoming.getInetAddress())) {
                    //if the user's ip is not in the arraylist let him vote
                    executor.submit(new UserServer(incoming, false, sem));
                } else {
                    //if his ip is in the arraylist this means he has already voted
                    //so concel his connection
                    executor.submit(new UserServer(incoming, true, sem));
                }
                if (done) {
                    break;
                }
            }
        } catch (Exception e) {

        }
        executor.shutdown();
        // Wait until all threads are finished
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // we have waited long enough
                // just shutdown all threads now
                executor.shutdownNow();
                System.out.println("I am not going to wait any longer");
            }
        } catch (InterruptedException ie) {
            // don't wait any longer
            // just shutdown all threads now
            executor.shutdownNow();
            System.out.println("I am not going to wait any longer");
        }
    }

    public void cancel() {
        done = true;
        executor.shutdownNow();
        try {
            s.close();
            //socket.close();

        } catch (Exception ie) {
        };
    }

}
