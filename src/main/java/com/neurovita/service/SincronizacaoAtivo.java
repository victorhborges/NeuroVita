package com.neurovita.service;

import java.util.function.Consumer;

/** Mantém o estado da entidade e da conta sincronizado com compensação. */
final class SincronizacaoAtivo {

    private SincronizacaoAtivo() {
    }

    static void salvar(
            boolean estadoEntidadeAnterior,
            boolean estadoContaAnterior,
            boolean novoEstado,
            Consumer<Boolean> definirEstadoEntidade,
            Runnable salvarEntidade,
            Consumer<Boolean> definirEstadoConta,
            Runnable salvarConta) {

        try {
            if (novoEstado) {
                // Só reabre o acesso depois que o cadastro estiver ativo.
                definirEstadoEntidade.accept(true);
                salvarEntidade.run();
                definirEstadoConta.accept(true);
                salvarConta.run();
            } else {
                // Revoga o acesso antes de persistir a inativação do cadastro.
                definirEstadoConta.accept(false);
                salvarConta.run();
                definirEstadoEntidade.accept(false);
                salvarEntidade.run();
            }
        } catch (RuntimeException falha) {
            restaurar(
                    estadoEntidadeAnterior,
                    estadoContaAnterior,
                    definirEstadoEntidade,
                    salvarEntidade,
                    definirEstadoConta,
                    salvarConta,
                    falha);
            throw falha;
        }
    }

    private static void restaurar(
            boolean estadoEntidadeAnterior,
            boolean estadoContaAnterior,
            Consumer<Boolean> definirEstadoEntidade,
            Runnable salvarEntidade,
            Consumer<Boolean> definirEstadoConta,
            Runnable salvarConta,
            RuntimeException falha) {

        definirEstadoEntidade.accept(estadoEntidadeAnterior);
        try {
            salvarEntidade.run();
        } catch (RuntimeException erroRestauracao) {
            falha.addSuppressed(erroRestauracao);
        }

        definirEstadoConta.accept(estadoContaAnterior);
        try {
            salvarConta.run();
        } catch (RuntimeException erroRestauracao) {
            falha.addSuppressed(erroRestauracao);
        }
    }
}
