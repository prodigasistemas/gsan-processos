package br.gov.agendadores;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

import br.gov.mensageiros.ProcessoMensageiro;
import br.gov.model.seguranca.SegurancaParametro;
import br.gov.servicos.seguranca.SegurancaParametroRepositorio;

@Stateless
public class GerenciadorLog {
    private static Logger logger = Logger.getLogger(ProcessoMensageiro.class);

	@EJB
	private SegurancaParametroRepositorio repositorioParametros;
	
	public void gravandoLog(String idProcesso, String mensagem) {
		gravandoLog(idProcesso, mensagem, true);
	}
	
	public void gravandoLog(String idProcesso, String mensagem, boolean append) {
	    String path = repositorioParametros.recuperaPeloNome(SegurancaParametro.NOME_PARAMETRO_SEGURANCA.CAMINHO_LOG_BATCH);
	    
		try {

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(path + idProcesso + "_iniciado.log", append), "UTF-8"));
			
			bw.write(mensagem);
			bw.newLine();
			
			bw.close();
		} catch (IOException e) {
		    logger.error("Erro ao gravar log", e);
		}
    }
}
