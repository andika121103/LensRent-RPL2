package com.lensrent.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Laporan asli (tanpa database: data kosong) benar-benar tersimpan sebagai PDF, folder dibuat jika belum ada. */
class CetakTest {

    @Test
    void laporanTersimpanSebagaiPdf(@TempDir Path tmp) throws Exception {
        var desain = JasperCompileManager.compileReport(getClass().getResourceAsStream("/laporan/laporan_transaksi.jrxml"));
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("tanggalAwal", java.sql.Date.valueOf("2026-11-01"));
        parameter.put("tanggalAkhir", java.sql.Date.valueOf("2026-11-30"));
        File file = tmp.resolve("LensRent/laporan.pdf").toFile(); // folder LensRent belum ada

        Cetak.simpan(JasperFillManager.fillReport(desain, parameter, new JREmptyDataSource(0)), file);

        byte[] isi = Files.readAllBytes(file.toPath());
        assertEquals("%PDF", new String(isi, 0, 4, java.nio.charset.StandardCharsets.ISO_8859_1));
    }
}
