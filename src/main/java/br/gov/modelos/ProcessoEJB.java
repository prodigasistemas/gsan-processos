package br.gov.modelos;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;

@Stateless
public class ProcessoEJB {

	@PersistenceContext
	private EntityManager entity;
	
	public List<ProcessoIniciado> buscarProcessosEmEspera(){
		return entity.createQuery("from ProcessoIniciado where situacao = :idSituacao", ProcessoIniciado.class)
						.setParameter("idSituacao", ProcessoSituacao.EM_ESPERA.getId())
						.getResultList();
	}
}
