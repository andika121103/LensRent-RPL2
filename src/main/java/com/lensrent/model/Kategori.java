package com.lensrent.model;

public enum Kategori {
    KAMERA("Kamera"),
    LENSA("Lensa"),
    LIGHTING("Lighting"),
    TRIPOD("Tripod"),
    AKSESORIS("Aksesoris");

    private final String label;

    Kategori(String label) {
        this.label = label;
    }

    /** Teks yang tampil di dropdown dan tabel. */
    @Override
    public String toString() {
        return label;
    }
}
