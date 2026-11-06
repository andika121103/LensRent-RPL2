package com.lensrent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.model.StatusAlat;
import com.lensrent.repository.AlatRepository;
import com.lensrent.repository.DetailTransaksiRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test AlatService tanpa database: repository diganti mock (tiruan). */
class AlatServiceTest {

    private AlatRepository repository;
    private DetailTransaksiRepository detailRepository;
    private AlatService alatService;

    @BeforeEach
    void siapkan() {
        repository = mock(AlatRepository.class);
        when(repository.save(any(Alat.class))).thenAnswer(i -> i.getArgument(0));
        detailRepository = mock(DetailTransaksiRepository.class);
        alatService = new AlatService(repository, detailRepository);
    }

    private Alat alat(String nama, Kategori kategori, String merek, int stok, StatusAlat status) {
        Alat a = new Alat();
        a.setNama(nama);
        a.setKategori(kategori);
        a.setMerek(merek);
        a.setStok(stok);
        a.setStatus(status);
        return a;
    }

    @Test
    void tambahAlatBerhasil() {
        Alat a = alatService.simpan(null, " Canon EOS 80D ", Kategori.KAMERA, "Canon", "150.000", "3",
                StatusAlat.TERSEDIA, "  ");
        assertEquals("Canon EOS 80D", a.getNama());
        assertEquals(150000, a.getHargaPerHari());
        assertEquals(3, a.getStok());
        assertNull(a.getKeterangan());
    }

    @Test
    void ubahAlatMemperbaruiDataLama() {
        Alat lama = alat("Canon EOS 80D", Kategori.KAMERA, "Canon", 3, StatusAlat.TERSEDIA);
        when(repository.findById(1L)).thenReturn(Optional.of(lama));
        alatService.simpan(1L, "Canon EOS 90D", Kategori.KAMERA, "Canon", "200000", "0",
                StatusAlat.TIDAK_TERSEDIA, "Sedang diservis");
        assertEquals("Canon EOS 90D", lama.getNama());
        assertEquals(0, lama.getStok());
        assertEquals(StatusAlat.TIDAK_TERSEDIA, lama.getStatus());
    }

    @Test
    void ubahAlatYangSudahDihapusDitolak() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(9L, "Lensa 50mm",
                Kategori.LENSA, "Canon", "50000", "1", StatusAlat.TERSEDIA, null));
        verify(repository, never()).save(any());
    }

    @Test
    void inputTidakValidDitolak() {
        // nama kosong, kategori kosong, harga huruf, harga 0, harga negatif, stok negatif, nama terlalu panjang
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, " ", Kategori.LENSA, "Canon", "50000", "1", StatusAlat.TERSEDIA, null));
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, "Lensa", null, "Canon", "50000", "1", StatusAlat.TERSEDIA, null));
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, "Lensa", Kategori.LENSA, "Canon", "lima puluh", "1", StatusAlat.TERSEDIA, null));
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, "Lensa", Kategori.LENSA, "Canon", "0", "1", StatusAlat.TERSEDIA, null));
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, "Lensa", Kategori.LENSA, "Canon", "-5000", "1", StatusAlat.TERSEDIA, null));
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, "Lensa", Kategori.LENSA, "Canon", "50000", "-1", StatusAlat.TERSEDIA, null));
        assertThrows(IllegalArgumentException.class, () -> alatService.simpan(null, "x".repeat(101), Kategori.LENSA, "Canon", "50000", "1", StatusAlat.TERSEDIA, null));
        verify(repository, never()).save(any());
    }

    @Test
    void cariBerdasarkanNamaMerekDanKategori() {
        when(repository.findAllByOrderByNamaAsc()).thenReturn(List.of(
                alat("EOS 80D", Kategori.KAMERA, "Canon", 2, StatusAlat.TERSEDIA),
                alat("Lensa 50mm", Kategori.LENSA, "Canon", 1, StatusAlat.TERSEDIA),
                alat("Tripod Pro", Kategori.TRIPOD, "Manfrotto", 4, StatusAlat.TERSEDIA)));

        assertEquals(3, alatService.cari("", null).size());
        assertEquals(2, alatService.cari("CANON", null).size()); // cocok di merek, huruf besar/kecil sama
        assertEquals(1, alatService.cari("canon", Kategori.LENSA).size());
        assertEquals(0, alatService.cari("nikon", null).size());
    }

    @Test
    void bisaDisewaHanyaJikaTersediaDanStokAda() {
        assertTrue(alat("A", Kategori.KAMERA, "Canon", 1, StatusAlat.TERSEDIA).isBisaDisewa());
        assertFalse(alat("A", Kategori.KAMERA, "Canon", 0, StatusAlat.TERSEDIA).isBisaDisewa());
        assertFalse(alat("A", Kategori.KAMERA, "Canon", 5, StatusAlat.TIDAK_TERSEDIA).isBisaDisewa());
    }

    @Test
    void hapusMemanggilRepository() {
        alatService.hapus(5L);
        verify(repository).deleteById(5L);
    }

    @Test
    void hapusAlatYangPernahDisewaDitolak() {
        when(detailRepository.existsByAlatId(5L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> alatService.hapus(5L));
        verify(repository, never()).deleteById(any());
    }
}
