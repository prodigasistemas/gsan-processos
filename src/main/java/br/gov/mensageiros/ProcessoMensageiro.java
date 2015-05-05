package br.gov.mensageiros;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import org.jboss.logging.Logger;

import br.gov.servicos.to.MensagemAtividadeTO;

@Stateless
public class ProcessoMensageiro {
    private static Logger logger = Logger.getLogger(ProcessoMensageiro.class);

	@Inject
	@JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")
	JMSContext contexto;

	@Resource(mappedName="java:global/jms/processosFila")
	Queue processosFila;

	public void enviarParaFila(MensagemAtividadeTO mensagemTO) {
	    try {
	        ObjectMessage mensagem = contexto.createObjectMessage(mensagemTO);
	        contexto.createProducer().send(processosFila, mensagem);
        } catch (Exception e) {
            logger.error("Erro ao enviar processo para fila", e);
        }
	}
}