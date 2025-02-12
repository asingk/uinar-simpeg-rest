package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface JabatanBulananService {
    Optional<JabatanBulanan> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan);
    List<JabatanBulanan> findByNipAndTahun(String nip, Integer tahun, Sort sort);
}
