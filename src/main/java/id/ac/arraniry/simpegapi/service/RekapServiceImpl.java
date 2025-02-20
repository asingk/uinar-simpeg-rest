package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Rekap;
import id.ac.arraniry.simpegapi.repo.RekapRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RekapServiceImpl implements RekapService {

    private final RekapRepo rekapRepo;

    public RekapServiceImpl(RekapRepo rekapRepo) {
        this.rekapRepo = rekapRepo;
    }

    @Override
    public Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId, String unitRemunId) {
        return rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId(jenisRekap, tahun, bulan, unitGajiId, unitRemunId);
    }

    @Override
    public Rekap save(Rekap rekap) {
        return rekapRepo.save(rekap);
    }
}
