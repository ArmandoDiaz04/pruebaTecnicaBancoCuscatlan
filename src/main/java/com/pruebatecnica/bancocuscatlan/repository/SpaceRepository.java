package com.pruebaTecnica.BancoCuscatlan.repository;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

	List<Space> findByActiveTrue();

	List<Space> findByActiveFalse();

	Optional<Space> findByIdAndActiveTrue(Long id);
}
