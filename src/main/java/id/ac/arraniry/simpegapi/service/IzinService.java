package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Izin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IzinService {
    List<Izin> findByNipAndTahunAndBulan(String idPegawai, Integer tahun, Integer bulan);
    Optional<Izin> findByNipAndTanggal(String nip, LocalDate tanggal);
    void deleteById(String id);
    String create(Izin izin);
}
