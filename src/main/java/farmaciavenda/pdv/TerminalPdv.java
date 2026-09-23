package farmaciavenda.pdv;

import farmaciavenda.enums.FuncaoFuncionario;
import farmaciavenda.enums.MeioPagamento;
import farmaciavenda.enums.ModalidadeIntegracao;
import farmaciavenda.enums.PlanoCliente;
import farmaciavenda.enums.StatusVenda;
import farmaciavenda.enums.TipoProduto;
import farmaciavenda.exception.DescontoInvalidoException;
import farmaciavenda.exception.MedicamentoControladoException;
import farmaciavenda.exception.VendaFinalizadaException;
import farmaciavenda.models.Cliente;
import farmaciavenda.models.FormaPagamento;
import farmaciavenda.models.Funcionario;
import farmaciavenda.models.ItemVenda;
import farmaciavenda.models.Produto;
import farmaciavenda.models.Venda;
import farmaciavenda.repository.VendaRepository;
import farmaciavenda.service.VendaService;
import farmaciavenda.service.integracao.GatewayPagamentoClient;
import farmaciavenda.service.integracao.SngpcClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Terminal PDV interativo para simulação de vendas na farmácia.
 * Permite selecionar produtos diretamente durante a venda, adicionar múltiplos
 * itens, aplicar descontos, validar regras do SNGPC (ANVISA) e processar pagamentos
 * eletrônicos (TEF/POS).
 */
public class TerminalPdv {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Cadastros e catálogos em memória
    private static final List<Cliente> clientesCadastrados = new ArrayList<>();
    private static final List<Funcionario> funcionariosCadastrados = new ArrayList<>();
    private static final List<Produto> catalogoProdutos = new ArrayList<>();
    private static final List<FormaPagamento> formasPagamentoDisponiveis = new ArrayList<>();
    private static final List<Venda> historicoVendas = new ArrayList<>();

    // Serviço de negócio
    private static VendaService vendaService;

    // Atendente padrão logado no caixa
    private static Funcionario atendenteLogado;

    public static void main(String[] args) {
        inicializarDadosMockados();
        inicializarServico();

        System.out.println("==================================================================");
        System.out.println("            SISTEMA DE PDV - FARMÁCIA POPULAR                     ");
        System.out.println("==================================================================");

        boolean executando = true;
        while (executando) {
            exibirMenuPrincipal();
            String entrada = lerLinha("Escolha uma opção: ");

            switch (entrada.toLowerCase()) {
                case "1" -> iniciarFluxoVenda();
                case "2" -> listarCatalogoProdutos();
                case "3" -> listarHistoricoVendas();
                case "4" -> listarFuncionariosEClientes();
                case "5" -> trocarAtendente();
                case "0", "sair" -> {
                    System.out.println("\nFinalizando Terminal PDV. Até logo!");
                    executando = false;
                }
                default -> System.out.println("Opção inválida! Escolha uma opção do menu.");
            }
        }
    }

    // =========================================================================
    // INICIALIZAÇÃO DE DADOS MOCKADOS E SERVIÇOS
    // =========================================================================

