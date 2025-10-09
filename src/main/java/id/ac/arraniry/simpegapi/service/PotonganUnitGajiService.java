package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;

import java.util.List;

public interface PotonganUnitGajiService {
    List<PotonganUnitGaji> saveAll(List<PotonganUnitGaji> gajiList);
    void deleteByBulanAndTahunAndKodeAnakSatker(Integer bulan, Integer tahun, String kodeUnitGaji);
}
