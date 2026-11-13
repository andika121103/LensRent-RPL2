package com.lensrent.repository;

import com.lensrent.model.Pembayaran;
import com.lensrent.model.StatusPembayaran;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PembayaranRepository extends JpaRepository<Pembayaran, Long> {

    Optional<Pembayaran> findFirstByTransaksiIdAndStatus(Long transaksiId, StatusPembayaran status);

    boolean existsByTransaksiId(Long transaksiId);

    /** Hanya pembayaran yang sudah diterima admin yang dihitung. */
    @Query("""
            select coalesce(sum(p.jumlah), 0) from Pembayaran p
            where p.transaksi.id = :transaksiId and p.status = com.lensrent.model.StatusPembayaran.DITERIMA
            """)
    long totalDibayar(Long transaksiId);
}
