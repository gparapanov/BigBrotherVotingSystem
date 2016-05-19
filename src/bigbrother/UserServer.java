/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bigbrother;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

/**
 *
 * @author 1307969
 */
public class UserServer implements Runnable {

    private Socket incoming;
    private boolean done = false;
    private boolean deny;
    private Semaphore sem;

    public UserServer(Socket s, boolean deny, Semaphore sem) {
        this.incoming = s;
        this.sem = sem;
        this.deny = deny;
    }

    @Override
    public void run() {
        try {
            while (!done) {
                // set up streams for bidirectional transfer
                // across connection socket 
                BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
                PrintWriter out = new PrintWriter(incoming.getOutputStream(), true /* auto flush */);
                out.println("You are connected to "
                        + incoming.getLocalAddress().getHostName()//or getRemoteSocketAdress()
                        + " on port " + incoming.getLocalPort()
                );
                String status = "Voting is ";
                if (AdminGUI.isVotingOpen()) {
                    status += "open! Nominated people:";
                    out.println(status);
                    //print nominated people
                    for (String s : AdminGUI.members) {
                        out.println("- " + s);
                    }
                    out.println("Vote for a person or type 'exit' to quit");
                } else {
                    status += "closed!";
                    out.println(status);
                    out.println("Now closing....");
                    cancel();
                }               
                if (deny) {
                    cancel();
                    out.println("You have already voted!");
                }
                while (!done) {
                    String str = in.readLine();//first input
                    if (str == null) {
                        done = true;
                    } else {
                        String str2 = in.readLine();//second input
                        if (str.equals(str2)) {
                            
                            if (AdminGUI.isVotingOpen() && !str.trim().equals("exit")) {
                                //if the voting is open
                                Calendar now = Calendar.getInstance();
                                String toAdd = "IP voted: " + incoming.getInetAddress()
                                        + " at " + now.get(Calendar.HOUR_OF_DAY)
                                        + ":" + now.get(Calendar.MINUTE) + "\nVoted for: "
                                        + str.trim() + "\n";
                                String eol = System.getProperty("line.separator");
                                String toPrint = "";
                                toPrint += AdminGUI.getVotingOpenedAt() + eol;
                                boolean hasVoted = false;
                                try {
                                    //critical section
                                    sem.acquire();
                                    hasVoted = AdminGUI.vote(str.trim(), incoming.getInetAddress());//attempt to vote
                                    if (hasVoted) {
                                        //if the vote has been successful update the voting log
                                        //with user's vote and ip
                                        AdminGUI.updateLog(toAdd);
                                    } else if (str.trim().equals("exit")) {
                                        cancel();
                                    }
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace();
                                } finally {
                                    //this is inside the semaphore, so no other user can
                                    //vote while one is getting the current information
                                    toPrint += "Highest votest cast: " + AdminGUI.mostVotes()
                                            + " at " + AdminGUI.getMostVotesTime() + eol;
                                    sem.release();
                                    //end of crit section
                                }
                                //output information to the user
                                toPrint += "Your nomination for: " + str.trim();
                                if (hasVoted) {
                                    toPrint += " accepted"+eol;
                                    toPrint+="Thank you for your vote!";
                                    cancel();
                                } else {
                                    toPrint += " rejected" + eol;
                                    toPrint += "Entry not found or already voted!";
                                }
                                out.println(toPrint);
                            } else {
                                if (str.trim().equals("exit")) {
                                    cancel();
                                } else {
                                    out.println("Voting is closed!");
                                    out.println(AdminGUI.getVotingClosedAt());
                                }
                            }
                        } else {
                            out.println("The two inputs don't match!");
                        }
                    }
                }
                incoming.close();
            }
        } catch (Exception ie) {

        }
    }

    public void cancel() {
        done = true;
    }

}
