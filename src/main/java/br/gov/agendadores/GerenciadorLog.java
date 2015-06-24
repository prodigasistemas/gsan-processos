package br.gov.agendadores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

import br.gov.mensageiros.ProcessoMensageiro;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.seguranca.SegurancaParametro;
import br.gov.servicos.seguranca.SegurancaParametroRepositorio;

@Stateless
public class GerenciadorLog {
    private static Logger logger = Logger.getLogger(ProcessoMensageiro.class);

	private String ROOT_PATH = "/var/tmp/";
	
	@EJB
	private SegurancaParametroRepositorio repositorioParametros;
	
	public void reiniciaLog(ProcessoIniciado processoIniciado) {
	    ROOT_PATH = repositorioParametros.recuperaPeloNome(SegurancaParametro.NOME_PARAMETRO_SEGURANCA.CAMINHO_LOG_BATCH);
	    
	    File file = new File(ROOT_PATH + processoIniciado.getId() + "_iniciado.log");

	    File logFile = new File(ROOT_PATH + processoIniciado.getId() + ".log");
	    
	    if(logFile.exists()) {
	    	logFile.delete();
	    }

	    file.renameTo(logFile);
	}
	
	public void gravandoLog(String idProcesso, String mensagem) {
		gravandoLog(idProcesso, mensagem, true);
	}
	
	public void gravandoLog(String idProcesso, String mensagem, boolean append) {
	    ROOT_PATH = repositorioParametros.recuperaPeloNome(SegurancaParametro.NOME_PARAMETRO_SEGURANCA.CAMINHO_LOG_BATCH);
	    
		try {

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(ROOT_PATH + idProcesso + "_iniciado.log", append), "UTF-8"));
			
			bw.write(mensagem);
			bw.newLine();
			
			bw.close();
		} catch (IOException e) {
		    logger.error("Erro ao gravar log", e);
		}
    }
}
