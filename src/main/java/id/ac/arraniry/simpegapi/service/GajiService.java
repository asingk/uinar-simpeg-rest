package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Gaji;

import java.util.List;

public interface GajiService {
    List<Gaji> saveAll(List<Gaji> gajiList);
    void deleteByBulanAndTahunAndKodeAnakSatker(Integer bulan, Integer tahun, String kodeUnitGaji);
    List<Gaji> findByNipAndTahun(String nip, Integer tahun);
    Gaji findById(String id);
}
