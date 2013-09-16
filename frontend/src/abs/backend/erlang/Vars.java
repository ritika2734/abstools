/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.erlang;

import java.util.LinkedHashMap;

import abs.frontend.ast.ParamDecl;

import com.google.common.collect.Sets;

public class Vars extends LinkedHashMap<String, Integer> {

    private static final long serialVersionUID = 1L;

    public Vars(Vars vars) {
        super(vars);
    }

    public Vars() {
    }

    public static Vars n(abs.frontend.ast.List<ParamDecl> args) {
        Vars l = new Vars();
        for (ParamDecl n : args)
            l.put(n.getName(), 0);
        return l;
    }

    public static Vars n(String... name) {
        Vars l = new Vars();
        for (String n : name)
            l.put(n, 0);
        return l;
    }

    public String nV(String name) {
        if (containsKey(name))
            throw new RuntimeException("Var already exists");
        put(name, 0);
        return get(name);
    }

    public static final String PREFIX = "V_";

    public String inc(String name) {
        if (containsKey(name))
            put(name, super.get(name) + 1);
        else
            put(name, 0);
        return get(name);
    }

    public void incAll() {
        for (java.util.Map.Entry<String, Integer> a : this.entrySet())
            a.setValue(a.getValue() + 1);
    }

    public String get(String name) {
        int c = super.get(name);
        return PREFIX + name + "_" + c;
    }

    public Vars pass() {
        return new Vars(this);
    }

    public String toParamList() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (java.util.Map.Entry<String, Integer> a : this.entrySet()) {
            if (!first)
                sb.append(',');
            first = false;
            sb.append(PREFIX).append(a.getKey()).append("_").append(a.getValue());
        }
        return sb.append("]").toString();
    }

    public void retainAll(Vars vars) {
        for (String k : Sets.difference(this.keySet(), vars.keySet()))
            remove(k);

    }
}