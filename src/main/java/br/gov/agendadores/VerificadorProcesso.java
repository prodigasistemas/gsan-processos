package br.gov.agendadores;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class VerificadorProcesso implements Job {
	
	private Logger logger = Logger.getLogger(VerificadorProcesso.class);
	
    @Override  
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	logger.info("Quartz test job executed!");
    }  
}
