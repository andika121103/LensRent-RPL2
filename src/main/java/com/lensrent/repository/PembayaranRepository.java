package com.lensrent.repository;

import com.lensrent.model.Pembayaran;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PembayaranRepository extends JpaRepository<Pembayaran, Long> {

    @Query("select coalesce(sum(p.jumlah), 0) from Pembayaran p where p.transaksi.id = :transaksiId")
    long totalDibayar(Long transaksiId);
}
