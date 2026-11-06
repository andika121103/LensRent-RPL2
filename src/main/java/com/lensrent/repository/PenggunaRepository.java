package com.lensrent.repository;

import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenggunaRepository extends JpaRepository<Pengguna, Long> {

    Optional<Pengguna> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Pengguna> findByRoleAndAktifTrueOrderByNamaAsc(Role role);
}
