package br.gov.mensageiros;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import br.gov.model.ProcessoIniciado;

@Stateless
public class ProcessoMensageiro {

	@Inject
	@JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
	JMSContext contexto;

	@Resource(mappedName="java:global/jms/myQueue")
	Queue fila;

	public void enviarParaFila(ProcessoIniciado processoIniciado) {
		ObjectMessage mensagem = contexto.createObjectMessage(processoIniciado);
		contexto.createProducer().send(fila, mensagem);
	}
}