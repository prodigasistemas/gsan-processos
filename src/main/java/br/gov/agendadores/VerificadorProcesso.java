package br.gov.agendadores;

import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;

import org.apache.log4j.Logger;

import br.gov.mensageiros.ProcessoMensageiro;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.modelos.ProcessoEJB;


@JMSDestinationDefinitions({
	 @JMSDestinationDefinition(name = "java:global/jms/processosFila",
	 interfaceName = "javax.jms.Queue",
	 destinationName="processosFila",
	 description="Fila de Processos")
})
@Stateless
public class VerificadorProcesso {
	
	@EJB private ProcessoEJB processoEJB;
	@EJB private ProcessoMensageiro sender;
	
	private List<ProcessoIniciado> processosProcessados;
	private Logger logger = Logger.getLogger(VerificadorProcesso.class);
	
	@Schedule(second="20", minute="*",hour="*", persistent=false)
    public void verificarProcessosAgendados() {
		List<ProcessoIniciado> processos = processoEJB.buscarProcessosPorSituacao(ProcessoSituacao.AGENDADO);
		
		processosProcessados = new ArrayList<ProcessoIniciado>();
		for (ProcessoIniciado processoIniciado : processos) {
			if(prontoParaProcessar(processoIniciado)){
				processoEJB.atualizaSituacaoProcesso(processoIniciado, ProcessoSituacao.EM_ESPERA);
				processosProcessados.add(processoIniciado);
			}
		}
		
		logger.info("Processos [ " + Arrays.toString(processosProcessados.toArray()) + "] alterados para EM ESPERA!");
	}
	
	private boolean prontoParaProcessar(ProcessoIniciado processoIniciado) {
		return processoIniciado.getAgendamento().before(new Date());
	}

	@Schedule(second="30", minute="*",hour="*", persistent=false)
    public void verificarProcessosEmEspera() {
    	List<ProcessoIniciado> processos = processoEJB.buscarProcessosPorSituacao(ProcessoSituacao.EM_ESPERA);
    	
    	processosProcessados = new ArrayList<ProcessoIniciado>();
    	for (ProcessoIniciado processoIniciado : processos) {
    		if(limitePermitido(processoIniciado)){
    			sender.enviarParaFila(processoIniciado);
        		processoEJB.atualizaSituacaoProcesso(processoIniciado, ProcessoSituacao.EM_FILA);
        		processosProcessados.add(processoIniciado);
    		}
		}
    	
    	logger.info("Processos [ " + Arrays.toString(processosProcessados.toArray()) + "] enviados para fila!");
    }

	private boolean limitePermitido(ProcessoIniciado processoIniciado) {
		int limitePermitido = processoEJB.buscarLimitePorProcesso(processoIniciado.getProcesso());
		
		if(limitePermitido == 0){ 
			return true;
		}
		
		List<ProcessoIniciado> processosIniciados = processoEJB.buscarProcessosPorSituacao(processoIniciado.getProcesso(), ProcessoSituacao.EM_FILA);
		
		return (limitePermitido - processosIniciados.size()) > 0;
	}
}