package com.lensrent.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.swing.JLabel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

/** Hasil JasperReports langsung dijadikan PDF di Documents\LensRent lalu dibuka dengan aplikasi PDF Windows. */
public final class Cetak {

    /** Folder tempat semua nota dan laporan PDF disimpan. */
    static final File FOLDER = new File(System.getProperty("user.home"), "Documents" + File.separator + "LensRent");

    private Cetak() {
    }

    /**
     * Membuat laporan, menyimpannya sebagai FOLDER/namaFile.pdf, lalu membukanya.
     * Jika gagal (mis. belum memilih baris, atau PDF lama masih terbuka), pesannya muncul di labelPesan.
     */
    public static void pdf(JLabel labelPesan, Supplier<JasperPrint> laporan, Supplier<String> namaFile) {
        File file;
        try {
            JasperPrint hasil = laporan.get();
            file = new File(FOLDER, namaFile.get() + ".pdf");
            simpan(hasil, file);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
            return;
        } catch (JRException ex) {
            // penyebab paling umum di Windows: PDF dengan nama yang sama masih terbuka dan terkunci
            Tema.tampilkanError(labelPesan, "PDF gagal dibuat. Tutup PDF dengan nama yang sama jika sedang terbuka, lalu coba lagi.");
            return;
        }
        labelPesan.setVisible(false);
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException | UnsupportedOperationException ex) {
            Tema.tampilkanError(labelPesan, "PDF tersimpan di " + file.getAbsolutePath() + ", tetapi tidak ada aplikasi untuk membukanya.");
        }
    }

    static void simpan(JasperPrint hasil, File file) throws JRException {
        file.getParentFile().mkdirs();
        JasperExportManager.exportReportToPdfFile(hasil, file.getPath());
    }
}
