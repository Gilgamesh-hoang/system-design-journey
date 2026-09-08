package com.journey.hashing;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashRing {
    private final HashFunction hashFunction;
    private final int numberOfReplicas; // Virtual nodes per physical node
    private final SortedMap<Long, Node> circle = new TreeMap<>();

    public ConsistentHashRing(HashFunction hashFunction, int numberOfReplicas, Collection<Node> nodes) {
        this.hashFunction = hashFunction;
        this.numberOfReplicas = numberOfReplicas;

        if (nodes != null) {
            for (Node node : nodes) {
                addNode(node);
            }
        }
    }

    public void addNode(Node node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            // Append an index to create virtual nodes (e.g. node1#0, node1#1)
            long hash = hashFunction.hash(node.getId() + "#" + i);
            circle.put(hash, node);
        }
    }

    public void removeNode(Node node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            long hash = hashFunction.hash(node.getId() + "#" + i);
            circle.remove(hash);
        }
    }

    public Node getNode(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        long hash = hashFunction.hash(key);
        if (!circle.containsKey(hash)) {
            // Get the sub-map strictly greater than the hash
            SortedMap<Long, Node> tailMap = circle.tailMap(hash);
            
            // If tailMap is empty, wrap around to the first key in the circle
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }
}
