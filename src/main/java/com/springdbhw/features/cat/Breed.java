package com.springdbhw.features.cat;

public enum Breed {
    SPHINX,
    ORANGE,
    STREET,
    CUTE;

    public static Breed toBreed(Object raw) {
        if (raw instanceof Breed b) {
            return b;
        }
        if (raw instanceof Number n) {
            return Breed.values()[n.intValue()];
        }
        throw new IllegalArgumentException("Unknown breed type: " + raw.getClass());
    }
}