    private static void inicializarDadosMockados() {
        // Clientes
        clientesCadastrados.add(new Cliente(1L, "João da Silva", "111.222.333-44", LocalDate.of(1985, 4, 12), PlanoCliente.SAUDE));
        clientesCadastrados.add(new Cliente(2L, "Maria Albuquerque", "222.333.444-55", LocalDate.of(1992, 8, 25), PlanoCliente.FIDELIDADE));
        clientesCadastrados.add(new Cliente(3L, "Carlos Eduardo", "333.444.555-66", LocalDate.of(2001, 1, 10), PlanoCliente.NENHUM));

        // Funcionários
        funcionariosCadastrados.add(new Funcionario(1L, "Lucas Lima", "444.555.666-77", FuncaoFuncionario.OPERADOR_CAIXA));
        funcionariosCadastrados.add(new Funcionario(2L, "Dra. Beatriz Santos", "555.666.777-88", "CRF-SP 98765", FuncaoFuncionario.FARMACEUTICO));
        funcionariosCadastrados.add(new Funcionario(3L, "Marcos Gerente", "666.777.888-99", FuncaoFuncionario.GERENTE));

        // Atendente padrão inicial: Caixa Lucas Lima
        atendenteLogado = funcionariosCadastrados.get(0);

        // Produtos (Controlados, Genéricos, Referência e Perfumaria)
        catalogoProdutos.add(new Produto(1L, "Rivotril 2mg (Clonazepam)", "Clonazepam", TipoProduto.REFERENCIA, true, new BigDecimal("32.50")));
        catalogoProdutos.add(new Produto(2L, "Diazepam 10mg (Genérico)", "Diazepam", TipoProduto.GENERICO, true, new BigDecimal("18.00")));
        catalogoProdutos.add(new Produto(3L, "Dipirona 500mg Gotas", "Dipirona Monoidratada", TipoProduto.GENERICO, false, new BigDecimal("8.50")));
        catalogoProdutos.add(new Produto(4L, "Novalgina 1g Comprimidos", "Dipirona Sódica", TipoProduto.REFERENCIA, false, new BigDecimal("24.00")));
        catalogoProdutos.add(new Produto(5L, "Dorflex 36 Comprimidos", "Dipirona + Cafeína + Orfenadrina", TipoProduto.SIMILAR, false, new BigDecimal("16.90")));
        catalogoProdutos.add(new Produto(6L, "Protetor Solar Facial FPS 60", "Filtro UV", TipoProduto.PERFUMARIA, false, new BigDecimal("59.90")));
        catalogoProdutos.add(new Produto(7L, "Shampoo Dermatológico 200ml", "Zinco Piritiona", TipoProduto.PERFUMARIA, false, new BigDecimal("34.00")));

        // Formas de Pagamento
        formasPagamentoDisponiveis.add(new FormaPagamento(1L, "Dinheiro em Espécie", MeioPagamento.DINHEIRO, ModalidadeIntegracao.NAO_APLICAVEL, true, false));
        formasPagamentoDisponiveis.add(new FormaPagamento(2L, "PIX Instantâneo", MeioPagamento.PIX, ModalidadeIntegracao.NAO_APLICAVEL, true, false));
        formasPagamentoDisponiveis.add(new FormaPagamento(3L, "Cartão Crédito (TEF)", MeioPagamento.CARTAO_CREDITO, ModalidadeIntegracao.TEF, true, true));
        formasPagamentoDisponiveis.add(new FormaPagamento(4L, "Cartão Débito (TEF)", MeioPagamento.CARTAO_DEBITO, ModalidadeIntegracao.TEF, true, false));
        formasPagamentoDisponiveis.add(new FormaPagamento(5L, "Cartão Crédito (POS Maquininha)", MeioPagamento.CARTAO_CREDITO, ModalidadeIntegracao.POS, true, true));
        formasPagamentoDisponiveis.add(new FormaPagamento(6L, "Cartão Débito (POS Maquininha)", MeioPagamento.CARTAO_DEBITO, ModalidadeIntegracao.POS, true, false));
    }

