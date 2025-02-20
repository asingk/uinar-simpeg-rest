package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Rekap;

import java.util.Optional;

public interface RekapService {
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId, String unitRemunId);
    Rekap save(Rekap rekap);
}
