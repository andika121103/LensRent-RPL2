package com.lensrent.model;

/** Pembayaran online menunggu dicek admin; pembayaran di toko langsung diterima. */
public enum StatusPembayaran {
    MENUNGGU("Menunggu Konfirmasi"),
    DITERIMA("Diterima");

    private final String label;

    StatusPembayaran(String label) {
        this.label = label;
    }

    /** Teks yang tampil di tabel. */
    @Override
    public String toString() {
        return label;
    }
}
