
import Learning.BURLAP;
import Learning.CoSyNe.CircleSyNe;
import Learning.CoSyNe.HRL.ActionLearner;
import Learning.CoSyNe.HRL.HybridRL;
import Learning.CoSyNe.SubGoalLearning;
import Learning.CoSyNe.SubSyne;
import Learning.GA;
import Learning.HumanController;
import Model.Simulation;
import View.MainFrame;

import java.io.FileNotFoundException;
import java.io.PrintStream;

// Roel:


public class Main {
	public static void main(String[] args) {
		boolean use_gui;
		if (args.length > 0 && args[0].equals("no_gui")) {
			final long startTime = System.currentTimeMillis();
			System.out.println("NO GUI!");
			use_gui = false;
			new Simulation(use_gui).start();
			final long endTime = System.currentTimeMillis();
			System.out.println("Total execution time: " + (endTime - startTime));
		} else if (args.length > 0 && args[0].equals("cosyne_gui")) {
			System.out.println("CoSyNe gui");
			new CircleSyNe();
		} else if (args.length > 0 && args[0].equals("GA")){
			System.out.println("GA");
			new GA();

		} else if (args.length > 0 && args[0].equals("BURLAP")) {
			BURLAP test = new BURLAP();
			test.example();
		} else if (args.length > 0 && args[0].equals("human")) {
			HumanController hc = new HumanController();
			Simulation s = new Simulation(hc);
			hc.setModel(s);
			MainFrame f = new MainFrame(s);
			f.simulationPanel.addKeyListener(hc);
			hc.simulationPanel = f.simulationPanel;
		} else if (args.length > 0 && args[0].equals("sub")){
			try {
				PrintStream fileOut = new PrintStream("./Astar.txt");
				System.setOut(fileOut);
			} catch (FileNotFoundException e){
				e.printStackTrace();
			}
			new SubGoalLearning();
		} else if (args.length > 0 && args[0].equals("subSyne")){
			new SubSyne();
		}
		else if (args.length > 0 && args[0].equals("HRL")) {
			new ActionLearner();
		}else if(args.length > 0 && args[0].equals("hybrid")){
			try {
				PrintStream fileOut = new PrintStream("./hybrid.txt");
				System.setOut(fileOut);
			} catch (FileNotFoundException e){
				e.printStackTrace();
			}
			new HybridRL();
		} else {
			use_gui = true;
			Simulation model = new Simulation(use_gui);
			new MainFrame(model);
		}
	}



}


