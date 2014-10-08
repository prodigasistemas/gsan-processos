package br.gov.mensageiros;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import br.gov.model.batch.ProcessoIniciado;

@Stateless
public class ProcessoMensageiro {

	@Inject
	JMSContext contexto;

	@Resource(mappedName="java:global/jms/processosFila")
	Queue processosFila;

	public void enviarParaFila(ProcessoIniciado processoIniciado) {
		ObjectMessage mensagem = contexto.createObjectMessage(processoIniciado);
		contexto.createProducer().send(processosFila, mensagem);
	}
}