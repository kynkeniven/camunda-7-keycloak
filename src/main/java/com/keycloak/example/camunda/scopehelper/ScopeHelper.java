package com.keycloak.example.camunda.scopehelper;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.delegate.DelegateExecution;

@AllArgsConstructor
@EqualsAndHashCode
public class ScopeHelper {

    private final DelegateExecution execution;

    public static final String NUMERO_SOLICITACAO = "numeroSolicitacao";

    public String getNumeroSolicitacao() {
        return (String) execution.getVariable(NUMERO_SOLICITACAO);
    }

    public void setNumeroSolicitacao(String numeroSolicitacao) {
        execution.setVariable(NUMERO_SOLICITACAO, numeroSolicitacao);
    }

}