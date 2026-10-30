package com.lensrent.session;

import com.lensrent.model.Pengguna;

/** Menyimpan pengguna yang sedang login. Aplikasi desktop hanya punya satu sesi. */
public final class Sesi {

    private static Pengguna penggunaAktif;

    private Sesi() {}

    public static void simpan(Pengguna pengguna) {
        penggunaAktif = pengguna;
    }

    public static Pengguna getPenggunaAktif() {
        return penggunaAktif;
    }

    public static void kosongkan() {
        penggunaAktif = null;
    }
}
