package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapRemunPegawai;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface RekapRemunPegawaiService {
    List<RekapRemunPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort);
    Optional<RekapRemunPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    Optional<RekapRemunPegawai> findById(String id);
}
