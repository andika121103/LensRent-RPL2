package com.lensrent.model;

public enum StatusAlat {
    TERSEDIA("Tersedia"),
    TIDAK_TERSEDIA("Tidak Tersedia");

    private final String label;

    StatusAlat(String label) {
        this.label = label;
    }

    /** Teks yang tampil di dropdown dan tabel. */
    @Override
    public String toString() {
        return label;
    }
}
