import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class RoundRobin {
    public static void main(String[] args) {
        Queue<Processo> filaDeProcessos = new LinkedList<>(); // Fila de processos
        List<Processo> listaDeProcessos = new ArrayList<>();
         // Quantum (tempo de execução para cada processo)

        Scanner scanner = new Scanner(System.in);
        System.out.println("Insira o tempo da troca de contexto: ");
        int trocaContexto = scanner.nextInt();

        System.out.println("Insira o quantum: ");
        int quantum = scanner.nextInt();

        System.out.println("Insira a quantidade de processos: ");
        int qntProcessos = scanner.nextInt();


        // inserir nomes e tempos de ingresso dos processos
        for (int i = 1; i <= qntProcessos; i++) {
            System.out.print("Insira a duração do Processo " + i + ": ");
            int tempoProcesso = scanner.nextInt();
            System.out.print("Insira o tempo de ingresso do Processo " + i + ": ");
            int tempoIngresso = scanner.nextInt();
            scanner.nextLine(); 
            listaDeProcessos.add(new Processo(tempoProcesso, tempoIngresso, i));
            
        }

        // Adcionando cada um na fila de processo de acordo com o tempo de ingresso
        listaDeProcessos = listaDeProcessos.stream().sorted((p1, p2) -> p1.tempoIngresso.compareTo(p2.tempoIngresso)).toList();
        //ordena os tempos de ingresso para começar pelo processo com o menor tempo
           
        // Fazendo com que o tempo de ingresso dos processos sejam sempre relativos ao menor tempo de ingresso
        // Isso quer dizer que se existem 2 processos A e B em que A entra no tempo 1 e B no tempo 4, na verdade eles entram nos tempos 0 e 3
        if(listaDeProcessos.get(0).tempoIngresso != 0){
            int min = listaDeProcessos.get(0).tempoIngresso;
            for (Processo processo : listaDeProcessos) {
                processo.tempoIngresso -= min;
            }
        }

        listaDeProcessos.forEach(pAtual -> filaDeProcessos.add(pAtual));

        scanner.close();
        // Executar o algoritmo RR e obter resultados
        ResultadosRoundRobin resultados = escalonadorRoundRobin(filaDeProcessos, quantum, trocaContexto);

        // Exibir os resultados
        System.out.println("Tempo Médio de Vida: " + resultados.tempoMedioVida);
        System.out.println("Tempo Médio de Espera: " + resultados.tempoMedioEspera);

        scanner.close();
    }

    // Estrutura para representar um processo
    private static class Processo {
        int nome;
        Integer tempoProcesso;
        Integer tempoIngresso;
        Integer tempoDeExecucao = 0;
        Integer tempoDeEspera = 0;

        Processo(int tempoProcesso, int tempoIngresso, int nome) {
            this.nome = nome;
            this.tempoProcesso = tempoProcesso;
            this.tempoIngresso = tempoIngresso;
        }

        boolean estaAtivo(){
            //verifica se o processo está finalizado(se ja foi morto)
            return tempoProcesso > 0;
        }
    }

    // Estrutura para armazenar os resultados do algoritmo Round Robin
    private static class ResultadosRoundRobin {
        double tempoMedioVida;
        double tempoMedioEspera;

        ResultadosRoundRobin(double tempoMedioVida, double tempoMedioEspera) {
            this.tempoMedioVida = tempoMedioVida;
            this.tempoMedioEspera = tempoMedioEspera;
        }
    }

    // Função para executar o algoritmo Round Robin e calcular os resultados
    private static ResultadosRoundRobin escalonadorRoundRobin(Queue<Processo> filaDeProcessos, int quantum, int trocaDeContexto ) {
        
        int tempoAtual = 0;
        Scanner sc = new Scanner(System.in);
        List<Processo> processosFinalizados = new ArrayList<>();

        while (!filaDeProcessos.isEmpty()) {
            Processo processoAtual = filaDeProcessos.poll(); // Obtém o próximo processo na fila

            if(processoAtual.tempoIngresso > tempoAtual && filaDeProcessos.size() > 0){
                // Processo não pode rodar ainda : Não ingressou
                filaDeProcessos.add(processoAtual);
                continue;
            }

            // Executa o processo por um tempo igual ao quantum ou até sua conclusão
            // Simulação de tempo de execução
            int tempoExecutado = filaDeProcessos.size() > 0 ? Math.min(quantum, processoAtual.tempoProcesso) : processoAtual.tempoProcesso; // Tempo que o processo executa na volta
            System.out.println("Executando " + processoAtual.nome + " por " + tempoExecutado + " unidades de tempo : total: " + processoAtual.tempoProcesso);
            
            // Remove o tempo que ele já foi processado(tempo do processo - quantum)
            processoAtual.tempoProcesso -= tempoExecutado;

            // Adciono o tempo que ele executou no seu tempo de Execução total
            processoAtual.tempoDeExecucao += tempoExecutado;

            // Aumentando o tempo de espera dos demais processos, que não estão executando
            filaDeProcessos.forEach(processo -> {
                if(!processo.equals(processoAtual)) {
                    processo.tempoDeEspera += tempoExecutado;
                }
            });


            // Verifica se o processo ainda tem trabalho a ser feito
            if (processoAtual.estaAtivo() && tempoExecutado > 0) {
                // Se sim, adiciona o processo de volta à fila com o restante do trabalho
                filaDeProcessos.add(processoAtual);
                System.out.println(processoAtual.nome + " ainda tem trabalho a ser feito. Adicionando de volta à fila. -> Restante: " + processoAtual.tempoProcesso);
            } else {
                // Processo finalizado adcionado na lista de finalizados
                processosFinalizados.add(processoAtual);
                System.out.println(processoAtual.nome + " concluído.");
            }

            // Verifico os processos que estão ativos e adciono a todos a troca de contexto no tempo de espera
            filaDeProcessos.forEach(pAtual -> {
                if(pAtual.estaAtivo()){
                    pAtual.tempoDeEspera += filaDeProcessos.size() > 0? trocaDeContexto : 0;
                }
            });

            // Somando o tempo total para determinar se o processo pode executar
            tempoAtual += tempoExecutado  + ((filaDeProcessos.size() > 0)? trocaDeContexto : 0);
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Ação interrompida pelo usuário");
            }
        
        }

        // Calcula os tempos médios
        double tempoTotalVida = 0;
        double tempoTotalEspera = 0;
        for(Processo p : processosFinalizados){
            tempoTotalEspera += p.tempoDeEspera - p.tempoIngresso;
            tempoTotalVida += p.tempoDeEspera + p.tempoDeExecucao - p.tempoIngresso;
        }

        double tempoMedioEspera = tempoTotalEspera / processosFinalizados.size();
        double tempoMedioVida = tempoTotalVida / processosFinalizados.size();


        return new ResultadosRoundRobin(tempoMedioVida, tempoMedioEspera);
    }
}
