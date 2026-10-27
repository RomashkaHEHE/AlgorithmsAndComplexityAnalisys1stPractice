package ru.urfu.algorithms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    public static void main(String[] args) throws IOException {
        int[] sizes = {1000, 2000, 4000, 8000, 16000};
        int repeats = 5;
        Random random = new Random(42);
        new File("results").mkdirs();

        try (PrintWriter table = new PrintWriter("results/results.csv");
             PrintWriter raw = new PrintWriter("results/raw.csv")) {
            table.println("type,n,bubble_ms,quick_ms");
            raw.println("type,n,repeat,bubble_ns,quick_ns");

            for (int type = 0; type < 2; type++) {
                String name = type == 0 ? "random" : "reversed";
                for (int n : sizes) {
                    int[] original = new int[n];
                    for (int i = 0; i < n; i++) {
                        original[i] = type == 0 ? random.nextInt(10000) : n - i;
                    }

                    int[] expected = original.clone();
                    Arrays.sort(expected);
                    long bubbleTotal = 0;
                    long quickTotal = 0;
                    for (int repeat = 0; repeat < repeats; repeat++) {
                        int[] bubble = original.clone();
                        int[] quick = original.clone();

                        long start = System.nanoTime();
                        Sorts.bubbleSort(bubble);
                        long bubbleTime = System.nanoTime() - start;

                        start = System.nanoTime();
                        Sorts.quickSort(quick);
                        long quickTime = System.nanoTime() - start;

                        if (!Arrays.equals(expected, bubble) || !Arrays.equals(expected, quick)) {
                            throw new AssertionError("Sorting error");
                        }
                        bubbleTotal += bubbleTime;
                        quickTotal += quickTime;
                        raw.println(name + "," + n + "," + (repeat + 1) + "," + bubbleTime + "," + quickTime);
                    }

                    double bubbleMs = bubbleTotal / (double) repeats / 1_000_000;
                    double quickMs = quickTotal / (double) repeats / 1_000_000;
                    table.printf(Locale.US, "%s,%d,%.6f,%.6f%n", name, n, bubbleMs, quickMs);
                    System.out.printf(Locale.US, "%s n=%d: bubble=%.4f ms, quick=%.4f ms%n",
                        name, n, bubbleMs, quickMs);
                }
            }
        }
    }
}
