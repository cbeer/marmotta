package org.apache.marmotta.ldpath.model.functions;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AsUriFunction<Node> extends SelectorFunction<Node> {

    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @param args a nested list of KiWiNodes
     * @return
     */
    @Override
    @SafeVarargs
    public final Collection<Node> apply(RDFBackend<Node> rdfBackend, Node context, Collection<Node>... args) throws IllegalArgumentException {

        final Set<Node> result = new HashSet<Node>();

        for (Collection<Node> arg : args) {
            for (Node node : arg) {
                try {
                    result.add(rdfBackend.createURI(rdfBackend.stringValue(node)));
                } catch(IllegalArgumentException e) {
                    // NOOP
                }
            }
        }

        return result;
    }

    /**
     * Return the name of the NodeFunction for registration in the function registry
     *
     * @return
     */
    @Override
    public String getLocalName() {
        return "asUri";
    }

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     *
     * @return
     */
    @Override
    public String getSignature() {
        return "fn:asUri(string : String) : Node";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "A node function converting a literal to a URI";
    }
}