package br.gov.agendadores;

import java.util.List;

import javax.ejb.EJB;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.gov.mensageiros.MessageSender;
import br.gov.model.ProcessoIniciado;
import br.gov.modelos.ProcessoEJB;

@JMSDestinationDefinitions({
	 @JMSDestinationDefinition(name = "java:global/jms/myQueue",
	 interfaceName = "javax.jms.Queue",
	 destinationName="queue1234",
	 description="My Queue")
})
public class VerificadorProcesso implements Job {
	
	@EJB
	private ProcessoEJB processoEJB;
	
	@EJB 
	private MessageSender sender;
	
	private Logger logger = Logger.getLogger(VerificadorProcesso.class);
	
    @Override  
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	List<ProcessoIniciado> processos = processoEJB.buscarProcessosEmEspera();
    	
    	for (ProcessoIniciado processoIniciado : processos) {
    		sender.sendMessage(processoIniciado);
    		logger.info("Processo [id: " + processoIniciado.getId() + "] enviado!");
		}
    }  
}
