package Learning;

import Model.Agent;
import Model.Simulation;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

public class HumanController implements RLController, KeyListener, Serializable {
    private KeyEvent keyEvent;
    public JPanel simulationPanel;
    private Fitness fitness = new Fitness();
    private Simulation model;

    public HumanController() {}

    /**
     * When the agent asks us to pick an action we wait till a key has been pressed
     * @param a
     */
    @Override
    public void pickAction(Agent a) {

        simulationPanel.requestFocus();
        while(keyEvent == null){
            try {
                Thread.sleep(Math.abs(100));
            } catch (java.lang.InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        switch (keyEvent.getKeyCode()){
            case KeyEvent.VK_UP:
                a.moveUp();
                break;
            case KeyEvent.VK_DOWN:
                a.moveDown();
                break;
            case KeyEvent.VK_LEFT:
                a.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                a.moveRight();
                break;
            case KeyEvent.VK_SPACE:
                a.makeDirt();
                break;
            case KeyEvent.VK_ENTER:
                a.doNothing();
                break;
            default:
                keyPressed(keyEvent);
        }

        if (model != null) {
            Fitness.SPE_Measure StraightPaths = fitness.new SPE_Measure(model);
            System.out.println("Fitness: " + StraightPaths.getFitness(1));
        } else {
            System.out.println("Model is null!");
        }

        keyEvent = null;

    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    /**
     * Receive the key pressed and make it available for pickAction
     * @param keyEvent
     */
    @Override
    public void keyPressed(KeyEvent keyEvent) { this.keyEvent = keyEvent; }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}

    public void setModel(Simulation model) { this.model = model; }
}
