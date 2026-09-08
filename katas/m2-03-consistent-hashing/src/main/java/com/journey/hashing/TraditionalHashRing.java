package com.journey.hashing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TraditionalHashRing {
    private final HashFunction hashFunction;
    private final List<Node> nodes;

    public TraditionalHashRing(HashFunction hashFunction, Collection<Node> initialNodes) {
        this.hashFunction = hashFunction;
        this.nodes = new ArrayList<>(initialNodes);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
    }

    public Node getNode(String key) {
        if (nodes.isEmpty()) return null;
        long hash = hashFunction.hash(key);
        // Deal with negative hash values safely
        int index = (int) (Math.abs(hash) % nodes.size());
        return nodes.get(index);
    }
}
