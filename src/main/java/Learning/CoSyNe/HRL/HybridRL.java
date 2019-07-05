package Learning.CoSyNe.HRL;

import Learning.CoSyNe.WeightBag;
import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import Navigation.PathFinding.DijkstraShortestPath;
import Navigation.SubGoal;
import org.neuroph.core.Layer;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.core.transfer.Linear;
import org.neuroph.util.TransferFunctionType;

import java.util.List;

public class HybridRL extends ActionLearner {

    private DijkstraShortestPath dsp;
    private SubGoal previousGoal;
    private WeightBag[] hybridBags;
    private Weight certaintyWeight;
    private Neuron[] hybridNeurons;

    public HybridRL(){
        super();
    }

    /**
     * Add support for connections outside the MLP
     */
    @Override
    protected void initializeBags(){
        super.initializeBags();
        hybridBags = new WeightBag[17];
        for(int i = 0; i<16; i++){
            hybridBags[i] = new WeightBag(defBagSize(),defAlpha(), defWeightSpread());
        }
        hybridBags[16] = new WeightBag(defBagSize(),defAlpha(), 1);
    }

    /**
     * Add support for connections outside the MLP
     */
    @Override
    protected void createMLP(){
        super.createMLP();
        if(hybridNeurons == null){
            hybridNeurons = new Neuron[4];
        }

        Layer out = mlp.getLayerAt(mlp.getLayersCount() -1);

        for(int i =0; i<4; i++){
            hybridNeurons[i] = new Neuron();
        }

        for(int i =0; i< out.getNeuronsCount(); i++){
            Neuron n = out.getNeuronAt(i);
            n.setTransferFunction(new Linear());
            for(int j=0; j<hybridNeurons.length; j++){
                int bagslot = i * hybridNeurons.length + j;
                n.addInputConnection(hybridNeurons[j], hybridBags[bagslot].randomWeight().getValue());
            }
        }

        certaintyWeight = hybridBags[16].randomWeight(true);    //Weight for certainty, rest of list is obsolete

    }

    @Override
    protected void createBest(){
        super.createBest();
        Layer out = mlp.getLayerAt(mlp.getLayersCount() -1);

        for(int i =0; i<4; i++){
            hybridNeurons[i] = new Neuron();
        }

        for(int i =0; i< out.getNeuronsCount(); i++){
            Neuron n = out.getNeuronAt(i);
            for(int j=0; j<hybridNeurons.length; j++){
                int bagslot = i * hybridNeurons.length + j;
                n.addInputConnection(hybridNeurons[j], hybridBags[bagslot].bestWeight().getValue());
            }
        }

        certaintyWeight = hybridBags[16].bestWeight();
    }

    /**
     * Add support for connections outside the MLP
     */
    @Override
    protected void breed(){
        super.breed();
        for(int i =0; i<17; i++){
            hybridBags[i].breed(defN_children());
        }
    }

    /**
     * Add support for connectiosn outside the MLP
     */
    @Override
    protected void assignFitness(){
        super.assignFitness();
        for(int i =0; i<17; i++){
            hybridBags[i].updateFitness(getFitness());
        }
    }

