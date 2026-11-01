package dsa;

import model.Candidate;

/**
 * Custom Hash Table implementation using Separate Chaining for collision resolution.
 * Maps student roll numbers (Keys) to Candidate objects (Values).
 *
 * Academic Assessment (ADSA):
 * - Built manually without using java.util.HashMap.
 * - Polynomial rolling hash function for uniform key distribution.
 * - Separate chaining linked list collision resolution.
 * - Dynamic rehashing when load factor exceeds 0.75.
 * - Search, Insert, Delete operations run in O(1) average time.
 */
public class HashTable {

    // Node class representing an entry in the separate chaining linked list
    public static class HashNode {
        public String key;          // Candidate roll number
        public Candidate value;     // Candidate details
        public HashNode next;       // Pointer to next node in the collision chain

        public HashNode(String key, Candidate value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private HashNode[] buckets;
    private int capacity;
    private int size;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * Constructs a HashTable with default capacity (16).
     */
    public HashTable() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a HashTable with specified initial capacity.
     *
     * @param initialCapacity bucket count
     */
    public HashTable(int initialCapacity) {
        this.capacity = Math.max(initialCapacity, 4);
        this.buckets = new HashNode[this.capacity];
        this.size = 0;
    }

    /**
     * Polynomial rolling hash function.
     * Computes index = (|sum(c * 31^i)|) % capacity
     *
     * @param key String key (Roll Number)
     * @return bucket array index in range [0, capacity - 1]
     */
    private int hashFunction(String key) {
        if (key == null || key.isEmpty()) {
            return 0;
        }
        int hashVal = 0;
        int prime = 31;
        for (int i = 0; i < key.length(); i++) {
            hashVal = hashVal * prime + key.charAt(i);
        }
        return Math.abs(hashVal % capacity);
    }

    /**
     * Inserts or updates a candidate in the hash table.
     * If the key exists, its candidate value is updated.
     * If collision occurs, the entry is prepended to the linked list bucket.
     * Time Complexity: O(1) average, O(N) worst case
     *
     * @param key candidate roll number
     * @param value Candidate object
     */
    public void insert(String key, Candidate value) {
        if (key == null || value == null) {
            return;
        }

        int index = hashFunction(key);
        HashNode head = buckets[index];

        // Check if key already exists in the chain
        HashNode curr = head;
        while (curr != null) {
            if (curr.key.equalsIgnoreCase(key)) {
                curr.value = value; // Update existing value
                return;
            }
            curr = curr.next;
        }

        // Insert new node at the head of the chain (O(1) insertion)
        HashNode newNode = new HashNode(key, value);
        newNode.next = head;
        buckets[index] = newNode;
        size++;

        // Check if dynamic resizing (rehashing) is required
        if ((double) size / capacity > LOAD_FACTOR_THRESHOLD) {
            rehash();
        }
    }

    /**
     * Searches for a candidate by their roll number.
     * Time Complexity: O(1) average, O(N) worst case
     *
     * @param key candidate roll number
     * @return Candidate object if found, null otherwise
     */
    public Candidate search(String key) {
        if (key == null || isEmpty()) {
            return null;
        }

        int index = hashFunction(key);
        HashNode curr = buckets[index];

        while (curr != null) {
            if (curr.key.equalsIgnoreCase(key)) {
                return curr.value;
            }
            curr = curr.next;
        }
        return null;
    }

    /**
     * Deletes a candidate entry by roll number.
     * Time Complexity: O(1) average, O(N) worst case
     *
     * @param key candidate roll number
     * @return true if found and removed, false otherwise
     */
    public boolean delete(String key) {
        if (key == null || isEmpty()) {
            return false;
        }

        int index = hashFunction(key);
        HashNode curr = buckets[index];
        HashNode prev = null;

        while (curr != null) {
            if (curr.key.equalsIgnoreCase(key)) {
                if (prev == null) {
                    buckets[index] = curr.next; // Head of chain removed
                } else {
                    prev.next = curr.next;      // Middle or tail node removed
                }
                size--;
                return true;
            }
            prev = curr;
            curr = curr.next;
        }
        return false;
    }

    /**
     * Rehashes all elements when the load factor exceeds 0.75.
     * Doubles capacity to maintain O(1) search performance.
     */
    private void rehash() {
        int oldCapacity = capacity;
        HashNode[] oldBuckets = buckets;

        capacity = oldCapacity * 2;
        buckets = new HashNode[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            HashNode curr = oldBuckets[i];
            while (curr != null) {
                insert(curr.key, curr.value);
                curr = curr.next;
            }
        }
    }

    /**
     * Displays the complete internal bucket structure of the Hash Table.
     * Shows separate chaining collision lists, collision count, and load factor.
     */
    public void display() {
        System.out.println("\n----------------- HASH TABLE INTERNAL STRUCTURE -----------------");
        System.out.printf("Capacity: %d | Total Entries: %d | Current Load Factor: %.2f\n",
                capacity, size, (double) size / capacity);
        System.out.println("-----------------------------------------------------------------");

        int collisionCount = 0;
        int usedBuckets = 0;

        for (int i = 0; i < capacity; i++) {
            HashNode curr = buckets[i];
            System.out.printf("Bucket [%02d]: ", i);

            if (curr == null) {
                System.out.println("Empty");
            } else {
                usedBuckets++;
                int chainLength = 0;
                StringBuilder sb = new StringBuilder();
                while (curr != null) {
                    chainLength++;
                    sb.append(String.format("[%s : %s]", curr.key, curr.value.getName()));
                    if (curr.next != null) {
                        sb.append(" -> ");
                    }
                    curr = curr.next;
                }
                sb.append(" -> null");
                System.out.println(sb.toString());

                if (chainLength > 1) {
                    collisionCount += (chainLength - 1);
                }
            }
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("Occupied Buckets: %d / %d | Total Collisions Resolved: %d\n",
                usedBuckets, capacity, collisionCount);
        System.out.println("-----------------------------------------------------------------\n");
    }

    /**
     * Structure representing bucket chain information for ADSA inspection.
     */
    public static class BucketInfo {
        public int index;
        public int chainLength;
        public java.util.List<String> entries = new java.util.ArrayList<>();
    }

    /**
     * Returns a debug snapshot of bucket chains, capacity, and collisions for JSON serialization.
     */
    public java.util.Map<String, Object> getDebugSnapshot() {
        java.util.Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("capacity", capacity);
        snapshot.put("size", size);
        snapshot.put("loadFactor", Math.round(((double) size / capacity) * 100.0) / 100.0);

        int collisionCount = 0;
        int occupiedBuckets = 0;
        java.util.List<BucketInfo> bucketList = new java.util.ArrayList<>();

        for (int i = 0; i < capacity; i++) {
            HashNode curr = buckets[i];
            BucketInfo bInfo = new BucketInfo();
            bInfo.index = i;

            if (curr != null) {
                occupiedBuckets++;
                int chainLen = 0;
                while (curr != null) {
                    chainLen++;
                    bInfo.entries.add(curr.key + " : " + curr.value.getName() + " (" + curr.value.getDepartment() + ")");
                    curr = curr.next;
                }
                bInfo.chainLength = chainLen;
                if (chainLen > 1) {
                    collisionCount += (chainLen - 1);
                }
            } else {
                bInfo.chainLength = 0;
            }
            bucketList.add(bInfo);
        }

        snapshot.put("occupiedBuckets", occupiedBuckets);
        snapshot.put("collisionCount", collisionCount);
        snapshot.put("buckets", bucketList);
        return snapshot;
    }

    /**
     * Returns total number of candidate entries stored.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if hash table contains no entries.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears all entries from the hash table.
     */
    public void clear() {
        buckets = new HashNode[capacity];
        size = 0;
    }
}

