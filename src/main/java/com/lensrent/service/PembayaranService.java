package com.lensrent.service;

import com.lensrent.model.StatusBayar;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.PembayaranRepository;
import com.lensrent.repository.TransaksiRepository;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;

/**
 * Logika pembayaran di toko dan cetak nota/laporan. Status bayar tidak disimpan, tetapi dihitung dari jumlah yang sudah dibayar.
 * Jika input tidak valid, method melempar IllegalArgumentException berisi pesan
 * yang langsung bisa ditampilkan ke pengguna.
 */
@Service
public class PembayaranService {

    private final PembayaranRepository pembayaranRepository;
    private final TransaksiRepository transaksiRepository;
    private final DataSource dataSource;

    public PembayaranService(PembayaranRepository pembayaranRepository, TransaksiRepository transaksiRepository,
                             DataSource dataSource) {
        this.pembayaranRepository = pembayaranRepository;
        this.transaksiRepository = transaksiRepository;
        this.dataSource = dataSource;
    }

    /** Tanpa DP: Lunas jika yang dibayar sudah mencapai total, selain itu Belum Bayar. */
    public static StatusBayar status(long total, long dibayar) {
        return dibayar >= total ? StatusBayar.LUNAS : StatusBayar.BELUM_BAYAR;
    }

    /** Nota satu transaksi (laporan/nota_sewa.jrxml): hanya untuk yang sudah lunas, karena alat diserahkan setelah dibayar. */
    public JasperPrint nota(Long transaksiId) {
        if (transaksiId == null) {
            throw new IllegalArgumentException("Pilih transaksi terlebih dahulu.");
        }
        Transaksi transaksi = transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan."));
        if (status(transaksi.getTotal(), pembayaranRepository.totalDibayar(transaksiId)) != StatusBayar.LUNAS) {
            throw new IllegalArgumentException("Transaksi ini belum dibayar. Catat pembayaran dulu, baru cetak nota.");
        }
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("transaksiId", transaksiId);
        return isiLaporan("nota_sewa.jrxml", parameter);
    }

    /** Laporan semua transaksi dengan tanggal sewa di antara awal dan akhir (laporan/laporan_transaksi.jrxml). */
    public JasperPrint laporanTransaksi(LocalDate awal, LocalDate akhir) {
        if (awal.isAfter(akhir)) {
            throw new IllegalArgumentException("Tanggal awal tidak boleh setelah tanggal akhir.");
        }
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("tanggalAwal", java.sql.Date.valueOf(awal));
        parameter.put("tanggalAkhir", java.sql.Date.valueOf(akhir));
        return isiLaporan("laporan_transaksi.jrxml", parameter);
    }

    // shortcut: file .jrxml di-compile setiap kali dicetak (±1 detik); simpan .jasper hasil compile jika terasa lambat
    private JasperPrint isiLaporan(String namaFile, Map<String, Object> parameter) {
        parameter.put(JRParameter.REPORT_LOCALE, Locale.forLanguageTag("id-ID")); // Rp 25.000, bukan Rp 25,000
        try (InputStream file = getClass().getResourceAsStream("/laporan/" + namaFile);
             Connection koneksi = dataSource.getConnection()) {
            if (file == null) {
                throw new IllegalStateException("File laporan " + namaFile + " tidak ditemukan.");
            }
            return JasperFillManager.fillReport(JasperCompileManager.compileReport(file), parameter, koneksi);
        } catch (JRException | SQLException | IOException e) {
            throw new IllegalStateException("Gagal membuat laporan: " + e.getMessage(), e);
        }
    }
}
