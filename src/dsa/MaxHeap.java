package dsa;

import model.ScoreRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Max Heap implementation for ranking candidates by examination marks.
 *
 * Academic Assessment (ADSA):
 * - Built manually without using java.util.PriorityQueue.
 * - Supports O(1) getMax(), O(log N) insert(), O(log N) extractMax().
 * - Bottom-up buildHeap() in O(N) using Floyd's algorithm.
 * - Efficient top-K retrieval in O(N + K log N).
 */
public class MaxHeap {

    // Internal dynamic array storing heap elements
    private List<ScoreRecord> heap;

    /**
     * Constructs an empty MaxHeap.
     */
    public MaxHeap() {
        this.heap = new ArrayList<>();
    }

    /**
     * Constructs a MaxHeap and initializes it with the given records using Floyd's buildHeap.
     *
     * @param items List of ScoreRecords
     */
    public MaxHeap(List<ScoreRecord> items) {
        this.heap = new ArrayList<>();
        buildHeap(items);
    }

    /**
     * Returns the parent index for node at index i.
     */
    private int parent(int i) {
        return (i - 1) / 2;
    }

    /**
     * Returns the left child index for node at index i.
     */
    private int leftChild(int i) {
        return 2 * i + 1;
    }

    /**
     * Returns the right child index for node at index i.
     */
    private int rightChild(int i) {
        return 2 * i + 2;
    }

    /**
     * Swaps two elements in the heap array.
     */
    private void swap(int i, int j) {
        ScoreRecord temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    /**
     * Compares two ScoreRecords.
     * Higher marks return a positive value (Max Heap priority).
     * If marks are identical, tie-breaker uses roll number.
     *
     * @param a first record
     * @param b second record
     * @return positive if a > b, negative if a < b, 0 if equal
     */
    private int compare(ScoreRecord a, ScoreRecord b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        if (Double.compare(a.getMarks(), b.getMarks()) != 0) {
            return Double.compare(a.getMarks(), b.getMarks());
        }
        // Tie-breaker: reverse alphabetical roll number for deterministic ordering
        return b.getRollNumber().compareTo(a.getRollNumber());
    }

    /**
     * Restores the max heap property by moving the element at index up.
     * Time Complexity: O(log N)
     *
     * @param index element index
     */
    public void heapifyUp(int index) {
        while (index > 0 && compare(heap.get(index), heap.get(parent(index))) > 0) {
            swap(index, parent(index));
            index = parent(index);
        }
    }

    /**
     * Restores the max heap property by moving the element at index down.
     * Time Complexity: O(log N)
     *
     * @param index element index
     */
    public void heapifyDown(int index) {
        int maxIndex = index;
        int left = leftChild(index);
        int right = rightChild(index);
        int n = heap.size();

        // Check if left child is larger than current max
        if (left < n && compare(heap.get(left), heap.get(maxIndex)) > 0) {
            maxIndex = left;
        }

        // Check if right child is larger than current max
        if (right < n && compare(heap.get(right), heap.get(maxIndex)) > 0) {
            maxIndex = right;
        }

        // If current element violates heap property, swap and recurse down
        if (index != maxIndex) {
            swap(index, maxIndex);
            heapifyDown(maxIndex);
        }
    }

    /**
     * Inserts a new ScoreRecord into the Max Heap.
     * Time Complexity: O(log N)
     *
     * @param item ScoreRecord to insert
     */
    public void insert(ScoreRecord item) {
        if (item == null) return;
        heap.add(item);
        heapifyUp(heap.size() - 1);
    }

    /**
     * Returns the maximum element (root) without removing it.
     * Time Complexity: O(1)
     *
     * @return highest-scoring ScoreRecord or null if empty
     */
    public ScoreRecord getMax() {
        if (heap.isEmpty()) {
            return null;
        }
        return heap.get(0);
    }

    /**
     * Removes and returns the maximum element (root) of the Max Heap.
     * Replaces root with last element and calls heapifyDown(0).
     * Time Complexity: O(log N)
     *
     * @return highest-scoring ScoreRecord or null if empty
     */
    public ScoreRecord extractMax() {
        if (heap.isEmpty()) {
            return null;
        }

        ScoreRecord maxItem = heap.get(0);
        ScoreRecord lastItem = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, lastItem);
            heapifyDown(0);
        }

        return maxItem;
    }

    /**
     * Constructs a Max Heap from an arbitrary list of items using Floyd's algorithm.
     * Runs bottom-up from the lowest non-leaf node down to the root.
     * Time Complexity: O(N)
     *
     * @param items List of records
     */
    public void buildHeap(List<ScoreRecord> items) {
        heap.clear();
        if (items == null || items.isEmpty()) {
            return;
        }
        heap.addAll(items);

        // Start from last non-leaf node: (size / 2) - 1 down to 0
        int startIdx = (heap.size() / 2) - 1;
        for (int i = startIdx; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    /**
     * Retrieves the top K highest-scoring candidates without destroying the original heap.
     * Clones the heap elements and repeatedly extracts the maximum.
     * Time Complexity: O(N + K log N)
     *
     * @param k number of top elements to retrieve
     * @return List of top K ScoreRecords sorted in descending order
     */
    public List<ScoreRecord> getTopK(int k) {
        List<ScoreRecord> topKList = new ArrayList<>();
        if (k <= 0 || heap.isEmpty()) {
            return topKList;
        }

        // Create a copy heap to preserve original heap structure
        MaxHeap copyHeap = new MaxHeap();
        copyHeap.heap.addAll(this.heap);

        int count = Math.min(k, copyHeap.size());
        for (int i = 0; i < count; i++) {
            topKList.add(copyHeap.extractMax());
        }

        return topKList;
    }

    /**
     * Returns the number of elements in the heap.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Checks if the heap is empty.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Clears all elements from the heap.
     */
    public void clear() {
        heap.clear();
    }

    /**
     * Returns a copy of the internal heap list for visualization / inspection.
     */
    public List<ScoreRecord> getHeapList() {
        return new ArrayList<>(heap);
    }
}

