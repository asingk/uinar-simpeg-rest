package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapRemunPegawai;
import id.ac.arraniry.simpegapi.repo.RekapRemunPegawaiRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RekapRemunPegawaiServiceImpl implements RekapRemunPegawaiService {

    private final RekapRemunPegawaiRepo rekapRemunPegawaiRepo;

    public RekapRemunPegawaiServiceImpl(RekapRemunPegawaiRepo rekapRemunPegawaiRepo) {
        this.rekapRemunPegawaiRepo = rekapRemunPegawaiRepo;
    }

    @Override
    public List<RekapRemunPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort) {
        return rekapRemunPegawaiRepo.findByNipAndTahun(nip, tahun, sort);
    }

    @Override
    public Optional<RekapRemunPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan) {
        return rekapRemunPegawaiRepo.findByNipAndTahunAndBulan(nip, tahun, bulan);
    }

    @Override
    public Optional<RekapRemunPegawai> findById(String id) {
        return rekapRemunPegawaiRepo.findById(id);
    }

    @Override
    public void deleteByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun) {
        rekapRemunPegawaiRepo.deleteByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun);
    }

    @Override
    public void saveAll(List<RekapRemunPegawai> rekapRemunPegawaiList) {
        rekapRemunPegawaiRepo.saveAll(rekapRemunPegawaiList);
    }

    @Override
    public List<RekapRemunPegawai> findByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun) {
        return rekapRemunPegawaiRepo.findByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun, Sort.by(Sort.Direction.ASC, "nip"));
    }

    @Override
    public List<RekapRemunPegawai> findByTahunAndBulan(Integer tahun, Integer bulan) {
        return rekapRemunPegawaiRepo.findByTahunAndBulan(tahun, bulan, Sort.by(Sort.Direction.ASC, "nip"));
    }
}
