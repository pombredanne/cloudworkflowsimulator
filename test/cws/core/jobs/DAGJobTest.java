package cws.core.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cws.core.algorithms.VMType;
import cws.core.dag.DAG;
import cws.core.dag.DAGJob;
import cws.core.dag.Task;

public class DAGJobTest {

    public DAG diamondDAG() {
        DAG diamond = new DAG();
        Task a = new Task("a", "test::a", 10, VMType.DEFAULT_VM_TYPE);
        Task b = new Task("b", "test::b", 5, VMType.DEFAULT_VM_TYPE);
        Task c = new Task("c", "test::c", 5, VMType.DEFAULT_VM_TYPE);
        Task d = new Task("d", "test::d", 10, VMType.DEFAULT_VM_TYPE);
        diamond.addTask(a);
        diamond.addTask(b);
        diamond.addTask(c);
        diamond.addTask(d);
        diamond.addEdge("a", "b");
        diamond.addEdge("a", "c");
        diamond.addEdge("c", "d");
        diamond.addEdge("b", "d");
        return diamond;
    }

    @Test
    public void testDiamondDAG() {
        DAG dag = diamondDAG();

        DAGJob dj = new DAGJob(dag, 0);

        assertEquals(dj.readyTasks(), 1);

        Task a = dj.nextReadyTask();

        assertEquals(a.getId(), "a");

        assertEquals(dj.readyTasks(), 0);

        dj.completeTask(a);

        assertEquals(dj.readyTasks(), 2);

        Task bc = dj.nextReadyTask();

        assertTrue("b".equals(bc.getId()) || "c".equals(bc.getId()));

        assertEquals(dj.readyTasks(), 1);

        Task cb = dj.nextReadyTask();

        assertTrue("b".equals(cb.getId()) || "c".equals(cb.getId()));

        assertEquals(dj.readyTasks(), 0);

        dj.completeTask(bc);

        assertEquals(dj.readyTasks(), 0);

        dj.completeTask(cb);

        assertEquals(dj.readyTasks(), 1);

        Task d = dj.nextReadyTask();

        assertEquals(d.getId(), "d");

        assertFalse(dj.isFinished());

        dj.completeTask(d);

        assertTrue(dj.isFinished());
    }

}
