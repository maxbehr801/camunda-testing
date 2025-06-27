package info.maxbehr.bpm.camundatesting.zpt.bpmn.process;

import info.maxbehr.bpm.camundatesting.zpt.helper.ProcessTestHelper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@ZeebeProcessTest
class TestStartInTheMiddlePrcess {

    private static final String STARTINTHEMIDDLE_PROCESS_FILE = "bpmn/process/StartInTheMiddleProcess.bpmn";
    private static final String STARTINTHEMIDDLE_PROCESS_ID = "Process_StartInTheMiddleProcess";
    private static final String TASK_FIRST_TASK = "Task_doFirstTask";
    private static final String TASK_SECOND_TASK = "Task_doSecondTask";
    private static final String SECOND_TASK_TYPE = "secondTask";

    private ZeebeClient client;
    private ZeebeTestEngine engine;
    private ProcessTestHelper testHelper;

    @BeforeEach
    void setup() {
        testHelper = new ProcessTestHelper(client, engine);
        client.newDeployResourceCommand().addResourceFromClasspath(STARTINTHEMIDDLE_PROCESS_FILE).send().join();
    }

    @Test
    void testStartInTheMiddle() {
        ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(STARTINTHEMIDDLE_PROCESS_ID)
                .latestVersion()
                .startBeforeElement(TASK_SECOND_TASK)
                .send()
                .join();
        assertThat(processInstance).isWaitingAtElements(TASK_SECOND_TASK);
        ActivatedJob activatedJob = testHelper.activateJob(SECOND_TASK_TYPE);
        testHelper.completeTaskForJob(activatedJob);
        assertThat(processInstance).isCompleted();
        assertThat(processInstance).hasPassedElement(TASK_SECOND_TASK);
        assertThat(processInstance).hasNotPassedElement(TASK_FIRST_TASK);
    }
}
