package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.service.Scope;

import javax.swing.*;
import java.util.*;

public class OperationDialog {
    private final String operationName;
    private final JPanel content;
    private final Scope defaultScope;
    private Optional<String> label;
    private Optional<Object> defaultValue;
    private String value = null;

    public OperationDialog(String operationName, JPanel content, boolean empty) {
        this.operationName = operationName;
        this.content = content;
        this.defaultScope = empty ? Scope.ARRANGEMENT : Scope.SELECTION;
    }

    public Scope show() {

        List<String> scopeList = new ArrayList<>();
        scopeList.addAll(Arrays.asList(Scope.ARRANGEMENT.name(), Scope.TRACK.name(), Scope.SELECTION.name(), defaultScope.name()));
        ParameterDialog pd;
        if (!label.isPresent()) {
            pd = new ParameterDialog(new String[]{operationName}, new String[]{"Scope"}, new Object[]{scopeList.toArray(new String[]{})}, content);
        } else {
            pd = new ParameterDialog(new String[]{operationName}, new String[]{"Scope", label.get() },
                    new Object[]{scopeList.toArray(new String[]{}), defaultValue.get()}, content);
        }
        String[] parameters = pd.getParameters();
        if (parameters!=null) {
            String scope = parameters[0];
            if (parameters.length>1) {
                value = parameters[1];
            }
            return Scope.valueOf(scope);
        }
        return null;
    }

    public void add(String label, Object defaultValue) {
        this.label = Optional.of(label);
        this.defaultValue = Optional.of(defaultValue);
    }

    public String getValue() {
        return value;
    }
}
