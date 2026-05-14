package com.pixelbase.backend.modules.user.repository;

import com.pixelbase.backend.modules.user.domain.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
}
