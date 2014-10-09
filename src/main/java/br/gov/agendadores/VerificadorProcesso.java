package br.gov.agendadores;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import br.gov.mensageiros.ProcessoMensageiro;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoParametroRepositorio;
import br.gov.servicos.batch.ProcessoRepositorio;

@Stateless
public class VerificadorProcesso {
	
	@EJB private ProcessoRepositorio processoEJB;
	@EJB private ProcessoParametroRepositorio processoParametroEJB;
	@EJB private ProcessoMensageiro sender;
	
	private List<ProcessoIniciado> processosProcessados;
	private Logger logger = Logger.getLogger(VerificadorProcesso.class);
	
	@Schedule(minute="10",hour="*", persistent=false)
    public void verificarProcessosAgendados() {
		List<ProcessoIniciado> processos = processoEJB.buscarProcessosPorSituacao(ProcessoSituacao.AGENDADO);
		
		processosProcessados = new ArrayList<ProcessoIniciado>();
		for (ProcessoIniciado processoIniciado : processos) {
			if(prontoParaProcessar(processoIniciado)){
				processoEJB.atualizaSituacaoProcesso(processoIniciado, ProcessoSituacao.EM_ESPERA);
				processosProcessados.add(processoIniciado);
			}
		}
		
		if(!processosProcessados.isEmpty()) {
    		logger.info("Processos [ " + Arrays.toString(processosProcessados.toArray()) + "] alterados para EM ESPERA!");
    	}
	}
	
	private boolean prontoParaProcessar(ProcessoIniciado processoIniciado) {
		return processoIniciado.getAgendamento().before(new Date());
	}

	@Schedule(second="*/30",minute="*",hour="*", persistent=false)
    public void verificarProcessosEmEspera() {
    	List<ProcessoIniciado> processos = processoEJB.buscarProcessosPorSituacao(ProcessoSituacao.EM_ESPERA);
    	
    	processosProcessados = new ArrayList<ProcessoIniciado>();
    	for (ProcessoIniciado processoIniciado : processos) {
    		if(limitePermitido(processoIniciado)){
    			sender.enviarParaFila(processoIniciado);
        		processoEJB.atualizaSituacaoProcesso(processoIniciado, ProcessoSituacao.EM_FILA);
        		processoParametroEJB.inserirParametrosDefault(processoIniciado);
        		processosProcessados.add(processoIniciado);
        		
        		processarRecorrencia(processoIniciado);
    		}
		}
    	
    	if(!processosProcessados.isEmpty()) {
    		logger.info("Processos [ " + Arrays.toString(processosProcessados.toArray()) + "] enviados para fila!");
    	}
    }
	
	@Schedule(second="*/30",minute="*",hour="*", persistent=false)
    public void verificarProcessosReiniciados() {
    	List<ProcessoIniciado> processos = processoEJB.buscarProcessosPorSituacao(ProcessoSituacao.REINICIADO);
    	
    	processosProcessados = new ArrayList<ProcessoIniciado>();
    	for (ProcessoIniciado processoIniciado : processos) {
    		if(limitePermitido(processoIniciado)){
    			sender.enviarParaFila(processoIniciado);
        		processoEJB.atualizaSituacaoProcesso(processoIniciado, ProcessoSituacao.EM_FILA);
        		processoParametroEJB.atualizarParametro(processoIniciado, "percentualProcessado", "1");
        		processosProcessados.add(processoIniciado);
    		}
		}
    	
    	if(!processosProcessados.isEmpty()) {
    		logger.info("Processos Reiniciados [ " + Arrays.toString(processosProcessados.toArray()) + "] enviados para fila!");
    	}
    }

	private void processarRecorrencia(ProcessoIniciado processoIniciado) {
		if(processoIniciado.getProcesso().isRecorrente()) {
			ProcessoIniciado proximoProcesso = new ProcessoIniciado();
			proximoProcesso.setUsuario(processoIniciado.getUsuario());
			proximoProcesso.setPrioridade(processoIniciado.getPrioridade());
			proximoProcesso.setProcesso(processoIniciado.getProcesso());
			proximoProcesso.setProcessoPrecedente(processoIniciado.getProcessoPrecedente());
			proximoProcesso.setSituacao(ProcessoSituacao.AGENDADO.getId());
			proximoProcesso.setAgendamento(processoIniciado.getProcesso().calculaProximaExecucao());
			proximoProcesso.setUltimaAlteracao(Date.from(Instant.now()));
			
			processoEJB.inserirProcesso(proximoProcesso);
			
			logger.info("Processos Reagendado: " + proximoProcesso);
		}
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