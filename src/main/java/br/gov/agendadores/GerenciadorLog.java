package br.gov.agendadores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.ejb.Stateless;

import org.jboss.util.file.Files;

import br.gov.model.batch.ProcessoIniciado;

@Stateless
public class GerenciadorLog {

	private static final String ROOT_PATH = "/var/tmp/";
	
	public void reiniciaLog(ProcessoIniciado processoIniciado) {
	    File file = new File(ROOT_PATH + processoIniciado.getId() + "_iniciado.log");

	    File file2 = new File(ROOT_PATH + processoIniciado.getId() + ".log");
	    if(file2.exists()) {
	    	Files.delete(file2);
	    }

	    file.renameTo(file2);
	    
	    gravandoLog(processoIniciado.getId().toString(), "### REINICIADO ###", false);
	}
	
	public void gravandoLog(String idProcesso, String mensagem) {
		gravandoLog(idProcesso, mensagem, true);
	}
	
	public void gravandoLog(String idProcesso, String mensagem, boolean append) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(ROOT_PATH + idProcesso + "_iniciado.log", append), "UTF-8"));
			
			bw.write(mensagem);
			bw.newLine();
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
