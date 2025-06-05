package info.maxbehr.bpm.camundatesting.zpt.bpmn.events.message;


import info.maxbehr.bpm.camundatesting.zpt.helper.ProcessTestHelper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@ZeebeProcessTest
class MessageStartEventProcessTest {

    private static final String BPMN_PROCESS_ID = "Process_MessageStartEventProcess";
    private static final String MESSAGE_START_EVENT_PROCESS_FILE = "bpmn/events/MessageStartEventProcess.bpmn";
    private static final String MESSAGE_NAME = "Message_StartEvent";
    private static final String DO_SERVICE_JOB_TYPE = "doService";

    private ZeebeClient client;
    private ZeebeTestEngine engine;
    private ProcessTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new ProcessTestHelper(client, engine);
        client.newDeployResourceCommand().addResourceFromClasspath(MESSAGE_START_EVENT_PROCESS_FILE).send().join();
    }

    @Test
    void shouldStartProcessWithMessage() {
        var publishMessageResponse = client.newPublishMessageCommand()
                .messageName(MESSAGE_NAME)
                .withoutCorrelationKey()
                .variable("message", "Hello World!")
                .send()
                .join();

        var activatedJob = helper.activateJob(DO_SERVICE_JOB_TYPE);

        helper.completeTaskForJob(activatedJob, Map.of("nochwas", "true"));

        assertThat(publishMessageResponse).hasCreatedProcessInstance().extractingProcessInstance().hasVariableWithValue("nochwas", "true").hasPassedElementsInOrder("StartEvent_MessageStartEvent", "Activity_doService", "Event_End").isCompleted();
    }
}
