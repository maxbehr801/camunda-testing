package info.maxbehr.bpm.camundatesting.zpt.bpmn.usertask;

import info.maxbehr.bpm.camundatesting.zpt.helper.ProcessTestHelper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.search.response.SearchQueryResponse;
import io.camunda.zeebe.client.api.search.response.UserTask;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@ZeebeProcessTest
class UserTaskProcessTest {

    private static final String USERTASK_PROCESS_FILE = "bpmn/usertask/userTaskProcess.bpmn";
    private static final String USERTASK_PROCESS_FILE_ZEEBE = "bpmn/usertask/userTaskProcess_ZeebeUserTask.bpmn";
    private static final String USERTASK_PROCESS_ID = "Process_UserTaskProcess";
    private static final String USERTASK_PROCESS_ID_ZEEBE = "Process_UserTaskProcessZeebeUserTask";
    private static final String USER_TASK = "Task_UserTask";
    private static final String USER_TASK_ZEEBE = "Task_UserTask_Zeebe";

    private ZeebeClient client;
    private ZeebeTestEngine engine;
    private ProcessTestHelper testHelper;

    @BeforeEach
    void setup() {
        testHelper = new ProcessTestHelper(client, engine);
        client.newDeployResourceCommand().addResourceFromClasspath(USERTASK_PROCESS_FILE).send().join();
        client.newDeployResourceCommand().addResourceFromClasspath(USERTASK_PROCESS_FILE_ZEEBE).send().join();
    }

    // Prozessmodell enthält einen Camunda User Task - modelliert mit Modeler 5.33.0
    @Test
    void testUserTask() {
        ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(USERTASK_PROCESS_ID)
                .latestVersion()
                .send()
                .join();
        testHelper.waitForIdleState();
        assertThat(processInstance).isWaitingAtElements(USER_TASK);
        testHelper.completeUserTask();
        testHelper.waitForIdleState();
        assertThat(processInstance).isCompleted();
    }

    // Prozessmodell enthält einen Zeebe User Task - modelliert mit Modeler 5.19.0
    @Test
    void testZeebeUserTask() {
        ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(USERTASK_PROCESS_ID_ZEEBE)
                .latestVersion()
                .send()
                .join();
        testHelper.waitForIdleState();
        assertThat(processInstance).isWaitingAtElements(USER_TASK_ZEEBE);
        testHelper.completeUserTask();
        testHelper.waitForIdleState();
        assertThat(processInstance).isCompleted();
    }
}
