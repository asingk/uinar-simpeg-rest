package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.repo.JabatanBulananRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JabatanBulananServiceImpl implements JabatanBulananService {

    private final JabatanBulananRepo jabatanBulananRepo;

    public JabatanBulananServiceImpl(JabatanBulananRepo jabatanBulananRepo) {
        this.jabatanBulananRepo = jabatanBulananRepo;
    }

    @Override
    public Optional<JabatanBulanan> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan) {
        return jabatanBulananRepo.findByNipAndTahunAndBulan(nip, tahun, bulan);
    }

    @Override
    public List<JabatanBulanan> findByNipAndTahun(String nip, Integer tahun, Sort sort) {
        return jabatanBulananRepo.findByNipAndTahun(nip, tahun, sort);
    }
}
