package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lensrent.model.StatusBayar;
import com.lensrent.model.Transaksi;
import com.lensrent.repository.PembayaranRepository;
import com.lensrent.repository.TransaksiRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test PembayaranService tanpa database: repository diganti mock (tiruan). */
class PembayaranServiceTest {

    private static final LocalDate HARI_INI = LocalDate.now();

    private PembayaranRepository pembayaranRepository;
    private TransaksiRepository transaksiRepository;
    private Transaksi transaksi;
    private PembayaranService service;

    @BeforeEach
    void siapkan() {
        pembayaranRepository = mock(PembayaranRepository.class);
        transaksiRepository = mock(TransaksiRepository.class);
        transaksi = new Transaksi();
        transaksi.setTotal(300_000);
        when(transaksiRepository.findById(1L)).thenReturn(Optional.of(transaksi));
        service = new PembayaranService(pembayaranRepository, transaksiRepository, null); // tanpa database
    }

    private void sudahDibayar(long jumlah) {
        when(pembayaranRepository.totalDibayar(1L)).thenReturn(jumlah);
    }

    @Test
    void statusDihitungDariJumlahDibayar() {
        assertEquals(StatusBayar.BELUM_BAYAR, PembayaranService.status(300_000, 0));
        assertEquals(StatusBayar.LUNAS, PembayaranService.status(300_000, 300_000));
    }

    @Test
    void laporanDenganTanggalAwalSetelahAkhirDitolak() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> service.laporanTransaksi(HARI_INI, HARI_INI.minusDays(1)));
        assertTrue(e.getMessage().contains("Tanggal awal"));
    }

    @Test
    void notaTanpaTransaksiDitolak() {
        assertThrows(IllegalArgumentException.class, () -> service.nota(null));
    }

    @Test
    void notaTransaksiBelumBayarDitolak() {
        sudahDibayar(0);
        var e = assertThrows(IllegalArgumentException.class, () -> service.nota(1L));
        assertEquals("Transaksi ini belum dibayar. Catat pembayaran dulu, baru cetak nota.", e.getMessage());
    }
}
