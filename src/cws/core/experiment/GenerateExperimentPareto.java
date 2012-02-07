package cws.core.experiment;


import java.util.Random;

import org.junit.Test;


/**
 * Generates series of ensembles consisting of workflows form workflow generator  
 * https://confluence.pegasus.isi.edu/display/pegasus/WorkflowGenerator
 * 
 * From each workflow type DAGs of Pareto - distributed sizes are selected. 
 * 
 * @author malawski
 *
 */

public class GenerateExperimentPareto {
    
	
	/******************************
	 * 
	 * Tests with max scaling = 0.0
	 * 
	 ******************************/
	
	public static void main(String [] args) {		

		String dagPath;
		String dagName;
		double price = 1.0;
		double max_scaling = 2.0;
		String prefix = "pareto-";
		double alpha = 0.7;
		String[] dags;
		double[] budgets;
		
		int maxHours;
		int stepHours;
		int startHours;

		
		dagPath = "../projects/pegasus/Montage/";
		dagName = "MONTAGE";
		
		dags = DAGListGenerator.generateDAGListPareto(new Random(0), dagName, 100);
		maxHours = 20;
		stepHours = 1;
		startHours = 1;
		max_scaling = 0;
		
//		budgets= new double[] {10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0};
		budgets = new double[] {20.0, 30.0, 50.0, 60.0, 80.0};
		
		for (double budget : budgets) {
			Experiment.generateSeriesRepeat(prefix, dagPath, dags, budget, price, maxHours, stepHours, startHours, max_scaling, alpha, 1);			
		}		

		dagPath = "../projects/pegasus/CyberShake/";
		dagName = "CYBERSHAKE";
		
		dags = DAGListGenerator.generateDAGListPareto(new Random(0), dagName, 100);
		
		maxHours = 20;
		stepHours = 1;
		startHours = 1;
		max_scaling = 0;

		
//		budgets= {10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 80.0, 100.0, 120.0, 140.0};
		budgets= new double[] {30.0, 50.0, 80.0, 100.0, 140.0};
		
		for (double budget : budgets) {
			Experiment.generateSeriesRepeat(prefix, dagPath, dags, budget, price, maxHours, stepHours, startHours, max_scaling, alpha, 1);			
		}
		
	

	
		
		
		dagPath = "../projects/pegasus/LIGO/";
		dagName = "LIGO";
		
		dags = DAGListGenerator.generateDAGListPareto(new Random(0), dagName, 100);
		
		maxHours = 40;
		stepHours = 1;
		startHours = 1;
		max_scaling = 0;


//		budgets= new double[] {200.0, 400.0, 600.0, 800.0, 1000.0, 1200.0, 1400.0, 1600.0, 1800.0, 2000.0};
		budgets= new double[] {400.0, 600.0, 800.0, 1000.0, 1200.0};

		for (double budget : budgets) {
			Experiment.generateSeriesRepeat(prefix, dagPath, dags, budget, price, maxHours, stepHours, startHours, max_scaling, alpha, 1);			
		}
		
		
		dagPath = "../projects/pegasus/Genome/";
		dagName = "GENOME";
		
		dags = DAGListGenerator.generateDAGListPareto(new Random(0), dagName, 100);
		
		maxHours = 1500;
		stepHours = 100;
		startHours = 100;
		max_scaling = 0;

		
//		budgets= {2000.0, 4000.0, 6000.0, 8000.0, 10000.0, 12000.0, 14000.0, 16000.0, 18000.0, 20000.0};
		budgets= new double[] {4000.0, 6000.0, 8000.0, 10000.0, 12000.0};

		
		for (double budget : budgets) {
			Experiment.generateSeriesRepeat(prefix, dagPath, dags, budget, price, maxHours, stepHours, startHours, max_scaling, alpha, 1);			
		}
			
				
		dagPath = "../projects/pegasus/SIPHT/";
		dagName = "SIPHT";
		
		dags = DAGListGenerator.generateDAGListPareto(new Random(0), dagName, 100);

		maxHours = 50;
		stepHours = 5;
		startHours = 5;
		max_scaling = 0;

		
//		budgets= new double[] {200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0, 1100.0};
		budgets= new double[] {200.0, 400.0, 600.0, 800.0, 1000.0};

		for (double budget : budgets) {
			Experiment.generateSeriesRepeat(prefix, dagPath, dags, budget, price, maxHours, stepHours, startHours, max_scaling, alpha, 1);			
		}
		
	}


	



	
}
