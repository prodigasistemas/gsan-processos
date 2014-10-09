package br.gov.agendadores;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@JMSDestinationDefinitions({
	 @JMSDestinationDefinition(name = "java:global/jms/loggerProcessos",
	 interfaceName = "javax.jms.Queue",
	 destinationName="loggerProcessos",
	 description="Log de Processos")
})
@MessageDriven(
	activationConfig = { 
			@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:global/jms/loggerProcessos") 
	}
)
public class VerificadorLog implements MessageListener {

	@EJB
	private GerenciadorLog gerenciadorLog;
	
    @Override
    public void onMessage(Message inMessage) {
        try {
        	String mensagem = inMessage.getBody(String.class);
        	String idProcesso = mensagem.split(" :: ")[0];
        	
            gerenciadorLog.gravandoLog(idProcesso, mensagem.substring(mensagem.indexOf(" :: ") + 3));
            
        } catch (JMSException e) {
        	gerenciadorLog.gravandoLog("error", e.getMessage());
        }
    }
}
