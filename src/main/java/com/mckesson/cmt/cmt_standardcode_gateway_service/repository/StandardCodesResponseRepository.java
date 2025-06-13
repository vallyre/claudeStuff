package com.mckesson.cmt.cmt_standardcode_gateway_service.repository;

import com.mckesson.cmt.cmt_standardcode_gateway_service.entities.StandardCodesResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface StandardCodesResponseRepository extends JpaRepository<StandardCodesResponse, Integer> {
    Optional<StandardCodesResponse> findByVersionUuid(UUID versionUuid);

    Optional<StandardCodesResponse> findByMasterUuid(UUID masterUuid);

    @Query("SELECT r FROM StandardCodesResponse r WHERE r.masterUuid = :masterUuid AND r.effectiveEndDate IS NULL")
    Optional<StandardCodesResponse> findActiveByMasterUuid(@Param("masterUuid") UUID masterUuid);

    List<StandardCodesResponse> findByMasterUuidOrderByVersionDesc(UUID masterUuid);

    @Query("SELECT r FROM StandardCodesResponse r WHERE r.standardCodesMaster.masterUuid = :masterUuid AND r.effectiveEndDate IS NULL")
    Optional<StandardCodesResponse> findByMasterUuidAndNoEffectiveEndDate(@Param("masterUuid") UUID masterUuid);

    @Query(value = """
            SELECT DISTINCT scr.api_response
            FROM "code-bridge".standard_codes_responses scr
            JOIN "code-bridge".standard_codes_master scm ON scm.id = scr.standard_codes_master_id
            WHERE scm.master_uuid = ANY(:uuids)
            AND scm.effective_end_date IS NULL
            AND scr.effective_end_date IS NULL
            """, nativeQuery = true)
    List<String> findActiveResponsesByMasterUuids(@Param("uuids") UUID[] uuids);

    @Query(value = """
            SELECT DISTINCT api_response
            FROM "code-bridge".standard_codes_responses
            WHERE version_uuid = ANY(:uuids)
            """, nativeQuery = true)
    List<String> findResponsesByVersionUuids(@Param("uuids") UUID[] uuids);
}