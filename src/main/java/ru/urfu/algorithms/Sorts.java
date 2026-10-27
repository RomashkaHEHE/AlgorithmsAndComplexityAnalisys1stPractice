package ru.urfu.algorithms;

import java.util.Objects;

public final class Sorts {
    private Sorts() { }

    public static void bubbleSort(int[] values) {
        Objects.requireNonNull(values, "values");
        for (int end = values.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int i = 0; i < end; i++) {
                if (values[i] > values[i + 1]) {
                    swap(values, i, i + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                return;
            }
        }
    }

    public static void quickSort(int[] values) {
        Objects.requireNonNull(values, "values");
        quickSort(values, 0, values.length - 1);
    }

    private static void quickSort(int[] values, int left, int right) {
        while (left < right) {
            int i = left;
            int j = right;
            int pivot = values[left + (right - left) / 2];
            while (i <= j) {
                while (values[i] < pivot) {
                    i++;
                }
                while (values[j] > pivot) {
                    j--;
                }
                if (i <= j) {
                    swap(values, i, j);
                    i++;
                    j--;
                }
            }
            // Recurse into the smaller part to bound stack depth by O(log n).
            if (j - left < right - i) {
                if (left < j) {
                    quickSort(values, left, j);
                }
                left = i;
            } else {
                if (i < right) {
                    quickSort(values, i, right);
                }
                right = j;
            }
        }
    }

    private static void swap(int[] values, int first, int second) {
        int temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
    }
}
