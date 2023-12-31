package com.ssafy.flowerly.address.repository;

import com.ssafy.flowerly.entity.Dong;
import com.ssafy.flowerly.entity.Sido;
import com.ssafy.flowerly.entity.Sigungu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DongRepository extends JpaRepository<Dong, Long> {

    Optional<Dong> findByDongNameAndSigungu(String dongName, Sigungu sigungu);

    Optional<Dong> findByDongCodeAndSigungu(Long dongCode, Sigungu sigungu);

    @Query(
            "SELECT d FROM Dong d " +
                    "WHERE d.dongName = '전체' " +
                    "AND d.sigungu IN (Select distinct(gu.sigunguCode) From Sigungu gu WHERE gu.sido = :sido)")
    List<Dong> findByDongCodeAllCode(Sido sido);

    Optional<Page<Dong>> findDongsBySigunguSigunguCode(Pageable pageable, Long sigunguCode);

}
