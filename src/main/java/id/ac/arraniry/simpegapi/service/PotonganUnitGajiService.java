package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;

import java.util.List;

public interface PotonganUnitGajiService {
    List<PotonganUnitGaji> saveAll(List<PotonganUnitGaji> gajiList);
    void deleteByBulanAndTahunAndUnitGajiId(Integer bulan, Integer tahun, String unitGajiId);
    List<PotonganUnitGaji> findPotonganGajiPegawai(String nip, Integer tahun);
    PotonganUnitGaji findById(String id);
    List<PotonganUnitGaji> findByRekapId(String rekapId);
    void deleteByRekapId(String rekapId);
}
