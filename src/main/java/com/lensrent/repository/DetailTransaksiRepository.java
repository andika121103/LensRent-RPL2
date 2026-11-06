package com.lensrent.repository;

import com.lensrent.model.DetailTransaksi;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DetailTransaksiRepository extends JpaRepository<DetailTransaksi, Long> {

    boolean existsByAlatId(Long alatId);

    /** Sewa alat ini yang tanggalnya bertabrakan dengan rentang yang diminta. */
    @Query("""
            select d from DetailTransaksi d join fetch d.transaksi t
            where d.alat.id = :alatId and t.tanggalSewa <= :kembali and t.tanggalKembali >= :sewa
            """)
    List<DetailTransaksi> yangBertabrakan(Long alatId, LocalDate sewa, LocalDate kembali);
}