    private static void inicializarServico() {
        // Implementação em memória do repositório
        VendaRepository vendaRepository = new VendaRepository() {
            private long sequence = 1000L;

            @Override
            public Venda salvar(Venda venda) {
                if (venda.getId() == null) {
                    venda.setId(sequence++);
                }
                historicoVendas.removeIf(v -> v.getId().equals(venda.getId()));
                historicoVendas.add(venda);
                return venda;
            }

            @Override
            public Optional<Venda> buscarPorId(Long id) {
                return historicoVendas.stream().filter(v -> v.getId().equals(id)).findFirst();
            }

            @Override
            public List<Venda> listarTodas() {
                return new ArrayList<>(historicoVendas);
            }

            @Override
            public void deletar(Long id) {
                historicoVendas.removeIf(v -> v.getId().equals(id));
            }
        };

        // Gateway de pagamento TEF/POS simulado
        GatewayPagamentoClient gatewayClient = new GatewayPagamentoClient() {
            @Override
            public boolean autorizarPagamento(FormaPagamento formaPagamento, BigDecimal valor) {
                System.out.println("\n[GATEWAY ELETRÔNICO] Conectando à adquirente via " + formaPagamento.getTipoPagamento() + "...");
                System.out.println("[GATEWAY ELETRÔNICO] Solicitando autorização de R$ " + valor + " no " + formaPagamento.getDescricao() + "...");
                boolean autorizado = valor.compareTo(BigDecimal.ZERO) > 0;
                if (autorizado) {
                    System.out.println("[GATEWAY ELETRÔNICO] >>> TRANSAÇÃO APROVADA PELA REDE! (NSU: " + System.currentTimeMillis() + ")");
                } else {
                    System.out.println("[GATEWAY ELETRÔNICO] >>> TRANSAÇÃO NEGADA PELA OPERADORA.");
                }
                return autorizado;
            }

            @Override
            public boolean estornarPagamento(String idTransacao, BigDecimal valor) {
                System.out.println("[GATEWAY ELETRÔNICO] Transação " + idTransacao + " estornada no valor de R$ " + valor);
                return true;
            }
        };

        // SNGPC ANVISA simulado
        SngpcClient sngpcClient = new SngpcClient() {
            @Override
            public boolean consultarReceita(String numeroReceita, String cpfCliente) {
                System.out.println("\n[SNGPC / ANVISA] Consultando webservice federal...");
                System.out.println("[SNGPC / ANVISA] Verificando receita nº '" + numeroReceita + "' para o paciente CPF: " + (cpfCliente != null ? cpfCliente : "Consumidor"));
                boolean valida = numeroReceita != null && !numeroReceita.trim().isEmpty() && !numeroReceita.equalsIgnoreCase("INVALIDA");
                if (valida) {
                    System.out.println("[SNGPC / ANVISA] >>> RECEITA VÁLIDA E ATIVA NO BANCO NACIONAL.");
                } else {
                    System.out.println("[SNGPC / ANVISA] >>> ERRO: RECEITA RECUSADA OU INEXISTENTE NO SNGPC.");
                }
                return valida;
            }

            @Override
            public String emitirNotificacaoSNGPC(String numeroReceita, String crfFarmaceutico, List<ItemVenda> itensControlados) {
                String protocolo = "SNGPC-2026-" + (System.currentTimeMillis() % 1000000);
                System.out.println("[SNGPC / ANVISA] Transmissão assinada pelo Farmacêutico (" + crfFarmaceutico + ").");
                System.out.println("[SNGPC / ANVISA] >>> NOTIFICAÇÃO DE DISPENSAÇÃO GERADA! PROTOCOLO ANVISA: " + protocolo);
                return protocolo;
            }
        };

        vendaService = new VendaService(vendaRepository, gatewayClient, sngpcClient);
    }

    // =========================================================================
    // MENU PRINCIPAL
    // =========================================================================

    private static void exibirMenuPrincipal() {
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("Atendente Atual: " + atendenteLogado.getNome() + " (" + atendenteLogado.getFuncao().getDescricao() + ")");
        System.out.println("------------------------------------------------------------------");
        System.out.println("1. Iniciar Nova Venda (PDV Aberto para Selecionar Itens)");
        System.out.println("2. Consultar Catálogo Completo de Produtos");
        System.out.println("3. Consultar Histórico de Vendas Concluídas");
        System.out.println("4. Listar Colaboradores e Clientes Cadastrados");
        System.out.println("5. Trocar Funcionário Atendente do Caixa");
        System.out.println("0. Sair do Sistema");
        System.out.println("------------------------------------------------------------------");
    }

    // =========================================================================
    // FLUXO DE VENDA DIRETO COM SELEÇÃO DE ITENS
    // =========================================================================

