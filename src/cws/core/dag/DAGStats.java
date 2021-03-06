package cws.core.dag;

import java.util.HashMap;

import cws.core.dag.algorithms.CriticalPath;
import cws.core.dag.algorithms.TopologicalOrder;
import cws.core.storage.StorageManager;

public class DAGStats {
    private double minCost;
    private double criticalPath;
    private double totalRuntime;

    public DAGStats(DAG dag, StorageManager storageManager) {
        TopologicalOrder order = new TopologicalOrder(dag);

        minCost = 0.0;
        totalRuntime = 0.0;

        HashMap<Task, Double> runtimes = new HashMap<Task, Double>();
        for (Task t : order) {
            double runtime = t.getPredictedRuntime(storageManager);
            runtimes.put(t, runtime);

            // Compute the minimum cost of running this workflow
            minCost += (runtime / (60 * 60)) * t.getVmType().getPrice();
            totalRuntime += runtime;
        }

        // Make sure a plan is feasible given the deadline and available VMs
        CriticalPath path = new CriticalPath(order, runtimes, storageManager);
        criticalPath = path.getCriticalPathLength();
    }

    public double getMinCost() {
        return minCost;
    }

    public double getCriticalPath() {
        return criticalPath;
    }

    public double getTotalRuntime() {
        return totalRuntime;
    }
}
