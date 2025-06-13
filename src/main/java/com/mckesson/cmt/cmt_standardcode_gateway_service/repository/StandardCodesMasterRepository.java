package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.StandardCodesMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface StandardCodesMasterRepository extends JpaRepository<StandardCodesMaster, Long> {
    Optional<StandardCodesMaster> findByMasterUuid(UUID masterUuid);

    boolean existsByMasterUuid(UUID masterUuid);

    @Query("SELECT m FROM StandardCodesMaster m WHERE m.masterUuid = :masterUuid AND m.effectiveEndDate IS NULL")
    Optional<StandardCodesMaster> findActiveByMasterUuid(@Param("masterUuid") UUID masterUuid);

    List<StandardCodesMaster> findByResourceTypeAndStatus(String resourceType, String status);
}