    private static void iniciarFluxoVenda() {
        // Inicia a venda com o atendente atual do caixa e consumidor padrão
        FormaPagamento formaPadrao = formasPagamentoDisponiveis.get(0); // Dinheiro
        Venda venda = vendaService.iniciarVenda(null, atendenteLogado, formaPadrao);

        System.out.println("\n==================================================================");
        System.out.println("             NOVA VENDA INICIADA - CAIXA ABERTO                   ");
        System.out.println("==================================================================");

        boolean vendaAtiva = true;
        while (vendaAtiva) {
            exibirPainelVendaComProdutos(venda);

            System.out.println("\nAÇÕES DISPONÍVEIS:");
            System.out.println(" -> Digite o NÚMERO (1 a " + catalogoProdutos.size() + ") ou o NOME do produto para adicioná-lo");
            System.out.println(" -> [F] Finalizar Venda (escolher pagamento e fechar)");
            System.out.println(" -> [D] Aplicar Desconto");
            System.out.println(" -> [R] Remover Item do Carrinho");
            System.out.println(" -> [C] Identificar Cliente (CPF / Convênio)");
            System.out.println(" -> [X] Cancelar Venda");
            System.out.println("------------------------------------------------------------------");

            String comando = lerLinha("O que deseja fazer? ");

            try {
                if (comando.equalsIgnoreCase("F")) {
                    boolean finalizada = processarFinalizacao(venda);
                    if (finalizada) {
                        vendaAtiva = false;
                    }
                } else if (comando.equalsIgnoreCase("D")) {
                    aplicarDesconto(venda);
                } else if (comando.equalsIgnoreCase("R")) {
                    removerItem(venda);
                } else if (comando.equalsIgnoreCase("C")) {
                    identificarCliente(venda);
                } else if (comando.equalsIgnoreCase("X")) {
                    System.out.print("Confirmar cancelamento da venda? (S/N): ");
                    if (lerLinha("").equalsIgnoreCase("s")) {
                        venda.cancelarVenda();
                        System.out.println(">> Venda cancelada com sucesso!");
                        vendaAtiva = false;
                    }
                } else {
                    // Tenta identificar se o usuário digitou o número ou nome de um produto
                    Produto produtoEncontrado = localizarProdutoPorEntrada(comando);
                    if (produtoEncontrado != null) {
                        adicionarProdutoDireto(venda, produtoEncontrado);
                    } else {
                        System.out.println("\n[AVISO] Comando ou produto '" + comando + "' não reconhecido! Digite o número de 1 a " + catalogoProdutos.size() + " ou use F, D, R, C, X.");
                    }
                }
            } catch (DescontoInvalidoException ex) {
                System.out.println("\n[ERRO DE NEGÓCIO - DESCONTO]: " + ex.getMessage());
            } catch (MedicamentoControladoException ex) {
                System.out.println("\n[ERRO DE NEGÓCIO - SNGPC / ANVISA]: " + ex.getMessage());
            } catch (VendaFinalizadaException ex) {
                System.out.println("\n[ERRO DE SEGURANÇA]: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("\n[ERRO]: " + ex.getMessage());
            }
        }
    }

    private static void exibirPainelVendaComProdutos(Venda venda) {
        System.out.println("\n====================== CATÁLOGO DE PRODUTOS ======================");
        System.out.printf("%-4s %-32s %-12s %-12s %s\n", "CÓD", "NOME DO PRODUTO", "PREÇO", "TIPO", "SNGPC");
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < catalogoProdutos.size(); i++) {
            Produto p = catalogoProdutos.get(i);
            String avisoSngpc = p.isSngpc() ? "SIM (Receita Obrigatória)" : "Não";
            System.out.printf("[%d]  %-32s R$ %-9.2f %-12s %s\n",
                    (i + 1), p.getNome(), p.getPreco(), p.getTipo().name(), avisoSngpc);
        }

        System.out.println("\n----------------------- ITENS NO CARRINHO ------------------------");
        if (venda.getItens().isEmpty()) {
            System.out.println("  (Nenhum produto adicionado ainda. Digite o número do produto acima)");
        } else {
            for (int i = 0; i < venda.getItens().size(); i++) {
                ItemVenda item = venda.getItens().get(i);
                String tag = item.getProduto().isSngpc() ? " [SNGPC]" : "";
                System.out.printf("  Item %d: %-28s %s | %d un x R$ %6.2f = R$ %7.2f\n",
                        (i + 1),
                        item.getProduto().getNome(),
                        tag,
                        item.getQtdVendida(),
                        item.getPrecoUnitario(),
                        item.getSubtotal());
            }
        }

        System.out.println("------------------------------------------------------------------");
        System.out.printf("Subtotal: R$ %7.2f  |  Desconto: R$ %7.2f  |  TOTAL A PAGAR: R$ %7.2f\n",
                venda.calcularSubtotalItens(),
                venda.getDesconto(),
                venda.getValorTotal());
        System.out.println("Cliente: " + (venda.getCliente() != null ? venda.getCliente().getNome() + " (" + venda.getCliente().getPlano().getDescricao() + ")" : "Consumidor Não Identificado"));
        if (venda.possuiMedicamentoControlado()) {
            System.out.println("Controle SNGPC: Receita nº " + (venda.getNumeroReceita() != null ? venda.getNumeroReceita() : "[PENDENTE]") +
                    " | Farmacêutico: " + (venda.getFarmaceuticoResponsavel() != null ? venda.getFarmaceuticoResponsavel().getNome() : (venda.getFuncionario().isFarmaceutico() ? venda.getFuncionario().getNome() : "[EXIGE FARMACÊUTICO]")));
        }
    }

