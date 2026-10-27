package ru.urfu.algorithms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;

public final class Benchmark {
    private static final long SEED = 20261027L;
    private static volatile long checksum;

    public static void main(String[] args) throws IOException {
        int maxSize = args.length > 0 ? Integer.parseInt(args[0]) : 16384;
        int samples = args.length > 1 ? Integer.parseInt(args[1]) : 9;
        if (maxSize < 256 || maxSize > 65536 || samples < 5 || samples > 100) {
            throw new IllegalArgumentException("maxSize: 256..65536, samples: 5..100");
        }
        Path results = Path.of("results");
        Files.createDirectories(results);
        Instant started = Instant.now();
        // Exercise every input shape and both algorithms before measuring.
        for (int round = 0; round < 150; round++) {
            for (Inputs.Scenario scenario : Inputs.Scenario.values()) {
                int[] original = Inputs.create(scenario, 512, SEED + round);
                for (boolean bubble : new boolean[]{true, false}) {
                    int[] working = original.clone();
                    sort(working, bubble);
                    consume(working);
                }
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(results.resolve("raw.csv"), StandardCharsets.UTF_8)) {
            writer.write("scenario,n,algorithm,sample,elapsed_ns\n");
            for (Inputs.Scenario scenario : Inputs.Scenario.values()) {
                for (int size = 256; size <= maxSize; size *= 2) {
                    for (int sample = 0; sample < samples; sample++) {
                        long seed = SEED + scenario.ordinal() * 1_000_003L + size * 101L + sample;
                        int[] original = Inputs.create(scenario, size, seed);
                        int[] expected = original.clone();
                        Arrays.sort(expected);
                        // Alternate order to reduce a systematic first/second-run advantage.
                        for (int order = 0; order < 2; order++) {
                            boolean bubble = ((sample + order) & 1) == 0;
                            int[] working = original.clone();
                            long before = System.nanoTime();
                            sort(working, bubble);
                            long elapsed = System.nanoTime() - before;
                            if (elapsed <= 0 || !Arrays.equals(expected, working)) {
                                throw new IllegalStateException("Invalid measurement or sort result");
                            }
                            consume(working);
                            writer.write(scenario.name() + "," + size + ","
                                + (bubble ? "BubbleSort" : "QuickSort") + "," + sample + "," + elapsed + "\n");
                        }
                    }
                    System.out.println(scenario + " n=" + size + " done");
                }
            }
        }
        String environment = "started_utc=" + started + "\nfinished_utc=" + Instant.now()
            + "\njava_runtime=" + System.getProperty("java.runtime.version")
            + "\njava_vm=" + System.getProperty("java.vm.name")
            + "\nos=" + System.getProperty("os.name") + " " + System.getProperty("os.version")
            + "\narchitecture=" + System.getProperty("os.arch")
            + "\nprocessors=" + Runtime.getRuntime().availableProcessors()
            + "\ncpu=" + System.getenv("PROCESSOR_IDENTIFIER")
            + "\nseed=" + SEED + "\nmax_size=" + maxSize + "\nsamples=" + samples
            + "\nwarmup_rounds_per_scenario=150\nwarmup_size=512"
            + "\ninput_seed=seed + scenario_ordinal*1000003 + n*101 + sample"
            + "\nclock=System.nanoTime\nchecksum=" + checksum + "\n";
        Files.writeString(results.resolve("environment.txt"), environment, StandardCharsets.UTF_8);
    }

    private static void sort(int[] values, boolean bubble) {
        if (bubble) {
            Sorts.bubbleSort(values);
        } else {
            Sorts.quickSort(values);
        }
    }

    private static void consume(int[] values) {
        if (values.length > 0) {
            checksum = checksum * 31 + values[0] + values[values.length / 2] + values[values.length - 1];
        }
    }
}
