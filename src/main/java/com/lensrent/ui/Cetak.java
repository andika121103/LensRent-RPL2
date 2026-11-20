package com.lensrent.ui;

import java.util.function.Supplier;
import javax.swing.JLabel;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

/** Membuka hasil JasperReports di jendela pratinjau (ada tombol cetak dan simpan PDF). */
public final class Cetak {

    private Cetak() {
    }

    /** Membuat laporan lalu menampilkannya; jika gagal, pesannya muncul di labelPesan. */
    public static void tampilkan(JLabel labelPesan, Supplier<JasperPrint> laporan) {
        try {
            JasperViewer.viewReport(laporan.get(), false); // false: menutup pratinjau tidak menutup aplikasi
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
        }
    }
}
