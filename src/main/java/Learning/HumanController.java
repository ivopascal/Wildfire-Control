package Learning;

import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import Navigation.PathFinding.DijkstraShortestPath;
import Navigation.SubGoal;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.List;

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


        if(goal == null || goal != a.goal){
            goal = a.goal;
            dsp = new DijkstraShortestPath(model.getAllCells(), a, a.goal.goal, true);
            dsp.writeGoalMap();
        }

        int x = a.getX();
        int y = a.getY();

        double[] cellcosts = new double[4];
        cellcosts[0] = getCellCost(x+1, y, dsp);
        cellcosts[1] = getCellCost(x-1, y, dsp);
        cellcosts[2] = getCellCost(x, y+1, dsp);
        cellcosts[3] = getCellCost(x, y-1, dsp);
        System.out.println("P0");
        System.out.println("R " + cellcosts[0]);
        System.out.println("L " + cellcosts[1]);
        System.out.println("U " + cellcosts[2]);
        System.out.println("D " + cellcosts[3]);

        double max = 0.0;
        for(int i = 0; i<4; i++){
            if(cellcosts[i] > max){
                max = cellcosts[i];
            }
        }
        for(int i = 0; i<4; i++){

            cellcosts[i]/=max;
        }
        System.out.println("P1");
        System.out.println("R " + cellcosts[0]);
        System.out.println("L " + cellcosts[1]);
        System.out.println("U " + cellcosts[2]);
        System.out.println("D " + cellcosts[3]);

        for(int i =0; i<4; i++){
            cellcosts[i] = (1-cellcosts[i]);
        }
        System.out.println("P2");

        System.out.println("R " + cellcosts[0]);
        System.out.println("L " + cellcosts[1]);
        System.out.println("U " + cellcosts[2]);
        System.out.println("D " + cellcosts[3]);

        max = Double.MIN_VALUE;
        for(int i = 0; i<cellcosts.length; i++){
            if(cellcosts[i] > max){
                max = cellcosts[i];
            }
        }

        for(int i =0; i<cellcosts.length; i++){
            cellcosts[i] -= max;
        }



        System.out.println("P3olling from " + a.getX() + ", " + a.getY());
        System.out.println("R " + cellcosts[0]);
        System.out.println("L " + cellcosts[1]);
        System.out.println("U " + cellcosts[2]);
        System.out.println("D " + cellcosts[3]);
        System.out.println("Goal " + a.goal.goal.getX() + ", " + a.goal.goal.getY());

        List<List<Element>> cells = model.getAllCells();
        if(isHouse(x-1, y, cells) == 1){
            System.out.println("Hl");
        }
        if(isHouse(x+1, y, cells) == 1){
            System.out.println("Hr");
        }
        if(isHouse(x, y+1, cells) == 1){
            System.out.println("Hu");
        }
        if(isHouse(x, y-1, cells) == 1){
            System.out.println("Hd");
        }

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
            System.out.println("Fitness: " + fitness.totalHousesLeft(model));
        } else {
            System.out.println("Model is null!");
        }

        keyEvent = null;

    }

    private int isHouse(int x, int y, List<List<Element>> cells){
        if(x <0 || y < 0 || x>= cells.size() || y >= cells.get(x).size()){
            return 0;
        }
        if(cells.get(x).get(y).getType() == "House"){
            return 1;
        }
        return 0;
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
