/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bigbrother;

import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Georgi
 */
public class BigBrother {

    /**
     * @param args the command line arguments
     */
    private static void createAndShowGUI() {
        AdminGUI g = new AdminGUI(); // initialise AdminGUI
        g.setVisible(true); // make frame visible 
    }
    public static void main(String[] args) {
        Runnable doCreateAndShowGUI = new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        };
        SwingUtilities.invokeLater(doCreateAndShowGUI);
        
    }
    
}