    @Override
    protected double[] getInput() {
        if(model == null){
            model = new Simulation(this, generation);
            model.applySubgoals();
        }
        Agent agent = model.getAgents().get(0);
        Element goal = agent.goal.goal;
        if(previousGoal == null || previousGoal != agent.goal){
            previousGoal = agent.goal;
            dsp = new DijkstraShortestPath(model.getAllCells(), agent, goal, true);
            dsp.writeGoalMap();
        }

        int x = agent.getX();
        int y = agent.getY();
        List<List<Element>> cells = model.getAllCells();
        double[] output = new double[26];

        //Immediate points
        output[0] = isHouse(x-1, y, cells);
        output[1] = isHouse(x+1, y, cells);
        output[2] = isHouse(x, y+1, cells);
        output[3] = isHouse(x, y-1, cells);

        //diagonal points
        output[4] = isHouse(x-1, y-1, cells);
        output[5] = isHouse(x-1, y+1, cells);
        output[6] = isHouse(x+1, y-1, cells);
        output[7] = isHouse(x+1, y+1, cells);

        //absolute relation to fire
        if(x > cells.size()/2){
            output[8] = 1;
        }else{
            output[8] = 0;
        }
        if(x < cells.size()/2){
            output[9] = 1;
        }else{
            output[9] = 0;
        }
        if(y > cells.get(x).size()/2){
            output[10] = 1;
        }else{
            output[10] = 0;
        }
        if(y < cells.get(x).size()/2){
            output[11] = 1;
        }else{
            output[11] = 0;
        }

        //absolute relation to goal
        if(goal.getX() > x){
            output[12] = 1;
        }else{
            output[12] = 0;
        }

        if(goal.getX() <x){
            output[13] = 1;
        }else{
            output[13] = 0;
        }

        if(goal.getY() < y){
            output[14] = 1;
        }else{
            output[14] = 0;
        }

        if(goal.getY() > y){
            output[15] = 1;
        }else{
            output[15] = 0;
        }

        //If house between Agent and goal
        output[16] = 0;
        if(x > goal.getX()) {
            for (int i = goal.getX(); i < x && output[16] == 0; i++) {
                if (y > goal.getY()) {
                    for (int j = goal.getY(); j < y && output[16] == 0; j++) {
                        if (isHouse(i, j, cells) == 1) {
                            output[16] = 1;
                        }
                    }
                } else {
                    for (int j = y; j < goal.getY() && output[16] == 0; j++) {
                        if (isHouse(i, j, cells) == 1) {
                            output[16] = 1;
                        }
                    }
                }
            }
        }else{
            for (int i = x; i < goal.getX() && output[16] == 0; i++) {
                if (y > goal.getY()) {
                    for (int j = goal.getY(); j < y && output[16] == 0; j++) {
                        if (isHouse(i, j, cells) == 1) {
                            output[16] = 1;
                        }
                    }
                } else {
                    for (int j = y; j < goal.getY() && output[16] == 0; j++) {
                        if (isHouse(i, j, cells) == 1) {
                            output[16] = 1;
                        }
                    }
                }
            }
        }

        //Vision grid addition
        output[17] = 0;
        output[18] = 0;
        output[19] = 0;
        output[20] = 0;
        output[21] = 0;
        output[22] = 0;
        output[23] = 0;
        output[24] = 0;
        for(int i = 0; i<3 ; i++){
            for(int j=0; j<3 ; j++){
                if(isHouse(x - 4 + i, y + 2 + j, cells) == 1){
                    output[17] = 1;
                }
                if(isHouse(x - 1 + i, y + 2 + j, cells) == 1){
                    output[18] = 1;
                }
                if(isHouse(x + 2 + i, y + 2 + j, cells) == 1){
                    output[19] = 1;
                }
                if(isHouse(x - 4 + i, y - 1 + j, cells) == 1){
                    output[20] = 1;
                }
                if(isHouse(x + 2 + i, y - 1 + j, cells) == 1){
                    output[21] = 1;
                }
                if(isHouse(x - 4 + i, y - 4 + j, cells) == 1){
                    output[22] = 1;
                }
                if(isHouse(x - 1 + i, y - 4 + j, cells) == 1){
                    output[23] = 1;
                }
                if(isHouse(x + 2 + i, y - 4 + j, cells) == 1){
                    output[24] = 1;
                }
            }
        }





        //AStar info
        double[] cellcosts = new double[4];
        cellcosts[0] = getCellCost(x+1, y);
        cellcosts[1] = getCellCost(x-1, y);
        cellcosts[2] = getCellCost(x, y+1);
        cellcosts[3] = getCellCost(x, y-1);

        //Input of distance to goal
        double average_cost =0;
        double accepted_points =0;
        for(int i=0; i< 4; i++){
            if(cellcosts[i] < 10000){
                average_cost += cellcosts[i];
                accepted_points++;
            }
        }
        output[25] = average_cost/accepted_points / 100;


        double max = 0.0;
        double min = Double.MAX_VALUE;
        for(int i = 0; i<4; i++){
            if(cellcosts[i] > max){
                max = cellcosts[i];
            }
            if(cellcosts[i] < min){
                min = cellcosts[i];
            }
        }


        for(int i = 0; i<4; i++){

            //cellcosts[i]/=max;
            if(cellcosts[i] == min){
                cellcosts[i] = 1;
            }else{
                cellcosts[i] = 0;
            }
        }


        for(int i=0; i<4 && hybridNeurons != null; i++){
            //System.err.print(cellcosts[i] + " ");
            hybridNeurons[i].setOutput(cellcosts[i]);
            //hybridNeurons[i].calculate();
            if(hybridNeurons[i].getOutConnections().size() != 4){
                //System.err.println("Neuron has " + hybridNeurons[i].getOutConnections().size() + " out");
            }
        }
        //System.err.print("\n");

        //Perform operations to set inputs of the extras
        return output;
    }

    @Override
    protected double[] rescaleOverflow(double[] outputs){


        return super.rescaleOverflow(outputs);
    }

    private int isHouse(int x, int y, List<List<Element>> cells){
        if(x <0 || y < 0 || x>= cells.size() || y >= cells.get(x).size()){
            return 0;
        }
        if(cells.get(x).get(y).getType() == "House"){
            return 1;
        }
        //if(cells.get(x).get(y).isBurning()){
        //    return 1;
        //}
        return 0;
    }

    private int getCellCost(int x, int y){
        if(x < 0 || y < 0 || x >= dsp.cost.length || y >= dsp.cost[x].length ){
            return Integer.MAX_VALUE;
        }
        return dsp.cost[x][y];
    }

    @Override
    protected double defCertainty() {

        if(certaintyWeight.getValue() <=0){
            return 0.001;
        }
        return certaintyWeight.getValue();
    }

    @Override
    protected int defWeightSpread(){
        return 5;
    }

    @Override
    protected int[] defHiddenLayers(){
        int[] output = {8};

        return output;
    }

    @Override
    protected TransferFunctionType defTransferFunction(){
        return TransferFunctionType.SIGMOID;
    }


}
