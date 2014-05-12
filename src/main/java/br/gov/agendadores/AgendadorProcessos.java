package br.gov.agendadores;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

@Singleton
@Startup
public class AgendadorProcessos {

	private Logger log = Logger.getLogger(AgendadorProcessos.class);

	@PostConstruct
	public void init() throws IOException, SchedulerException {

		Scheduler scheduler = new StdSchedulerFactory().getScheduler();  
		
		log.info("Starting up PreprocessorScheduler.");

		JobDetail verificador = JobBuilder.newJob(VerificadorProcesso.class)  
								        .withIdentity("verificaProcesso", "verificadores")  
								        .build();  
         
        SimpleTrigger trigger = TriggerBuilder.newTrigger()  
										        .withIdentity("processoTrigger", "verificadores")  
										        .withSchedule(SimpleScheduleBuilder.simpleSchedule()  
										        .withIntervalInSeconds(10)  
										        .repeatForever())  
										        .build();  
         
        scheduler.scheduleJob(verificador, trigger);  
        scheduler.start();  

		log.info("Started up PreprocessorScheduler successfully.");
	}
}