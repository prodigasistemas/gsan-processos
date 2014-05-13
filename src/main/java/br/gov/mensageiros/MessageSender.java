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
public class MessageSender {

	@Inject
	@JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
	JMSContext context;

	@Resource(mappedName="java:global/jms/myQueue")
	Queue queue;

	public void sendMessage(ProcessoIniciado processoIniciado) {
		ObjectMessage message = context.createObjectMessage(processoIniciado);
		context.createProducer().send(queue, message);
	}
}