package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapUMPegawai;
import id.ac.arraniry.simpegapi.repo.RekapUMPegawaiRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RekapUMPegawaiServiceImpl implements RekapUMPegawaiService {

    private final RekapUMPegawaiRepo rekapUMPegawaiRepo;

    public RekapUMPegawaiServiceImpl(RekapUMPegawaiRepo rekapUMPegawaiRepo) {
        this.rekapUMPegawaiRepo = rekapUMPegawaiRepo;
    }

    @Override
    public List<RekapUMPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort) {
        return rekapUMPegawaiRepo.findByNipAndTahun(nip, tahun, sort);
    }

    @Override
    public Optional<RekapUMPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan) {
        return rekapUMPegawaiRepo.findByNipAndTahunAndBulan(nip, tahun,bulan);
    }

    @Override
    public Optional<RekapUMPegawai> findById(String id) {
        return rekapUMPegawaiRepo.findById(id);
    }

    @Override
    public void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji) {
        rekapUMPegawaiRepo.deleteByTahunAndBulanAndUnitGaji(tahun, bulan, unitGaji);
    }

    @Override
    public void saveAll(List<RekapUMPegawai> rekapPegawais) {
        rekapUMPegawaiRepo.saveAll(rekapPegawais);
    }

    @Override
    public List<RekapUMPegawai> findByTahunAndBulanAndStatusPegawaiAndUnitGaji(Integer tahun, Integer bulan, String statusPegawai, String unitGaji) {
        return rekapUMPegawaiRepo.findByTahunAndBulanAndStatusPegawaiAndUnitGaji(tahun, bulan, statusPegawai, unitGaji,
                Sort.by(Sort.Direction.ASC, "nip"));
    }

    @Override
    public List<RekapUMPegawai> findByTahunAndBulanAndStatusPegawai(Integer tahun, Integer bulan, String statusPegawai) {
        return rekapUMPegawaiRepo.findByTahunAndBulanAndStatusPegawai(tahun, bulan, statusPegawai,
                Sort.by(Sort.Direction.ASC, "nip"));
    }
}
