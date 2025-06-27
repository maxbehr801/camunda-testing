package info.maxbehr.bpm.camundatesting.zpt.helper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.CompleteJobResponse;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.List;
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

    /**
     * Wichtige Hinweise:
     * Die Job Worker-Implementierung für User Tasks ist veraltet (deprecated). Camunda empfiehlt, stattdessen die neue "Camunda User Task"-Implementierung zu verwenden, da diese mehr Features und eine bessere API bietet [Job worker implementation].
     * Mit Camunda User Tasks (ab Version 8.6/8.7) werden User Tasks direkt von der Engine verwaltet und nicht mehr als Jobs behandelt. Die Verwaltung und das Abschließen erfolgt dann über die Camunda 8 API, nicht mehr über Job Worker [Migrate to Camunda user tasks].
     * Zusammengefasst:
     * io.camunda.zeebe:userTask ist der Job-Typ für klassische, Job Worker-basierte User Tasks in Camunda 8. Für neue Projekte solltest du aber die Camunda User Task-Implementierung nutzen, da die Job Worker-Variante bald entfernt wird [User tasks].
     */
    public void completeUserTask() {
        List<ActivatedJob> jobs = client.newActivateJobsCommand()
                .jobType("io.camunda.zeebe:userTask")
                .maxJobsToActivate(1)
                .send()
                .join()
                .getJobs();
        ActivatedJob job = jobs.get(0);
        client.newCompleteCommand(job.getKey())
                .send()
                .join();
    }

    public void waitForIdleState() {
        waitForIdleState(Duration.ofSeconds(3));
    }

    @SneakyThrows
    public void waitForIdleState(Duration duration) {
        engine.waitForIdleState(duration);
    }
}