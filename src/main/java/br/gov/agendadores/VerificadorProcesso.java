package br.gov.agendadores;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;

import org.apache.log4j.Logger;

import br.gov.mensageiros.ProcessoMensageiro;
import br.gov.model.ProcessoIniciado;
import br.gov.modelos.ProcessoEJB;


@JMSDestinationDefinitions({
	 @JMSDestinationDefinition(name = "java:global/jms/myQueue",
	 interfaceName = "javax.jms.Queue",
	 destinationName="queue1234",
	 description="My Queue")
})
@Stateless
public class VerificadorProcesso {
	
	@EJB private ProcessoEJB processoEJB;
	@EJB private ProcessoMensageiro sender;
	
	private Logger logger = Logger.getLogger(VerificadorProcesso.class);
	
	@Schedule(second="30", minute="*",hour="*", persistent=false)
    public void verificar() {
    	List<ProcessoIniciado> processos = processoEJB.buscarProcessosEmEspera();
    	
    	for (ProcessoIniciado processoIniciado : processos) {
    		sender.enviarParaFila(processoIniciado);
    		logger.info("Processo [id: " + processoIniciado.getId() + "] enviado!");
		}
    }  
}
