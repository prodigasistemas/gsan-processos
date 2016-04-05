package br.gov.agendadores;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoRepositorio;

public class VerificadorProcessoTest {
    
    @Mock
    ProcessoRepositorio repositorioProcesso;
    
    @InjectMocks
    VerificadorProcesso verificadorProcesso;
    
    List<ProcessoIniciado> processos ;
    
    Date ontem, amanha;
    
    @Before
    public void init(){
        verificadorProcesso = new VerificadorProcesso();
        
        processos = new ArrayList<ProcessoIniciado>();
        
        MockitoAnnotations.initMocks(this);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        ontem = calendar.getTime();
        
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        amanha = calendar.getTime();
    }
    
    @Test
    public void naoHaProcessosAgendados(){
        when(repositorioProcesso.buscarProcessosPorSituacao(ProcessoSituacao.AGENDADO)).thenReturn(processos);
        
        verificadorProcesso.verificarProcessosAgendados();
        
        assertTrue(verificadorProcesso.processosProcessados.isEmpty());
    }
    
    @Test
    public void existeProcessoAgendadoMasNaoDisponivelParaProcessar(){
        ProcessoIniciado processo = new ProcessoIniciado();
        processo.setAgendamento(amanha);
        processos.add(processo);
        
        when(repositorioProcesso.buscarProcessosPorSituacao(ProcessoSituacao.AGENDADO)).thenReturn(processos);
        
        verificadorProcesso.verificarProcessosAgendados();
        
        assertTrue(verificadorProcesso.processosProcessados.isEmpty());
    }
    
    @Test
    public void existeProcessoAgendadoProntoParaProcessar(){
        ProcessoIniciado processo = new ProcessoIniciado();
        processo.setAgendamento(ontem);
        processo.setControleAtividades(new ArrayList<ControleProcessoAtividade>());
        processos.add(processo);
        
        when(repositorioProcesso.buscarProcessosPorSituacao(ProcessoSituacao.AGENDADO)).thenReturn(processos);
        when(repositorioProcesso.atualizaSituacaoProcesso(processo, ProcessoSituacao.EM_ESPERA)).thenReturn(true);
        
        verificadorProcesso.verificarProcessosAgendados();
        
        assertFalse(verificadorProcesso.processosProcessados.isEmpty());
    }
}
