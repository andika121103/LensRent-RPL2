package com.lensrent.repository;

import com.lensrent.model.Transaksi;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {

    // join fetch: tabel di layar membaca detail setelah sesi database ditutup
    @Query("""
            select distinct t from Transaksi t
            join fetch t.penyewa
            left join fetch t.detail d
            left join fetch d.alat
            order by t.tanggalSewa desc, t.id desc, d.id
            """)
    List<Transaksi> semuaLengkap();
}
