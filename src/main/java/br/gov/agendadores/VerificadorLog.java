package br.gov.agendadores;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.logging.Logger;

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

	static final Logger logger = Logger.getLogger("VerificadorLog");

    @Override
    public void onMessage(Message inMessage) {
        try {
        	String mensagem = inMessage.getBody(String.class);
        	String idProcesso = mensagem.split(" :: ")[0];
        	
            gravandoLog(idProcesso, mensagem.substring(mensagem.indexOf(" :: ") + 3));
            
        } catch (JMSException e) {
        	gravandoLog("error", e.getMessage());
        }
    }
    
    private void gravandoLog(String idProcesso, String mensagem) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream("/var/tmp/" + idProcesso + "_iniciado.log", true), "UTF-8"));
			
			bw.write(mensagem);
			bw.newLine();
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
