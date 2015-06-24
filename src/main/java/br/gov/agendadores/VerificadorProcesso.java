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
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.ProcessoAtividade;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;
import br.gov.servicos.batch.ProcessoAtividadeRepositorio;
import br.gov.servicos.batch.ProcessoParametroRepositorio;
import br.gov.servicos.batch.ProcessoRepositorio;
import br.gov.servicos.to.MensagemAtividadeTO;

@Stateless
public class VerificadorProcesso {
	
	@EJB private GerenciadorLog gerenciadorLog;
	@EJB private ProcessoRepositorio repositorioProcesso;
	@EJB private ControleProcessoAtividadeRepositorio controleAtividade;
	@EJB private ProcessoParametroRepositorio repositorioParametrosProcesso;
	@EJB private ProcessoAtividadeRepositorio processoAtividadeRepositorio;
	@EJB private ProcessoMensageiro sender;
	
	private List<ProcessoIniciado> processosProcessados;
	private Logger logger = Logger.getLogger(VerificadorProcesso.class);
	
	@Schedule(minute="*/10",hour="*", persistent=false)
    public void verificarProcessosAgendados() {
		List<ProcessoIniciado> processos = repositorioProcesso.buscarProcessosPorSituacao(ProcessoSituacao.AGENDADO);
		
		processosProcessados = new ArrayList<ProcessoIniciado>();
		for (ProcessoIniciado processoIniciado : processos) {
			if(prontoParaProcessar(processoIniciado)){
				repositorioProcesso.atualizaSituacaoProcesso(processoIniciado, ProcessoSituacao.EM_ESPERA);
				processoIniciado.getControleAtividades().forEach(e -> {
				    controleAtividade.atualizaSituacaoAtividade(e.getId(), ProcessoSituacao.EM_ESPERA);
				});
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
    public void chamarAtividadeDependente() {
        List<ControleProcessoAtividade> concluidas = controleAtividade.buscarAtividadesComTodosItensProcessados();
        
        for (ControleProcessoAtividade concluida : concluidas) {
            controleAtividade.atualizaSituacaoAtividade(concluida.getId(), ProcessoSituacao.CONCLUIDO);

            ProcessoAtividade dependente = processoAtividadeRepositorio.obterDependencia(concluida.getAtividade().getId());
            
            if (dependente != null){
                ControleProcessoAtividade sub = controleAtividade.obterExecucaoExistente(
                        concluida.getProcessoIniciado().getId()
                        , dependente.getId());
                
                controleAtividade.atualizaSituacaoAtividade(sub.getId(), ProcessoSituacao.EM_FILA);
                
                sender.enviarParaFila(new MensagemAtividadeTO().build(sub));
            }
        }
    }

    @Schedule(second="*/30",minute="*",hour="*", persistent=false)
    public void marcarProcessoComoConcluido() {
        
        List<ProcessoIniciado> processos= repositorioProcesso.buscarProcessosPorSituacao(ProcessoSituacao.EM_PROCESSAMENTO);
        
        processos.forEach(processo -> {
            boolean concluido = true;
            for(ControleProcessoAtividade ativ : processo.getControleAtividades()){
                if (!ativ.concluida()){
                    concluido = false;
                }
            }
            
            if (concluido){
                repositorioProcesso.atualizaSituacaoProcesso(processo, ProcessoSituacao.CONCLUIDO);
                
                processarRecorrencia(processo);
            }
        });
    }
    
	@Schedule(second="*/30",minute="*",hour="*", persistent=false)
    public void verificarAtividadesEmEspera() {
	    marcarAtividadeParaProcessar(ProcessoSituacao.EM_ESPERA);
    }
	
	@Schedule(second="*/30",minute="*",hour="*", persistent=false)
    public void verificarAtividadesReiniciadas() {
	    marcarAtividadeParaProcessar(ProcessoSituacao.REINICIADO);
    }
	
	private void marcarAtividadeParaProcessar(ProcessoSituacao situacao){
        List<ControleProcessoAtividade> atividades = controleAtividade.buscarAtividadesPorSituacao(situacao);
        
        List<ProcessoAtividade> enviadas = new ArrayList<ProcessoAtividade>();
        
        Integer idProcesso = -1;
        
        for (ControleProcessoAtividade controle : atividades) {
            if (!limitePermitido(controle.getProcessoIniciado()) || existeAtividadeEmExecucao(controle)){
                continue;
            }
            
            if (idProcesso != controle.getProcessoIniciado().getProcesso().getId().intValue()){
                idProcesso = controle.getProcessoIniciado().getProcesso().getId();
                
                controleAtividade.atualizaSituacaoAtividade(controle.getId(), ProcessoSituacao.EM_FILA);
                
                repositorioProcesso.atualizaSituacaoProcesso(controle.getProcessoIniciado().getId(), ProcessoSituacao.EM_FILA);
                
                if (situacao == ProcessoSituacao.EM_ESPERA){
                    repositorioParametrosProcesso.inserirParametrosDefault(controle.getProcessoIniciado());
                }
                
                enviadas.add(controle.getAtividade());
                
                sender.enviarParaFila(new MensagemAtividadeTO().build(controle));
            }
        }
        
        if(!enviadas.isEmpty()) {
            logger.info("Atividades [ " + Arrays.toString(enviadas.toArray()) + "] enviadas para fila!");
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
			
			repositorioProcesso.inserirProcesso(proximoProcesso);
			
			logger.info("Processos Reagendado: " + proximoProcesso);
		}
	}

	private boolean limitePermitido(ProcessoIniciado processoIniciado) {
		int limitePermitido = repositorioProcesso.buscarLimitePorProcesso(processoIniciado.getProcesso());
		
		if(limitePermitido == 0){ 
			return true;
		}
		
		List<ProcessoIniciado> processosIniciados = repositorioProcesso.buscarProcessosPorSituacao(processoIniciado.getProcesso(), ProcessoSituacao.EM_FILA);
		
		return (limitePermitido - processosIniciados.size()) > 0;
	}
	    
    private boolean existeAtividadeEmExecucao(ControleProcessoAtividade controle) {
        for (ControleProcessoAtividade execucao : controle.getProcessoIniciado().getControleAtividades()){
            if (execucao.getSituacao().intValue() == ProcessoSituacao.EM_PROCESSAMENTO.getId()){
                return true;
            }
        }
        
        return false;
    }    
}