package ru.urfu.algorithms;

import java.util.Arrays;
import java.util.Random;

public final class SortsTest {
    private static int checks;

    public static void main(String[] args) {
        int[][] cases = {
            {}, {1}, {2, 1}, {1, 2}, {4, 4, 4}, {0, -1, 1, -1},
            {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE},
            {9, 1, 8, 2, 7, 3, 6, 4, 5}, {3, 2, 1, 2, 3, 2, 1}
        };
        for (int[] values : cases) {
            checkBoth(values);
        }
        for (Inputs.Scenario scenario : Inputs.Scenario.values()) {
            for (int size : new int[]{0, 1, 2, 3, 16, 127, 1024}) {
                checkBoth(Inputs.create(scenario, size, 20261027L + size));
            }
        }
        Random random = new Random(918979226L);
        for (int test = 0; test < 300; test++) {
            int[] values = new int[random.nextInt(1025)];
            for (int i = 0; i < values.length; i++) {
                values[i] = test % 2 == 0 ? random.nextInt(11) - 5 : random.nextInt();
            }
            checkBoth(values);
        }
        for (int size = 0; size <= 8; size++) {
            int[] permutation = new int[size];
            for (int i = 0; i < size; i++) {
                permutation[i] = i;
            }
            permutations(permutation, 0);
        }
        // Large inputs exercise the QuickSort stack without quadratic BubbleSort work.
        for (Inputs.Scenario scenario : Inputs.Scenario.values()) {
            checkQuick(Inputs.create(scenario, 200_000, 42));
        }
        expectNullFailure(true);
        expectNullFailure(false);
        System.out.println("PASS: " + checks + " checks against Arrays.sort (including element preservation).");
    }

    private static void permutations(int[] values, int position) {
        if (position == values.length) {
            checkBoth(values);
            return;
        }
        for (int i = position; i < values.length; i++) {
            swap(values, position, i);
            permutations(values, position + 1);
            swap(values, position, i);
        }
    }

    private static void swap(int[] values, int first, int second) {
        int temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
    }

    private static void checkBoth(int[] original) {
        int[] expected = original.clone();
        Arrays.sort(expected);
        int[] actual = original.clone();
        Sorts.bubbleSort(actual);
        assertSame(expected, actual, "BubbleSort");
        checkQuick(original);
    }

    private static void checkQuick(int[] original) {
        int[] expected = original.clone();
        Arrays.sort(expected);
        int[] actual = original.clone();
        Sorts.quickSort(actual);
        assertSame(expected, actual, "QuickSort");
    }

    private static void assertSame(int[] expected, int[] actual, String algorithm) {
        checks++;
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(algorithm + ": expected " + Arrays.toString(expected)
                + ", actual " + Arrays.toString(actual));
        }
    }

    private static void expectNullFailure(boolean bubble) {
        try {
            if (bubble) {
                Sorts.bubbleSort(null);
            } else {
                Sorts.quickSort(null);
            }
            throw new AssertionError("null must be rejected");
        } catch (NullPointerException expected) {
            checks++;
        }
    }
}
