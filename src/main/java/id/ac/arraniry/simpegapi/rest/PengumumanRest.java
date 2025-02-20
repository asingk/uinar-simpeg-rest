package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.dto.PengumumanRequest;
import id.ac.arraniry.simpegapi.dto.StatusPegawaiSimpegVO;
import id.ac.arraniry.simpegapi.entity.Pengumuman;
import id.ac.arraniry.simpegapi.entity.StatusPegawai;
import id.ac.arraniry.simpegapi.service.PengumumanService;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pengumuman")
@CrossOrigin
public class PengumumanRest {

    private final PengumumanService pengumumanService;
    private final KehadiranUtils kehadiranUtils;

    public PengumumanRest(PengumumanService pengumumanService, KehadiranUtils kehadiranUtils) {
        this.pengumumanService = pengumumanService;
        this.kehadiranUtils = kehadiranUtils;
    }

    @GetMapping
    public List<Pengumuman> getAll() {
        return pengumumanService.findAll();
    }

    @GetMapping("/{id}")
    public Pengumuman getById(@PathVariable String id) {
        return pengumumanService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengumuman tidak ditemukan"));
    }

    @GetMapping("/active")
    public Pengumuman getActive() {
        return pengumumanService.findByIsActive(true).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengumuman tidak ditemukan"));
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResponse create(@Valid @RequestBody PengumumanRequest request) {
        Optional<Pengumuman> pengumumanAktif = pengumumanService.findByIsActive(true);
        if(request.getIsActive() && pengumumanAktif.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "sudah ada pengumuman yang aktif");
        }
        Pengumuman pengumunan = new Pengumuman();
        pengumunan.setIsActive(request.getIsActive());
        pengumunan.setMessage(request.getMessage());
        pengumunan.setNama(request.getNama());
        List<StatusPegawai> statusPegawaiList = new ArrayList<>();
        request.getStatusPegawaiId().forEach(row -> {
            StatusPegawaiSimpegVO statusPegawaiSimpegVO = kehadiranUtils.getNamaStatusPegawaiByIdFromSimpegGraphql(row);
            StatusPegawai statusPegawai = new StatusPegawai();
            statusPegawai.setId(statusPegawaiSimpegVO.getId());
            statusPegawai.setNama(statusPegawaiSimpegVO.getNama());
            statusPegawaiList.add(statusPegawai);
        });
        pengumunan.setStatusPegawai(statusPegawaiList);
        pengumunan.setJenisJabatan(request.getJenisJabatan());

        return pengumumanService.create(pengumunan);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @Valid @RequestBody PengumumanRequest request) {
        Pengumuman pengumuman = pengumumanService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengumuman tidak ditemukan"));
        Optional<Pengumuman> pengumumanAktif = pengumumanService.findByIsActive(true);
        if (request.getIsActive() && pengumumanAktif.isPresent() && !id.equals(pengumumanAktif.get().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "sudah ada pengumuman yang aktif");
        }
        pengumuman.setIsActive(request.getIsActive());
        pengumuman.setMessage(request.getMessage());
        pengumuman.setNama(request.getNama());
        pengumuman.getStatusPegawai().clear();
        request.getStatusPegawaiId().forEach(row -> {
            StatusPegawaiSimpegVO statusPegawaiSimpegVO = kehadiranUtils.getNamaStatusPegawaiByIdFromSimpegGraphql(row);
            StatusPegawai statusPegawai = new StatusPegawai();
            statusPegawai.setId(statusPegawaiSimpegVO.getId());
            statusPegawai.setNama(statusPegawaiSimpegVO.getNama());
            pengumuman.getStatusPegawai().add(statusPegawai);
        });
        pengumuman.getJenisJabatan().clear();
        pengumuman.getJenisJabatan().addAll(request.getJenisJabatan());
        pengumumanService.update(pengumuman);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        pengumumanService.deleteById(id);
    }

}
