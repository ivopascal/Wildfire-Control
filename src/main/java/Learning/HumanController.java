package Learning;

import Model.Agent;
import Model.Simulation;
import Navigation.PathFinding.DijkstraShortestPath;
import Navigation.SubGoal;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

public class HumanController implements RLController, KeyListener, Serializable {
    private KeyEvent keyEvent;
    public JPanel simulationPanel;
    private Fitness fitness = new Fitness();
    private Simulation model;
    private Features features;
    private DijkstraShortestPath dsp;
    private SubGoal goal;

    public HumanController() {
        features = new Features();
    }

    /**
     * When the agent asks us to pick an action we wait till a key has been pressed
     * @param a
     */
    @Override
    public void pickAction(Agent a) {
        simulationPanel.requestFocus();
        double [] feat = features.cornerVectors(model, false);

        for(double d : feat){
            System.out.print(d +" ");
        }
        System.out.print("\n");

        if(goal == null || goal != a.goal){
            goal = a.goal;
            dsp = new DijkstraShortestPath(model.getAllCells(), a, a.goal.goal, true);
            dsp.findPath();
        }

        int x = a.getX();
        int y = a.getY();

        System.out.println("Polling from " + a.getX() + ", " + a.getY());
        System.out.println("L " + getCellCost(x-1, y, dsp));
        System.out.println("R " + getCellCost(x+1, y, dsp));
        System.out.println("U " + getCellCost(x, y+1,dsp));
        System.out.println("D " + getCellCost(x, y-1,dsp));
        System.out.println("Goal " + a.goal.goal.getX() + ", " + a.goal.goal.getY());

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
            //System.out.println("Fitness: " + StraightPaths.getFitness(2));
            System.out.println("Fitness: " + fitness.totalFuelBurnt(model));
        } else {
            System.out.println("Model is null!");
        }

        keyEvent = null;

    }

    private int getCellCost(int x, int y, DijkstraShortestPath dsp){
        if(x < 0 || y < 0 || x >= dsp.cost.length || y >= dsp.cost[x].length ){
            return Integer.MAX_VALUE;
        }
        return dsp.cost[x][y];
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
