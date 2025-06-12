package info.maxbehr.bpm.camundatesting.zpt.bpmn.multiinstance;

import info.maxbehr.bpm.camundatesting.zpt.helper.ProcessTestHelper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@ZeebeProcessTest
class MultiInstanceProcessTest {

    private static final String MULTIINSTANCE_PROCESS_FILE = "bpmn/multiinstance/multiinstance-process.bpmn";
    private static final String MULTIINSTANCE_PROCESS_ID = "Process_MultiinstanceProcess";
    private static final String MOCK_PROCESS_FILE = "bpmnmock/multiinstance/mock-process.bpmn";
    private static final String MOCK_PROCESS_ID = "Process_MockProcess";
    private static final String MULTI_INSTANCE_CALL_ACTIVITY = "Activity_doParallelMultiinstance";


    private ZeebeClient client;
    private ZeebeTestEngine engine;
    private ProcessTestHelper testHelper;

    @BeforeEach
    void setup() {
        testHelper = new ProcessTestHelper(client, engine);
        client.newDeployResourceCommand().addResourceFromClasspath(MULTIINSTANCE_PROCESS_FILE).send().join();
        client.newDeployResourceCommand().addResourceFromClasspath(MOCK_PROCESS_FILE).send().join();
    }

    @Test
    void shouldStartTheSubprocessMultipleTimes() {
        List<String> inputCollection = List.of("item1", "item2");
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.put("collection", inputCollection);
        ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(MULTIINSTANCE_PROCESS_ID)
                .latestVersion()
                .variables(inputVariables)
                .send()
                .join();
        testHelper.waitForIdleState();
        assertThat(processInstance).isCompleted()
                .hasPassedElement(MULTI_INSTANCE_CALL_ACTIVITY, 3) // 2 mal als type CallActivity und 1 mal als type Multiinstance
                .hasCalledProcess(MOCK_PROCESS_ID);
    }
}
