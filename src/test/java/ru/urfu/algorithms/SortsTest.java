package ru.urfu.algorithms;

import java.util.Arrays;
import java.util.Random;

public class SortsTest {
    public static void main(String[] args) {
        int[][] examples = {
            {}, {1}, {2, 1}, {1, 2, 3, 4}, {4, 3, 2, 1},
            {7, 7, 7}, {3, -2, 0, -2, 1},
            {Integer.MAX_VALUE, 0, Integer.MIN_VALUE}
        };
        for (int[] a : examples) {
            check(a);
        }

        Random random = new Random(42);
        for (int test = 0; test < 12; test++) {
            int[] a = new int[100];
            for (int i = 0; i < a.length; i++) {
                a[i] = random.nextInt(100);
            }
            check(a);
        }
        System.out.println("Tests passed: 20 arrays, both sorting algorithms");
    }

    private static void check(int[] original) {
        int[] expected = original.clone();
        Arrays.sort(expected);

        int[] bubble = original.clone();
        Sorts.bubbleSort(bubble);
        if (!Arrays.equals(expected, bubble)) {
            throw new AssertionError("BubbleSort: " + Arrays.toString(original));
        }

        int[] quick = original.clone();
        Sorts.quickSort(quick);
        if (!Arrays.equals(expected, quick)) {
            throw new AssertionError("QuickSort: " + Arrays.toString(original));
        }
    }
}