    private static void adicionarProdutoDireto(Venda venda, Produto produto) {
        System.out.println("\n>> Produto selecionado: " + produto.getNome() + " (R$ " + produto.getPreco() + ")");
        int qtd = lerInteiro("Informe a quantidade desejada: ");
        if (qtd <= 0) {
            System.out.println("Quantidade deve ser superior a zero!");
            return;
        }

        // Se for medicamento controlado (SNGPC), valida receita e farmacêutico
        if (produto.isSngpc()) {
            System.out.println("\n[ATENÇÃO SANITÁRIA] O produto '" + produto.getNome() + "' é CONTROLADO (SNGPC)!");
            if (venda.getNumeroReceita() == null || venda.getNumeroReceita().trim().isEmpty()) {
                String receita = lerLinha("Digite o número da Receita Médica (ex: REC-12345): ");
                while (receita.trim().isEmpty()) {
                    System.out.println("O número da receita médica não pode ficar em branco!");
                    receita = lerLinha("Digite o número da Receita Médica: ");
                }
                venda.setNumeroReceita(receita);
            }

            // Se o atendente atual não for Farmacêutico, exige indicar um Farmacêutico habilitado
            if (!venda.getFuncionario().isFarmaceutico() && venda.getFarmaceuticoResponsavel() == null) {
                System.out.println("O operador de caixa atual não é Farmacêutico.");
                System.out.println("É obrigatório selecionar um Farmacêutico responsável pela dispensação:");
                Funcionario farmaceutico = selecionarFarmaceutico();
                venda.setFarmaceuticoResponsavel(farmaceutico);
            }
        }

        // Adiciona o item na venda com subtotal calculado automaticamente
        vendaService.adicionarItem(venda, produto, qtd);
        System.out.println(">> SUCESSO: " + qtd + "x '" + produto.getNome() + "' adicionado ao carrinho!");
    }

    private static Produto localizarProdutoPorEntrada(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            return null;
        }
        entrada = entrada.trim();

