package br.gov.modelos;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;

@Stateless
public class ProcessoEJB {

	@PersistenceContext
	private EntityManager entity;
	
	public List<ProcessoIniciado> buscarProcessosPorSituacao(ProcessoSituacao situacao){
		return entity.createQuery("from ProcessoIniciado where situacao = :idSituacao order by prioridade desc", ProcessoIniciado.class)
						.setParameter("idSituacao", situacao.getId())
						.getResultList();
	}
	
	public boolean atualizaSituacaoProcesso(ProcessoIniciado processo, ProcessoSituacao situacao){
		int result = entity.createQuery("update ProcessoIniciado set situacao = :situacao where id = :processoId ")
						.setParameter("situacao", situacao.getId())
						.setParameter("processoId", processo.getId())
						.executeUpdate();
		return result >= 1;
	}
	
	public int buscarLimitePorProcesso(Processo processo){
		return entity.createQuery("select limite from Processo where id = :id", Integer.class)
						.setParameter("id", processo.getId())
						.getSingleResult();
	}
	
	public List<ProcessoIniciado> buscarProcessosPorSituacao(Processo processo, ProcessoSituacao situacao) {
		return entity.createQuery("from ProcessoIniciado where processo.id = :processoId and situacao = :situacao", ProcessoIniciado.class)
						.setParameter("processoId", processo.getId())
						.setParameter("situacao", situacao.getId())
						.getResultList();
	}
}
