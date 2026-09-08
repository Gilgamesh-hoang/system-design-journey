package com.journey.hashing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== KATA M2-03: CONSISTENT HASHING SIMULATOR ===");

        int keyCount = 1_000_000;
        int nodeCount = 10;
        
        List<String> keys = new ArrayList<>(keyCount);
        for (int i = 0; i < keyCount; i++) {
            keys.add("key-" + i);
        }

        List<Node> initialNodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            initialNodes.add(new Node("Node-" + i));
        }

        HashFunction hashFunction = new HashFunction();

        // 1. Evaluate Load Distribution (Standard Dev, Min, Max)
        System.out.println("\n--- 1. LOAD DISTRIBUTION (1M keys across " + nodeCount + " nodes) ---");
        
        // 1a. Traditional Hashing
        TraditionalHashRing traditionalRing = new TraditionalHashRing(hashFunction, initialNodes);
        Map<String, String> traditionalMapping = measureDistribution("Traditional (hash % N)", traditionalRing, keys, initialNodes);

        // 1b. Consistent Hashing (V=1)
        ConsistentHashRing consistentV1 = new ConsistentHashRing(hashFunction, 1, initialNodes);
        measureDistribution("Consistent Hashing (V=1)", consistentV1, keys, initialNodes);

        // 1c. Consistent Hashing (V=10)
        ConsistentHashRing consistentV10 = new ConsistentHashRing(hashFunction, 10, initialNodes);
        measureDistribution("Consistent Hashing (V=10)", consistentV10, keys, initialNodes);

        // 1d. Consistent Hashing (V=100)
        ConsistentHashRing consistentV100 = new ConsistentHashRing(hashFunction, 100, initialNodes);
        Map<String, String> consistentMapping = measureDistribution("Consistent Hashing (V=100)", consistentV100, keys, initialNodes);

        // 2. Evaluate Rebalancing (Adding 1 node)
        System.out.println("\n--- 2. REBALANCING (Adding 1 node: Node-10) ---");
        Node newNode = new Node("Node-10");
        
        // Add to traditional
        traditionalRing.addNode(newNode);
        measureRebalance("Traditional (hash % N)", traditionalRing, keys, traditionalMapping);

        // Add to consistent (V=100)
        consistentV100.addNode(newNode);
        measureRebalance("Consistent Hashing (V=100)", consistentV100, keys, consistentMapping);
        
        System.out.println("\n(Theory: Consistent Hashing migration should be ~ " + String.format("%.2f", 100.0 / (nodeCount + 1)) + "% )");
    }

    private static Map<String, String> measureDistribution(String name, Object ring, List<String> keys, List<Node> nodes) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> keyMapping = new HashMap<>();
        
        for (Node n : nodes) {
            counts.put(n.getId(), 0);
        }

        for (String key : keys) {
            Node assignedNode;
            if (ring instanceof TraditionalHashRing) {
                assignedNode = ((TraditionalHashRing) ring).getNode(key);
            } else {
                assignedNode = ((ConsistentHashRing) ring).getNode(key);
            }
            
            counts.put(assignedNode.getId(), counts.get(assignedNode.getId()) + 1);
            keyMapping.put(key, assignedNode.getId());
        }

        double mean = (double) keys.size() / nodes.size();
        double sumSq = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;

        for (int count : counts.values()) {
            sumSq += Math.pow(count - mean, 2);
            if (count < min) min = count;
            if (count > max) max = count;
        }

        double stdDev = Math.sqrt(sumSq / nodes.size());
        
        System.out.printf("%-26s -> Min: %6d | Max: %7d | StdDev: %7.2f (Ideal: %d)%n", 
                name, min, max, stdDev, (int)mean);
                
        return keyMapping;
    }

    private static void measureRebalance(String name, Object ring, List<String> keys, Map<String, String> oldMapping) {
        int movedKeys = 0;

        for (String key : keys) {
            Node newNode;
            if (ring instanceof TraditionalHashRing) {
                newNode = ((TraditionalHashRing) ring).getNode(key);
            } else {
                newNode = ((ConsistentHashRing) ring).getNode(key);
            }
            
            if (!newNode.getId().equals(oldMapping.get(key))) {
                movedKeys++;
            }
        }

        double movedPercent = (double) movedKeys / keys.size() * 100;
        System.out.printf("%-26s -> Moved Keys: %7d (%.2f%%)%n", name, movedKeys, movedPercent);
        
        // Assert for Consistent Hashing
        if (ring instanceof ConsistentHashRing) {
            if (movedPercent > 15.0) { // Should be ~9.09% (1/11), assert < 15%
                System.err.println("WARNING: Consistent Hashing rebalanced too many keys!");
            }
        }
    }
}