        // 1. Tenta como índice/número da lista (1 a N)
        try {
            int num = Integer.parseInt(entrada);
            if (num >= 1 && num <= catalogoProdutos.size()) {
                return catalogoProdutos.get(num - 1);
            }
            // Tenta também por ID direto caso seja diferente
            for (Produto p : catalogoProdutos) {
                if (p.getId().equals((long) num)) {
                    return p;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        // 2. Tenta por nome parcial (case-insensitive)
        String termo = entrada.toLowerCase();
        for (Produto p : catalogoProdutos) {
            if (p.getNome().toLowerCase().contains(termo) || p.getPrincipioAtivo().toLowerCase().contains(termo)) {
                return p;
            }
        }

        return null;
    }

    private static void aplicarDesconto(Venda venda) {
        System.out.printf("\nSubtotal atual dos itens: R$ %.2f\n", venda.calcularSubtotalItens());
        BigDecimal desc = lerBigDecimal("Informe o valor do desconto em R$ (ex: 5.00): ");
        vendaService.aplicarDesconto(venda, desc);
        System.out.printf(">> Desconto de R$ %.2f aplicado! Novo Total: R$ %.2f\n", venda.getDesconto(), venda.getValorTotal());
    }

    private static void removerItem(Venda venda) {
        if (venda.getItens().isEmpty()) {
            System.out.println("\nO carrinho está vazio.");
            return;
        }

        System.out.println("\nItens no carrinho:");
        for (int i = 0; i < venda.getItens().size(); i++) {
            ItemVenda item = venda.getItens().get(i);
            System.out.printf(" [%d] %s (%d un - R$ %.2f)\n",
                    (i + 1), item.getProduto().getNome(), item.getQtdVendida(), item.getSubtotal());
        }

        int index = lerInteiro("Informe o número do item que deseja remover (0 para cancelar): ");
        if (index >= 1 && index <= venda.getItens().size()) {
            ItemVenda item = venda.getItens().get(index - 1);
            venda.removerItem(item);
            System.out.println(">> Item '" + item.getProduto().getNome() + "' removido do carrinho!");
        }
    }

    private static void identificarCliente(Venda venda) {
        System.out.println("\nSelecione o Cliente para vincular à venda:");
        System.out.println("[0] Consumidor Não Identificado (Avulso)");
        for (int i = 0; i < clientesCadastrados.size(); i++) {
            Cliente c = clientesCadastrados.get(i);
            System.out.printf("[%d] %s (CPF: %s | %s)\n", (i + 1), c.getNome(), c.getCpf(), c.getPlano().getDescricao());
        }

        int escolha = lerInteiro("Escolha o cliente: ");
        if (escolha >= 1 && escolha <= clientesCadastrados.size()) {
            Cliente cliente = clientesCadastrados.get(escolha - 1);
            venda.setCliente(cliente);
            System.out.println(">> Cliente '" + cliente.getNome() + "' vinculado à venda!");
        } else {
            venda.setCliente(null);
            System.out.println(">> Venda definida como Consumidor Avulso.");
        }
    }

    private static boolean processarFinalizacao(Venda venda) {
        if (venda.getItens().isEmpty()) {
            System.out.println("\n[BLOQUEIO] Não é possível finalizar uma venda sem itens no carrinho!");
            return false;
        }

        // Escolha da forma de pagamento no fechamento
        System.out.println("\n================ FORMA DE PAGAMENTO ================");
        for (int i = 0; i < formasPagamentoDisponiveis.size(); i++) {
            FormaPagamento fp = formasPagamentoDisponiveis.get(i);
            System.out.printf("[%d] %s\n", (i + 1), fp.getDescricao());
        }

        int escolhaPgto = lerInteiro("Selecione a forma de pagamento (1 a " + formasPagamentoDisponiveis.size() + "): ");
        FormaPagamento formaEscolhida;
        if (escolhaPgto >= 1 && escolhaPgto <= formasPagamentoDisponiveis.size()) {
            formaEscolhida = formasPagamentoDisponiveis.get(escolhaPgto - 1);
        } else {
            formaEscolhida = formasPagamentoDisponiveis.get(0);
        }
        venda.setFormaPagamento(formaEscolhida);

        System.out.println("\nProcessando autorizações e regras de negócio...");

        // Processa finalização via VendaService (SNGPC + TEF/POS + Salvar)
        Venda vendaConcluida = vendaService.processarFinalizacaoVenda(venda);

        // Imprime cupom fiscal
        System.out.println("\n==================================================================");
        System.out.println("                     CUPOM FISCAL DE VENDA                        ");
        System.out.println("==================================================================");
        System.out.println("Venda Nº: " + vendaConcluida.getId());
        System.out.println("Data/Hora: " + vendaConcluida.getDataHora().format(DATE_FORMAT));
        System.out.println("Status: " + vendaConcluida.getStatus());
        System.out.println("Operador: " + vendaConcluida.getFuncionario().getNome() + " (" + vendaConcluida.getFuncionario().getFuncao().getDescricao() + ")");
        if (vendaConcluida.getCliente() != null) {
            System.out.println("Cliente: " + vendaConcluida.getCliente().getNome() + " (CPF: " + vendaConcluida.getCliente().getCpf() + ")");
            System.out.println("Plano / Convênio: " + vendaConcluida.getCliente().getPlano().getDescricao());
        } else {
            System.out.println("Cliente: Consumidor Não Identificado");
        }
        if (vendaConcluida.possuiMedicamentoControlado()) {
            System.out.println("Receita Médica SNGPC: " + vendaConcluida.getNumeroReceita());
            Funcionario fResp = vendaConcluida.getFarmaceuticoResponsavel() != null
                    ? vendaConcluida.getFarmaceuticoResponsavel()
                    : vendaConcluida.getFuncionario();
            System.out.println("Farmacêutico Responsável: " + fResp.getNome() + " (CRF: " + fResp.getCrf() + ")");
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("ITENS DISPENSADOS:");
        for (ItemVenda item : vendaConcluida.getItens()) {
            System.out.printf(" - %-30s %2d un x %7.2f = R$ %7.2f\n",
                    item.getProduto().getNome(),
                    item.getQtdVendida(),
                    item.getPrecoUnitario(),
                    item.getSubtotal());
        }
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Subtotal dos Itens:   R$ %7.2f\n", vendaConcluida.calcularSubtotalItens());
        System.out.printf("Desconto Aplicado:    R$ %7.2f\n", vendaConcluida.getDesconto());
        System.out.printf("TOTAL PAGO:           R$ %7.2f\n", vendaConcluida.getValorTotal());
        System.out.println("Forma de Pagamento:   " + vendaConcluida.getFormaPagamento().getDescricao());
        System.out.println("==================================================================");
        System.out.println("            VENDA CONCLUÍDA E GRAVADA NO SISTEMA!                 ");
        System.out.println("==================================================================");

        return true;
    }

    // =========================================================================
    // CONSULTAS ADICIONAIS
    // =========================================================================

    private static void listarCatalogoProdutos() {
        System.out.println("\n================= CATÁLOGO GERAL DE PRODUTOS =================");
        System.out.printf("%-4s %-32s %-12s %-12s %s\n", "ID", "NOME", "PREÇO", "TIPO", "SNGPC");
        System.out.println("----------------------------------------------------------------------");
        for (Produto p : catalogoProdutos) {
            System.out.printf("%-4d %-32s R$ %-9.2f %-12s %s\n",
                    p.getId(),
                    p.getNome(),
                    p.getPreco(),
                    p.getTipo().name(),
                    p.isSngpc() ? "SIM (Controlado)" : "NÃO");
        }
    }

    private static void listarHistoricoVendas() {
        System.out.println("\n============= HISTÓRICO DE VENDAS REALIZADAS =============");
        if (historicoVendas.isEmpty()) {
            System.out.println("Nenhuma venda realizada nesta sessão.");
            return;
        }

        for (Venda v : historicoVendas) {
            System.out.printf("Venda #%d | Data: %s | Status: %s | Total: R$ %.2f | Itens: %d | Atendente: %s\n",
                    v.getId(),
                    v.getDataHora().format(DATE_FORMAT),
                    v.getStatus(),
                    v.getValorTotal(),
                    v.getItens().size(),
                    v.getFuncionario().getNome());
        }
    }

    private static void listarFuncionariosEClientes() {
        System.out.println("\n================ FUNCIONÁRIOS CADASTRADOS ================");
        for (Funcionario f : funcionariosCadastrados) {
            String crf = f.getCrf() != null ? " [CRF: " + f.getCrf() + "]" : "";
            System.out.printf("%d. %s (%s) - CPF: %s%s\n",
                    f.getId(), f.getNome(), f.getFuncao().getDescricao(), f.getCpf(), crf);
        }

        System.out.println("\n================== CLIENTES CADASTRADOS ==================");
        for (Cliente c : clientesCadastrados) {
            System.out.printf("%d. %s - CPF: %s | Plano: %s\n",
                    c.getId(), c.getNome(), c.getCpf(), c.getPlano().getDescricao());
        }
    }

    private static void trocarAtendente() {
        System.out.println("\nSelecione o novo atendente:");
        for (int i = 0; i < funcionariosCadastrados.size(); i++) {
            Funcionario f = funcionariosCadastrados.get(i);
            System.out.printf("[%d] %s (%s)\n", (i + 1), f.getNome(), f.getFuncao().getDescricao());
        }

        int escolha = lerInteiro("Escolha o funcionário: ");
        if (escolha >= 1 && escolha <= funcionariosCadastrados.size()) {
            atendenteLogado = funcionariosCadastrados.get(escolha - 1);
            System.out.println(">> Atendente alterado para: " + atendenteLogado.getNome());
        }
    }

    private static Funcionario selecionarFarmaceutico() {
        List<Funcionario> farmaceuticos = funcionariosCadastrados.stream()
                .filter(Funcionario::isFarmaceutico)
                .toList();

        System.out.println("Farmacêuticos disponíveis para validação da receita:");
        for (int i = 0; i < farmaceuticos.size(); i++) {
            Funcionario f = farmaceuticos.get(i);
            System.out.printf("[%d] %s (CRF: %s)\n", (i + 1), f.getNome(), f.getCrf());
        }

        int escolha = lerInteiro("Escolha o farmacêutico (1 a " + farmaceuticos.size() + "): ");
        if (escolha >= 1 && escolha <= farmaceuticos.size()) {
            return farmaceuticos.get(escolha - 1);
        }
        return farmaceuticos.get(0);
    }

    // =========================================================================
    // ENTRADA SEGURA DE DADOS
    // =========================================================================

    private static String lerLinha(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int lerInteiro(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = scanner.nextLine().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private static BigDecimal lerBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = scanner.nextLine().trim().replace(",", ".");
            try {
                return new BigDecimal(linha);
            } catch (Exception e) {
                System.out.println("Valor monetário inválido. Digite no formato: 10.50");
            }
        }
    }
}
