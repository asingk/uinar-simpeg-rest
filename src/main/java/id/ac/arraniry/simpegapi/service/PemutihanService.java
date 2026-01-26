package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.PemutihanCreateRequest;
import id.ac.arraniry.simpegapi.entity.Pemutihan;

import java.time.LocalDate;
import java.util.List;

public interface PemutihanService {
    Pemutihan findByDateStringAndStatus(LocalDate tanggal, String status);
    List<Pemutihan> findByBulanAndTahun(Integer bulan, Integer tahun);
    List<Pemutihan> findByBulanAndTahunAndStatus(Integer bulan, Integer tahun, String status);
    String create(PemutihanCreateRequest request);
    void delete(String id);
}
