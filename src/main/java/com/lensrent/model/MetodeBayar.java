package com.lensrent.model;

public enum MetodeBayar {
    TUNAI("Tunai"),
    TRANSFER("Transfer");

    private final String label;

    MetodeBayar(String label) {
        this.label = label;
    }

    /** Teks yang tampil di dropdown dan tabel. */
    @Override
    public String toString() {
        return label;
    }
}
