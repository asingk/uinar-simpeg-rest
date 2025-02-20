package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.dto.JabBulCreateRequest;
import id.ac.arraniry.simpegapi.dto.JabBulUpdateRequest;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface JabatanBulananService {
    Optional<JabatanBulanan> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    List<JabatanBulanan> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    List<JabatanBulanan> findByUnitGajiAndTahunAndBulanAndIdStatusPegawaiIn(String unitGaji, Integer tahun, Integer bulan, List<Integer> idStatusPegList);
    List<JabatanBulanan> findRemunByUnitRemunAndTahunAndBulan(String unitRemun, Integer tahun, Integer bulan);
    CreateResponse create(String nip, JabBulCreateRequest request);
    Optional<JabatanBulanan> findById(String id);
    void update(String id, JabBulUpdateRequest request);
    void deleteById(String id);
}
