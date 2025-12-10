package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.RekapRemunPegawai;
import id.ac.arraniry.simpegapi.entity.SelisihRekapRemunPegawai;
import id.ac.arraniry.simpegapi.repo.RekapRemunPegawaiRepo;
import id.ac.arraniry.simpegapi.repo.SelisihRekapRemunPegawaiRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RekapRemunPegawaiServiceImpl implements RekapRemunPegawaiService {

    private final RekapRemunPegawaiRepo rekapRemunPegawaiRepo;
    private final SelisihRekapRemunPegawaiRepo selisihRekapRemunPegawaiRepo;

    public RekapRemunPegawaiServiceImpl(RekapRemunPegawaiRepo rekapRemunPegawaiRepo, SelisihRekapRemunPegawaiRepo selisihRekapRemunPegawaiRepo) {
        this.rekapRemunPegawaiRepo = rekapRemunPegawaiRepo;
        this.selisihRekapRemunPegawaiRepo = selisihRekapRemunPegawaiRepo;
    }

//    @Override
//    public List<RekapRemunPegawai> findByNipAndTahun(String nip, Integer tahun, Sort sort) {
//        return rekapRemunPegawaiRepo.findByNipAndTahun(nip, tahun, sort);
//    }
//
//    @Override
//    public Optional<RekapRemunPegawai> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan) {
//        return rekapRemunPegawaiRepo.findByNipAndTahunAndBulan(nip, tahun, bulan);
//    }

    @Override
    public RekapRemunPegawai findById(String id) {
        return rekapRemunPegawaiRepo.findById(id).orElseThrow(() -> new RuntimeException("Rekap Remun Pegawai not found"));
    }

//    @Override
//    public void deleteByTahunAndBulanAndUnitRemun(Integer tahun, Integer bulan, String unitRemun) {
//        rekapRemunPegawaiRepo.deleteByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun);
//    }

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

    @Override
    public void deleteByRekapId(String rekapId) {
        rekapRemunPegawaiRepo.deleteByRekapId(rekapId);
    }

    @Override
    public void deleteSelisihByRekapId(String rekapId) {
        selisihRekapRemunPegawaiRepo.deleteByRekapId(rekapId);
    }

    @Override
    public void saveAllSelisih(List<SelisihRekapRemunPegawai> selisihRemunPegawaiList) {
        selisihRekapRemunPegawaiRepo.saveAll(selisihRemunPegawaiList);
    }

    @Override
    public List<RekapRemunPegawai> findRemunPegawai(String nip, Integer tahun, Integer bulan) {
        List<RekapRemunPegawai> result = new ArrayList<>();
        if (null != bulan) {
            rekapRemunPegawaiRepo.findByNipAndTahunAndBulan(nip, tahun, bulan).ifPresent(result::add);
        } else {
            result = rekapRemunPegawaiRepo.findByNipAndTahun(nip, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
        }
        return result;
    }

    @Override
    public List<SelisihRekapRemunPegawai> findSelisihRemunPegawai(String nip, Integer tahun, Integer bulan) {
        List<SelisihRekapRemunPegawai> result = new ArrayList<>();
        if (null != bulan) {
            selisihRekapRemunPegawaiRepo.findByNipAndTahunAndBulan(nip, tahun, bulan).ifPresent(result::add);
        } else {
            result = selisihRekapRemunPegawaiRepo.findByNipAndTahun(nip, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
        }
        return result;
    }
}
