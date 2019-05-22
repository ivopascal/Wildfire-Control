package Learning.CoSyNe.HRL;

import Learning.CoSyNe.WeightBag;
import Model.Agent;
import Model.Elements.Element;
import Model.Simulation;
import Navigation.PathFinding.DijkstraShortestPath;
import Navigation.SubGoal;
import org.neuroph.core.Weight;
import org.neuroph.util.TransferFunctionType;

public class HybridRL extends ActionLearner {

    private DijkstraShortestPath dsp;
    private SubGoal previousGoal;
    private WeightBag[] hybridBags;
    private Weight[] hybridWeights;

    public HybridRL(){
        super();
    }

    /**
     * Add support for connections outside the MLP
     */
    @Override
    protected void initializeBags(){
        super.initializeBags();
        hybridBags = new WeightBag[4];
        for(int i = 0; i<4; i++){
            hybridBags[i] = new WeightBag(defBagSize(),defAlpha(), defWeightSpread());
        }
    }

    /**
     * Add support for connections outside the MLP
     */
    @Override
    protected void createMLP(){
        super.createMLP();
        if(hybridWeights == null){
            hybridWeights = new Weight[4];
        }
        for(int i = 0; i<4; i++){
            hybridWeights[i] = hybridBags[i].randomWeight();
        }
    }

    /**
     * Add support for connections outside the MLP
     */
    @Override
    protected void breed(){
        super.breed();
        for(int i =0; i<4; i++){
            hybridBags[i].breed(defN_children());
        }
    }

    /**
     * Add support for connectiosn outside the MLP
     */
    @Override
    protected void assignFitness(){
        super.assignFitness();
        for(int i =0; i<4; i++){
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
            dsp.findPath();
        }

        int x = agent.getX();
        int y = agent.getY();

        double[] output = new double[4];
        output[0] = getCellCost(x-1, y);
        output[1] = getCellCost(x+1, y);
        output[2] = getCellCost(x, y+1);
        output[3] = getCellCost(x, y-1);

        double max = 0.0;
        for(int i = 0; i<4; i++){
            if(output[i] > max){
                max = output[i];
            }
        }

        for(int i = 0; i<4; i++){
            output[i]/=max;
        }
        return output;

        /*
        System.out.println("L " + getCellCost(x-1, y));
        System.out.println("R " + getCellCost(x+1, y));
        System.out.println("U " + getCellCost(x, y+1));
        System.out.println("D " + getCellCost(x, y-1));


        return super.getInput();
        */
    }

    @Override
    protected double[] rescaleOverflow(double[] outputs){
        int x = model.getAgents().get(0).getX();
        int y = model.getAgents().get(0).getY();
        outputs[0] += hybridWeights[0].getValue()*getCellCost(x+1, y);
        outputs[1] += hybridWeights[1].getValue()*getCellCost(x-1, y);
        outputs[2] += hybridWeights[2].getValue()*getCellCost(x, y+1);
        outputs[3] += hybridWeights[3].getValue()*getCellCost(x, y-1);
        return super.rescaleOverflow(outputs);

    }

    private int getCellCost(int x, int y){
        if(x < 0 || y < 0 || x >= dsp.cost.length || y >= dsp.cost[x].length ){
            return Integer.MAX_VALUE;
        }
        return dsp.cost[x][y];
    }

    @Override
    protected double defCertainty() {
        return 0.5;
    }



}
