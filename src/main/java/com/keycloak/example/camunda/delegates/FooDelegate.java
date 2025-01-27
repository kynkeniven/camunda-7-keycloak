package com.keycloak.example.camunda.delegates;

import com.keycloak.example.camunda.scopehelper.ScopeHelper;
import com.keycloak.example.config.camunda.LogDelegate;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PRIVATE;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class FooDelegate implements JavaDelegate {

    @Override
    @LogDelegate
    public void execute(DelegateExecution execution) {
        final var scopeHelper = new ScopeHelper(execution);

    }
}
