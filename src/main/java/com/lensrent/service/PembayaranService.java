package com.lensrent.service;

import com.lensrent.model.MetodeBayar;
import com.lensrent.model.Pembayaran;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Semua transaksi, untuk halaman admin. */
    public List<Transaksi> semuaTransaksi() {
        return transaksiRepository.semuaLengkap();
    }

    public long dibayar(Transaksi transaksi) {
        return pembayaranRepository.totalDibayar(transaksi.getId());
    }

    /** Teks kolom Status: Belum Bayar atau Lunas. */
    public String keterangan(Transaksi transaksi) {
        return status(transaksi.getTotal(), dibayar(transaksi)).toString();
    }

    /** Tanpa DP: Lunas jika yang dibayar sudah mencapai total, selain itu Belum Bayar. */
    public static StatusBayar status(long total, long dibayar) {
        return dibayar >= total ? StatusBayar.LUNAS : StatusBayar.BELUM_BAYAR;
    }

    // shortcut: cek sisa lalu simpan tanpa mengunci baris; aman untuk satu admin di satu PC, tambah kunci PESSIMISTIC_WRITE jika dipakai bersamaan
    /** Penyewa membayar di toko (tunai atau transfer): langsung lunas sebesar sisa tagihan, tanpa DP. */
    @Transactional
    public Pembayaran catat(Long transaksiId, LocalDate tanggal, MetodeBayar metode) {
        if (transaksiId == null) {
            throw new IllegalArgumentException("Pilih transaksi terlebih dahulu.");
        }
        Transaksi transaksi = transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan."));

        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal bayar wajib diisi.");
        }
        if (tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal bayar tidak boleh setelah hari ini.");
        }
        if (metode == null) {
            throw new IllegalArgumentException("Pilih metode bayar.");
        }

        long sisa = transaksi.getTotal() - pembayaranRepository.totalDibayar(transaksiId);
        if (sisa <= 0) {
            throw new IllegalArgumentException("Transaksi ini sudah lunas.");
        }

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTransaksi(transaksi);
        pembayaran.setJumlah(sisa);
        pembayaran.setTanggal(tanggal);
        pembayaran.setMetode(metode);
        return pembayaranRepository.save(pembayaran);
    }

    /** Nota satu transaksi (laporan/nota_sewa.jrxml): dicetak admin. */
    public JasperPrint nota(Long transaksiId) {
        if (transaksiId == null) {
            throw new IllegalArgumentException("Pilih transaksi terlebih dahulu.");
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
