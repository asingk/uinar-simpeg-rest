package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.repo.JabatanBulananRepo;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import id.ac.arraniry.simpegapi.utils.SimpegGraphUtils;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JabatanBulananServiceImpl implements JabatanBulananService {

    private final JabatanBulananRepo jabatanBulananRepo;
    private final KehadiranUtils kehadiranUtils;
    private final Environment environment;

    public JabatanBulananServiceImpl(JabatanBulananRepo jabatanBulananRepo, KehadiranUtils kehadiranUtils, Environment environment) {
        this.jabatanBulananRepo = jabatanBulananRepo;
        this.kehadiranUtils = kehadiranUtils;
        this.environment = environment;
    }

    @Override
    public Optional<JabatanBulanan> findByNipAndTahunAndBulan(String nip, Integer tahun, Integer bulan) {
        return jabatanBulananRepo.findByNipAndTahunAndBulan(nip, tahun, bulan);
    }

    @Override
    public List<JabatanBulanan> findByNipAndTahun(String nip, Integer tahun, Sort sort) {
        return jabatanBulananRepo.findByNipAndTahun(nip, tahun, sort);
    }

    @Override
    public List<JabatanBulanan> findByUnitGajiAndTahunAndBulanAndIdStatusPegawaiIn(String unitGaji, Integer tahun, Integer bulan, List<Integer> idStatusPegList) {
        return jabatanBulananRepo.findByUnitGajiAndTahunAndBulanAndIdStatusPegawaiIn(unitGaji, tahun, bulan, idStatusPegList, Sort.by(Sort.Direction.ASC, "nip"));
    }

    @Override
    public List<JabatanBulanan> findRemunByUnitRemunAndTahunAndBulan(String unitRemun, Integer tahun, Integer bulan) {
        return jabatanBulananRepo.findRemunByUnitRemunAndTahunAndBulan(unitRemun, tahun, bulan, Sort.by(Sort.Direction.ASC, "nip"));
    }

    @Override
    public CreateResponse create(String nip, JabBulCreateRequest request) {
        JabatanBulanan jabatanBulanan = new JabatanBulanan();
        jabatanBulanan.setNip(nip);
        jabatanBulanan.setNama(request.getNamaPegawai());
        jabatanBulanan.setGolongan(request.getGolongan());
        jabatanBulanan.setIdStatusPegawai(request.getIdStatusPegawai());
        StatusPegawaiSimpegVO statusPegawai = kehadiranUtils.getNamaStatusPegawaiByIdFromSimpegGraphql(request.getIdStatusPegawai());
        jabatanBulanan.setNamaStatusPegawai(statusPegawai.getNama());
        jabatanBulanan.setJenisJabatan(request.getJenisJabatan());
        jabatanBulanan.setUnitGaji(request.getUnitGaji());
        jabatanBulanan.setUnitRemun(request.getUnitRemun());
        jabatanBulanan.setRemunGrade(request.getRemunGrade());
        jabatanBulanan.setGrade(request.getGrade());
        jabatanBulanan.setTahun(request.getTahun());
        jabatanBulanan.setBulan(request.getBulan());
        jabatanBulanan.setImplementasiRemun(request.getImplementasiRemun());
        jabatanBulanan.setPajak(request.getPajak());
        jabatanBulanan.setUangMakanHarian(request.getUangMakanHarian());
        PegawaiSimpegVO pegawaiSimpegVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getAdmin(), environment);
        jabatanBulanan.setCreatedBy(pegawaiSimpegVO.getNama());
        jabatanBulanan.setCreatedDate(LocalDateTime.now());
        try {
            return new CreateResponse(jabatanBulananRepo.save(jabatanBulanan).getId());
        } catch (DuplicateKeyException dke) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "rekaman sudah ada!");
        }
    }

    @Override
    public Optional<JabatanBulanan> findById(String id) {
        return jabatanBulananRepo.findById(id);
    }

    @Override
    public void update(String id, JabBulUpdateRequest request) {
        Optional<JabatanBulanan> opt = jabatanBulananRepo.findById(id);
        if(opt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "riwayat profil tidak ditemukan!");
        JabatanBulanan jabbul = opt.get();
        jabbul.setGolongan(request.getGolongan());
        jabbul.setJenisJabatan(request.getJenisJabatan());
        jabbul.setUnitGaji(request.getUnitGaji());
        jabbul.setUnitRemun(request.getUnitRemun());
        jabbul.setRemunGrade(request.getRemunGrade());
        jabbul.setGrade(request.getGrade());
        jabbul.setImplementasiRemun(request.getImplementasiRemun());
        jabbul.setPajak(request.getPajak());
        jabbul.setUangMakanHarian(request.getUangMakanHarian());
        PegawaiSimpegVO pegawaiSimpegVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getAdmin(), environment);
        jabbul.setLastModifiedBy(pegawaiSimpegVO.getNama());
        jabbul.setLastModifiedDate(LocalDateTime.now());
        jabatanBulananRepo.save(jabbul);
    }

    @Override
    public void deleteById(String id) {
        jabatanBulananRepo.deleteById(id);
    }

}
