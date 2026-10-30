package com.lensrent.repository;

import com.lensrent.model.Alat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlatRepository extends JpaRepository<Alat, Long> {

    List<Alat> findAllByOrderByNamaAsc();
}
