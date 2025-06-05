package info.maxbehr.bpm.camundatesting.zpt.helper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.CompleteJobResponse;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.Map;

@RequiredArgsConstructor
public class ProcessTestHelper {

    private final ZeebeClient client;
    private final ZeebeTestEngine engine;

    public ActivatedJob activateJob(String jobtype) {
        ActivateJobsResponse response = client.newActivateJobsCommand()
                .jobType(jobtype)
                .maxJobsToActivate(1)
                .send()
                .join();
        waitForIdleState(Duration.ofSeconds(1));
        return response.getJobs().get(0);
    }

    public void completeTaskForJob(ActivatedJob activatedJob, Map<String, Object> variables) {
        @SuppressWarnings("unused")
        CompleteJobResponse completeJobResponse = client.newCompleteCommand(activatedJob)
                .variables(variables)
                .send()
                .join();
        waitForIdleState(Duration.ofSeconds(1));
    }

    public void completeTaskForJob(ActivatedJob activatedJob) {
        @SuppressWarnings("unused")
        CompleteJobResponse completeJobResponse = client.newCompleteCommand(activatedJob)
                .send()
                .join();
        waitForIdleState(Duration.ofSeconds(1));
    }

    public void waitForIdleState() {
        waitForIdleState(Duration.ofSeconds(3));
    }

    @SneakyThrows
    public void waitForIdleState(Duration duration) {
        engine.waitForIdleState(duration);
    }
}