package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.entity.RekapBaruUMPegawai;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.repo.RekapBaruUMPegawaiRepo;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RekapBaruUMPegawaiServiceImpl implements RekapBaruUMPegawaiService {

    private final RekapBaruUMPegawaiRepo rekapBaruUMPegawaiRepo;

    public RekapBaruUMPegawaiServiceImpl(RekapBaruUMPegawaiRepo rekapBaruUMPegawaiRepo) {
        this.rekapBaruUMPegawaiRepo = rekapBaruUMPegawaiRepo;
    }

    @Override
    public void deleteByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji) {
        rekapBaruUMPegawaiRepo.deleteByTahunAndBulanAndUnitGaji(tahun, bulan, unitGaji);
    }

    @Override
    public void saveAll(List<KehadiranVO> kehadiranVOList, List<JabatanBulanan> jabatanBulanansUmAsn) {
        List<RekapBaruUMPegawai> rekapBaruUMPegawais = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        kehadiranVOList.forEach(row -> {
            var rekapBaruUMPegawai = new RekapBaruUMPegawai();
            rekapBaruUMPegawai.setNip(row.getIdPegawai());
            rekapBaruUMPegawai.setNama(row.getNamaPegawai());
            rekapBaruUMPegawai.setTanggal(row.getTanggal());
            JabatanBulanan jabatanBulanan = jabatanBulanansUmAsn.stream().filter(jabnul -> row.getIdPegawai().equals(jabnul.getNip())).findAny()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "jabatan bulanan pegawai " + row.getIdPegawai() + " tidak ditemukan"));
            assert jabatanBulanan != null;
            rekapBaruUMPegawai.setTahun(jabatanBulanan.getTahun());
            rekapBaruUMPegawai.setBulan(jabatanBulanan.getBulan());
            if (jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_PNS)
                    || jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_CPNS)
                    || jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_PNSDPB)) {
                rekapBaruUMPegawai.setStatusPegawai("PNS");
            } else if (jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_PPPK)) {
                rekapBaruUMPegawai.setStatusPegawai("PPPK");
            }
            rekapBaruUMPegawai.setUnitGaji(jabatanBulanan.getUnitGaji());
            rekapBaruUMPegawai.setCreatedDate(now);
            rekapBaruUMPegawais.add(rekapBaruUMPegawai);
        });
        rekapBaruUMPegawaiRepo.saveAll(rekapBaruUMPegawais);
    }

    @Override
    public List<RekapBaruUMPegawai> findByTahunAndBulanAndUnitGaji(Integer tahun, Integer bulan, String unitGaji) {
        return rekapBaruUMPegawaiRepo.findByTahunAndBulanAndUnitGaji(tahun, bulan, unitGaji,
                Sort.by(Sort.Direction.ASC, "nip").and(Sort.by(Sort.Direction.ASC, "tanggal")));
    }

    @Override
    public List<RekapBaruUMPegawai> findByTahunAndBulan(Integer tahun, Integer bulan) {
        return rekapBaruUMPegawaiRepo.findByTahunAndBulan(tahun, bulan,
                Sort.by(Sort.Direction.ASC, "nip").and(Sort.by(Sort.Direction.ASC, "tanggal")));
    }
}
