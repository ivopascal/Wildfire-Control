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
            hybridBags[i] = new WeightBag(defBagSize(),defAlpha(), defWeightSpread()*100);
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

        certaintyWeight = hybridBags[16].randomWeight();    //Weight for certainty, rest of list is obsolete

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
        double[] output = new double[12];

        output[0] = isHouse(x-1, y, cells);
        output[1] = isHouse(x+1, y, cells);
        output[2] = isHouse(x, y+1, cells);
        output[3] = isHouse(x, y-1, cells);

        output[4] = isHouse(x-1, y-1, cells);
        output[5] = isHouse(x-1, y+1, cells);
        output[6] = isHouse(x+1, y-1, cells);
        output[7] = isHouse(x+1, y+1, cells);

        output[8] = x - (cells.size()/2);
        output[9] = (cells.size()/2) -x;
        output[10] = y - (cells.get(x).size()/2);
        output[11] = (cells.get(x).size()/2) - y;

        double[] cellcosts = new double[4];
        cellcosts[0] = getCellCost(x+1, y);
        cellcosts[1] = getCellCost(x-1, y);
        cellcosts[2] = getCellCost(x, y+1);
        cellcosts[3] = getCellCost(x, y-1);
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



}
