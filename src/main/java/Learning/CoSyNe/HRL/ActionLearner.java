package Learning.CoSyNe.HRL;

import Learning.CoSyNe.SubSyne;
import Learning.CoSyNe.WeightBag;
import Learning.Fitness;
import Model.Simulation;
import View.MainFrame;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * We build on SubSyne, since this already knows how to navigate to a subgoal, and use GoalLearner to pick the subgoals.
 */
public class ActionLearner extends SubSyne {

    private Double bestGoalFitness;
    private Double meanGoalFitness;
    private GoalLearner goalLearner;
    private List<Double> goalFitnessess;
    public ActionLearner(){
        super();
    }

    @Override
    protected void rerunScreenShot(){
        model = new Simulation(this, generation);
        model.getParameter_manager().changeParameter("Model", "Step Time", 1000f);
        JFrame f = new MainFrame(model);
        model.applySubgoals();
        model.start();
        try {
            Thread.sleep(Math.abs(1000));
        } catch (java.lang.InterruptedException e) {
            System.out.println(e.getMessage());
        }
        screenshot(0, (int) getFitness());
        f.dispose();
    }

    @Override
    /**
     * Change the testMLP to have goalLearner pick subGoals
     */
    protected void testMLP(){
        //Goal learner does not run its own generation, so testMLP asks for inputs, and grants fitness
        if(goalLearner == null){
            goalLearner = new GoalLearner();
        }
        double[] dist = goalLearner.generateGoals(model);
        model.setSubGoals(dist);
        model.applySubgoals();


        model.start();
        assignFitness();
        //We grant a fitness to goalLearner
        goalLearner.setFitness(getGoalFitness());
        Fitness fit = new Fitness();
        goalFitnessess.add((double) fit.totalHousesLeft(model));


        mean_perfomance += getFitness();
        if(best_performance == null || getFitness() < best_performance){
            best_performance = getFitness();
        }
        if(ultimate_performance == null || getFitness() < ultimate_performance){    //take screenshot
            ultimate_performance = getFitness();
            rerunScreenShot();
        }
        model = new Simulation(this, generation);
    }

    @Override
    /**
     * Changed printPerformance to add some more insights relevant to the HRL
     */
    protected void printPerformance(){
        //System.out.println("Best performance: " + best_performance + " , " + bestGoalFitness);
        //System.out.println("Mean performance: " + mean_perfomance + " , " + meanGoalFitness/defGenerationSize());
        //System.out.println("Mean confidence: " + mean_confidence / conf_counter);

        double parentMean = 0;
        Collections.sort(goalFitnessess);
        for(int i=0; i<goalFitnessess.size(); i++){
            parentMean += goalFitnessess.get(i);
        }
        parentMean/=(goalFitnessess.size());
        System.out.print(goalFitnessess.get(0) + "\t");
        System.out.print(parentMean + "\t");

        goalFitnessess = null;
        bestGoalFitness = null;
        meanGoalFitness = null;
        testBest();
    }

    @Override
    protected void createBest() {
        super.createBest();
    }

    @Override
    protected void printBest(){
        double[] dist = goalLearner.generateBest(model);
        model.setSubGoals(dist);
        model.applySubgoals();
        model.start();
        System.out.println(getGoalFitness());
        model = new Simulation(this, generation);

    }

    @Override
    /**
     * Override the breeding step to inform GoalLearner that it needs to breed
     */
    protected void breed(){
        for(int layer = 0; layer < weightBags.size(); layer++){
            for(int neuron = 0; neuron < weightBags.get(layer).size(); neuron++) {
                for (int weight = 0; weight < weightBags.get(layer).get(neuron).size(); weight++) {
                    WeightBag bag = weightBags.get(layer).get(neuron).get(weight);
                    bag.breed(defN_children());
                }
            }
        }
        goalLearner.breed();
    }

    /**
     * We have an extra function to determine the fitness of the goal.
     * The goal does not need to care whether the agent is able to reach it, only whether the map burns.
     * @return
     */
    private double getGoalFitness(){
        Fitness fit = new Fitness();
        if(goalFitnessess == null){
            goalFitnessess = new ArrayList<>();
        }
        double fitness = fit.totalHousesLeft(model);
        if(bestGoalFitness == null || fitness < bestGoalFitness){
            bestGoalFitness = fitness;
        }
        if(meanGoalFitness == null){
            meanGoalFitness = new Double(0);
        }
        meanGoalFitness += fitness;
        return fitness;
    }

}
