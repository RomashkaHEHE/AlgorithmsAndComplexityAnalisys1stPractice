package ru.urfu.algorithms;

import java.util.Arrays;
import java.util.Random;

public final class Inputs {
    public enum Scenario {
        RANDOM, REVERSED, SORTED, FEW_UNIQUE, ALL_EQUAL, NEARLY_SORTED
    }

    private Inputs() { }

    public static int[] create(Scenario scenario, int size, long seed) {
        int[] values = new int[size];
        Random random = new Random(seed);
        switch (scenario) {
            case RANDOM -> {
                for (int i = 0; i < size; i++) {
                    values[i] = random.nextInt();
                }
            }
            case REVERSED -> {
                for (int i = 0; i < size; i++) {
                    values[i] = size - i;
                }
            }
            case SORTED, NEARLY_SORTED -> {
                for (int i = 0; i < size; i++) {
                    values[i] = i;
                }
                if (scenario == Scenario.NEARLY_SORTED && size > 1) {
                    for (int k = 0; k < Math.max(1, size / 100); k++) {
                        int first = random.nextInt(size);
                        int second = random.nextInt(size);
                        int temporary = values[first];
                        values[first] = values[second];
                        values[second] = temporary;
                    }
                }
            }
            case FEW_UNIQUE -> {
                for (int i = 0; i < size; i++) {
                    values[i] = random.nextInt(100);
                }
            }
            case ALL_EQUAL -> Arrays.fill(values, 7);
        }
        return values;
    }
}
