/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.frontend.delta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abs.frontend.analyser.SemanticErrorList;
import abs.frontend.ast.DeltaDecl;
import abs.frontend.ast.Model;

public class DeltaTrie {
    private final Node root;
    private final Model model;

    /**
     * Constructor
     */
    public DeltaTrie(Model model, SemanticErrorList errors) {
        this.model = model;
        root = new Node(errors);
    }

    // Adds a word to the Trie
    public void addWord(List<String> word) {
        System.out.print("DeltaSequence");
        if (word.size() == 0) // no deltas
            root.isValidProduct = true;
        else
            root.addWord(word, 0);
        System.out.println();
    }

    /**********************************************************************************************/
    /*
     * Trie Node
     */
    class Node {
        private final Map<String, Node> children;
        private String deltaID = null;
        private boolean isValidProduct = false;
        private final ProgramTypeAbstraction ta;

         // Constructor for top level root node
        public Node(SemanticErrorList errors) {
            this.deltaID = "core";
            this.children = new HashMap<String, Node>();
            this.ta = new ProgramTypeAbstraction(errors);
            model.buildCoreTypeAbstraction(ta);
        }

        // Constructor for child node
        public Node(String name, ProgramTypeAbstraction ta) {
           this.children = new HashMap<String, Node>();
           this.deltaID = name;
           this.ta = ta;
        }

        /** Add a word to the trie
         *
         * @param word: Non-empty List of delta names
         * @param d:    Index of List element to start with (for recursive invocation)
         */
        protected void addWord(List<String> word, int d) {
            Node nextNode;

            System.out.print(">>>" + word.get(d));

            if (children.containsKey(word.get(d))) {
                nextNode = children.get(word.get(d));
            } else {
                ProgramTypeAbstraction nextTA = new ProgramTypeAbstraction(ta);
                nextNode = new Node(word.get(d), nextTA);

                // Apply delta to nextNode's program type abstraction
                DeltaDecl delta = model.getDeltaDeclsMap().get(nextNode.deltaID);
                nextTA.applyDelta(delta);

                children.put(word.get(d), nextNode);
                System.out.print("*");
            }

            if (word.size() > d+1)
                nextNode.addWord(word, d+1);
            else {
                nextNode.isValidProduct = true;
                // TODO type check this product
                System.out.print(".");
            }
        }


        // Getters
        public Map<String, Node> getChildren() {
            return children;
        }

        public String getDeltaID() {
            return deltaID;
        }

        public boolean isValidProduct() {
            return isValidProduct;
        }

        public ProgramTypeAbstraction getProgramTypeAbstraction() {
            return ta;
        }

        @Override
        public String toString() {
            return toString(new StringBuilder());
        }

        public String toString(StringBuilder s) {
            s.append("Delta: " + deltaID + ".  " + "Valid Product: " + isValidProduct + "\n");
            s.append(ta.toString());
            for (Node child : children.values())
                child.toString(s);
            return s.toString();
        }

    }
    /**********************************************************************************************/

    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }

}
