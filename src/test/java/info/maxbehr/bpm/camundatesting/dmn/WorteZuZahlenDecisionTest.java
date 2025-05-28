package info.maxbehr.bpm.camundatesting.dmn;

import info.maxbehr.bpm.camundatesting.helper.ProcessTestHelper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@ZeebeProcessTest
class WorteZuZahlenDecisionTest {

    private static final String TESTPROZESS_WORTE_ZU_ZAHLEN_DECISION_FILE = "dmn/DecisionTestProcess.bpmn";
    private static final String WORTE_ZU_ZAHLEN_DECISION_FILE = "dmn/WorteZuZahlenDecision.dmn";
    private static final String BPMN_PROCESS_ID = "Process_DecisionTestProcess";

    private ZeebeClient client;
    private ZeebeTestEngine engine;
    private ProcessTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new ProcessTestHelper(client, engine);
        client.newDeployResourceCommand().addResourceFromClasspath(TESTPROZESS_WORTE_ZU_ZAHLEN_DECISION_FILE).send().join();
        client.newDeployResourceCommand().addResourceFromClasspath(WORTE_ZU_ZAHLEN_DECISION_FILE).send().join();
    }

    @ParameterizedTest
    @CsvSource({
            "eins,1", "zwei,2", "drei,3"
    })
    void shouldReturn1ForEins(String input, int expected) {
        var processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(BPMN_PROCESS_ID)
                .latestVersion()
                .variable("stringInput", input)
                .send()
                .join();
        assertThat(processInstance).isStarted();
        helper.waitForIdleState();
        assertThat(processInstance).isCompleted();
        assertThat(processInstance).hasVariableWithValue("outputVariable", expected);
    }

}
