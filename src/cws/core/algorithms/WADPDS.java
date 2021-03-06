package cws.core.algorithms;

import java.util.List;

import cws.core.cloudsim.CloudSimWrapper;
import cws.core.dag.DAG;
import cws.core.provisioner.SimpleUtilizationBasedProvisioner;
import cws.core.scheduler.WorkflowAwareEnsembleScheduler;

public class WADPDS extends DynamicAlgorithm {
    public WADPDS(double budget, double deadline, List<DAG> dags, double price, double maxScaling,
            CloudSimWrapper cloudsim, StorageSimulationParams simulationParams) {
        super(budget, deadline, dags, price, new WorkflowAwareEnsembleScheduler(cloudsim),
                new SimpleUtilizationBasedProvisioner(maxScaling, cloudsim), cloudsim, simulationParams);
    }
}
