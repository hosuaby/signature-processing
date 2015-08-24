package io.hosuaby.signatures.batch;

import java.util.Date;
import java.util.HashMap;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Batch job scheduler.
 */
@Component
public class JobScheduler {

    /** Job execution rate */
    private static final int RATE = 15000;  // 15 sec

    /** Job launcher */
    @Autowired
    private JobLauncher jobLauncher;

    /** Job */
    @Autowired
    private Job job;

    /**
     * Runs the scheduled job.
     *
     * @throws JobParametersInvalidException
     * @throws JobInstanceAlreadyCompleteException
     * @throws JobRestartException
     * @throws JobExecutionAlreadyRunningException
     */
    @Scheduled(fixedRate = RATE)
    public void run() throws JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException,
            JobParametersInvalidException {
        JobParameters params = new JobParameters(new HashMap<String, JobParameter>() {
            private static final long serialVersionUID = 1L;
            {
                put("execDate", new JobParameter(new Date(), true));
            }
        });

        jobLauncher.run(job, params);
    }

}
