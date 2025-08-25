package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Rekap;
import id.ac.arraniry.simpegapi.repo.RekapRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Override
    public List<Rekap> findByJenisRekapAndTahunAndUnitGajiId(String jenisRekap, Integer tahun, String unitGajiId) {
        return rekapRepo.findByJenisRekapAndTahunAndUnitGajiId(jenisRekap, tahun, unitGajiId, Sort.by(Sort.Direction.DESC, "bulan"));
    }

    @Override
    public List<Rekap> findByJenisRekapAndTahun(String jenisRekap, Integer tahun) {
        return rekapRepo.findByJenisRekapAndTahun(jenisRekap, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
    }
}
