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
}
