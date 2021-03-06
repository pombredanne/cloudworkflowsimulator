package cws.core;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import cws.core.algorithms.VMType;
import cws.core.cloudsim.CWSSimEntity;
import cws.core.cloudsim.CWSSimEvent;
import cws.core.cloudsim.CloudSimWrapper;
import cws.core.dag.Task;
import cws.core.jobs.Job;
import cws.core.storage.StorageManager;
import cws.core.storage.VoidStorageManager;

public class VMTest {
    private CloudSimWrapper cloudsim;
    @SuppressWarnings("unused")
    private StorageManager storageManager;

    private class VMDriver extends CWSSimEntity {
        private VM vm;
        private Job[] jobs;

        public VMDriver(VM vm, CloudSimWrapper cloudsim) {
            super("VMDriver", cloudsim);
            this.vm = vm;
            getCloudsim().addEntity(this);
        }

        public void setJobs(Job[] jobs) {
            this.jobs = jobs;
        }

        @Override
        public void startEntity() {
            sendNow(vm.getId(), WorkflowEvent.VM_LAUNCH);

            // Submit all the jobs
            for (Job j : jobs) {
                j.setOwner(getId());
                getCloudsim().send(getId(), vm.getId(), 0.0, WorkflowEvent.JOB_SUBMIT, j);
            }
        }

        @Override
        public void processEvent(CWSSimEvent ev) {
            switch (ev.getTag()) {
            case WorkflowEvent.JOB_STARTED: {
                Job j = (Job) ev.getData();
                assertEquals(Job.State.RUNNING, j.getState());
                break;
            }
            case WorkflowEvent.JOB_FINISHED: {
                Job j = (Job) ev.getData();
                assertEquals(Job.State.TERMINATED, j.getState());
                break;
            }
            }
        }
    }

    @Before
    public void setUp() {
        cloudsim = new CloudSimWrapper();
        cloudsim.init();
        storageManager = new VoidStorageManager(cloudsim);
    }

    @Test
    public void testSingleJob() {
        Job j = new Job(cloudsim);
        j.setTask(new Task("task_id", "transformation", 1000, VMType.DEFAULT_VM_TYPE));

        VMStaticParams vmStaticParams = new VMStaticParams();
        vmStaticParams.setMips(100);
        vmStaticParams.setCores(1);
        vmStaticParams.setPrice(0.40);

        VM vm = new VM(vmStaticParams, cloudsim);

        VMDriver driver = new VMDriver(vm, cloudsim);
        driver.setJobs(new Job[] { j });

        cloudsim.startSimulation();

        assertEquals(0.0, j.getReleaseTime(), 0.0);
        assertEquals(0.0, j.getSubmitTime(), 0.0);
        assertEquals(0.0, j.getStartTime(), 0.0);
        assertEquals(10.0, j.getFinishTime(), 0.0);
    }

    @Test
    public void testTwoJobs() {
        Job j1 = new Job(cloudsim);
        j1.setTask(new Task("task_id", "transformation", 1000, VMType.DEFAULT_VM_TYPE));
        Job j2 = new Job(cloudsim);
        j2.setTask(new Task("task_id2", "transformation", 1000, VMType.DEFAULT_VM_TYPE));

        VMStaticParams vmStaticParams = new VMStaticParams();
        vmStaticParams.setMips(100);
        vmStaticParams.setCores(1);
        vmStaticParams.setPrice(0.40);

        VM vm = new VM(vmStaticParams, cloudsim);

        VMDriver driver = new VMDriver(vm, cloudsim);
        driver.setJobs(new Job[] { j1, j2 });

        cloudsim.startSimulation();

        assertEquals(0.0, j1.getReleaseTime(), 0.0);
        assertEquals(0.0, j1.getSubmitTime(), 0.0);
        assertEquals(0.0, j1.getStartTime(), 0.0);
        assertEquals(10.0, j1.getFinishTime(), 0.0);

        assertEquals(0.0, j2.getReleaseTime(), 0.0);
        assertEquals(0.0, j2.getSubmitTime(), 0.0);
        assertEquals(10.0, j2.getStartTime(), 0.0);
        assertEquals(20.0, j2.getFinishTime(), 0.0);
    }

    @Test
    public void testMultiCoreVM() {
        Job j1 = new Job(cloudsim);
        j1.setTask(new Task("task_id1", "transformation", 1000, VMType.DEFAULT_VM_TYPE));

        Job j2 = new Job(cloudsim);
        j2.setTask(new Task("task_id2", "transformation", 1000, VMType.DEFAULT_VM_TYPE));

        VMStaticParams vmStaticParams = new VMStaticParams();
        vmStaticParams.setMips(100);
        vmStaticParams.setCores(2);
        vmStaticParams.setPrice(0.40);

        VM vm = new VM(vmStaticParams, cloudsim);

        VMDriver driver = new VMDriver(vm, cloudsim);
        driver.setJobs(new Job[] { j1, j2 });

        cloudsim.startSimulation();

        assertEquals(0.0, j1.getReleaseTime(), 0.0);
        assertEquals(0.0, j1.getSubmitTime(), 0.0);
        assertEquals(0.0, j1.getStartTime(), 0.0);
        assertEquals(10.0, j1.getFinishTime(), 0.0);

        assertEquals(0.0, j2.getReleaseTime(), 0.0);
        assertEquals(0.0, j2.getSubmitTime(), 0.0);
        assertEquals(0.0, j2.getStartTime(), 0.0);
        assertEquals(10.0, j2.getFinishTime(), 0.0);
    }
}
