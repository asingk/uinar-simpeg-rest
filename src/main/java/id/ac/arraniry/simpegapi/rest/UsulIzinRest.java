package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.assembler.UsulIzinModelAssembler;
import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import id.ac.arraniry.simpegapi.dto.UsulIzinEditRequest;
import id.ac.arraniry.simpegapi.dto.UsulIzinModel;
import id.ac.arraniry.simpegapi.entity.Izin;
import id.ac.arraniry.simpegapi.entity.Pegawai;
import id.ac.arraniry.simpegapi.entity.UsulIzin;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.IzinService;
import id.ac.arraniry.simpegapi.service.UsulIzinService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/usul-izin")
public class UsulIzinRest {

    private final UsulIzinService usulIzinService;
    private final KehadiranUtils kehadiranUtils;
    private final IzinService izinService;
    private final UsulIzinModelAssembler assembler;
    private final PagedResourcesAssembler<UsulIzin> pagedResourcesAssembler;

    public UsulIzinRest(UsulIzinService usulIzinService, KehadiranUtils kehadiranUtils, IzinService izinService, UsulIzinModelAssembler assembler,
                        PagedResourcesAssembler<UsulIzin> pagedResourcesAssembler) {
        this.usulIzinService = usulIzinService;
        this.kehadiranUtils = kehadiranUtils;
        this.izinService = izinService;
        this.assembler = assembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "dapatkan usulan izin berdasarkan id")
    @GetMapping("/{id}")
    public UsulIzin getById(@PathVariable String id) {
        return usulIzinService.findById(id);
    }

    @Operation(summary = "Mengubah usulan izin pegawai")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUsulizin(@PathVariable String id, @Valid @RequestBody UsulIzinEditRequest request) {
        UsulIzin usulIzin = usulIzinService.findById(id);
        if (request.getStatus() == GlobalConstants.STATUS_USUL_IZIN_DIBATALKAN) {
            cancelUsulIzin(usulIzin, request.getUpdatedBy());
        } else if (request.getStatus() == GlobalConstants.STATUS_USUL_IZIN_DISETUJUI) {
            approveUsulIzin(id, request.getUpdatedBy());
        } else if (request.getStatus() == GlobalConstants.STATUS_USUL_IZIN_DITOLAK) {
            rejectUsulizin(id, request.getUpdatedBy(), request.getKet());
        }
    }

    private void cancelUsulIzin(UsulIzin usulIzin, String updatedBy) {
        if (GlobalConstants.STATUS_USUL_IZIN_DITOLAK == usulIzin.getStatus() || GlobalConstants.STATUS_USUL_IZIN_DIBATALKAN == usulIzin.getStatus()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "usulan izin ini tidak bisa dibatalkan lagi!");
        }
        int statusUsulBefore = usulIzin.getStatus();
        usulIzin.setStatus(GlobalConstants.STATUS_USUL_IZIN_DIBATALKAN);
        Pegawai cancaledBy = new Pegawai(updatedBy, kehadiranUtils.getProfilPegawaiFromSimpegGraphql(updatedBy).getNama());
        usulIzin.setUpdatedBy(cancaledBy);
        usulIzin.setUpdatedDate(LocalDateTime.now());
        usulIzinService.save(usulIzin);
        if(GlobalConstants.STATUS_USUL_IZIN_DISETUJUI == statusUsulBefore) {
            Optional<Izin> izinOpt = izinService.findByNipAndTanggal(usulIzin.getNip(), usulIzin.getStartDate());
            if(izinOpt.isPresent()) {
                izinService.deleteById(izinOpt.get().getId());
                kehadiranUtils.undeleteKehadiran(usulIzin.getStartDate(), usulIzin.getNip());
                if (null != usulIzin.getEndDate()) {
                    List<LocalDate> tanggalList = usulIzin.getStartDate().plusDays(1).datesUntil(usulIzin.getEndDate().plusDays(1)).toList();
                    tanggalList.forEach((tanggal) -> {
                        Optional<Izin> izinOpt2 = izinService.findByNipAndTanggal(usulIzin.getNip(), tanggal);
                        if(izinOpt2.isPresent()) {
                            izinService.deleteById(izinOpt2.get().getId());
                            kehadiranUtils.undeleteKehadiran(tanggal, usulIzin.getNip());
                        }
                    });
                }
            }
        }
    }

    @Operation(summary = "mendapatkan daftar usulan izin")
    @GetMapping()
    public PagedModel<UsulIzinModel> getAll(@RequestParam(name = "status", defaultValue = "0,1,2,3") List<Integer> status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "100") int size, @RequestParam(required = false) String idPegawai) {
        if(size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<UsulIzin> usulIzinPage;
        if (null != idPegawai) {
            usulIzinPage = usulIzinService.findByNipAndStatusIn(idPegawai, status, pageable);
        } else {
            usulIzinPage = usulIzinService.findByStatusIn(status, pageable);
        }
        return pagedResourcesAssembler.toModel(usulIzinPage, assembler);
    }

    private void approveUsulIzin(String id, String updatedBy) {
        UsulIzin usulIzin = usulIzinService.findById(id);
        if (GlobalConstants.STATUS_USUL_IZIN_DIPROSES != usulIzin.getStatus()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "usulan izin sudah selesai tahap proses!");
        }
        if (kehadiranUtils.isLibur(usulIzin.getStartDate()) && null == usulIzin.getEndDate()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "tanggal ini hari libur!");
        }
        usulIzin.setStatus(GlobalConstants.STATUS_USUL_IZIN_DISETUJUI);
        Pegawai pegawai = new Pegawai();
        pegawai.setNip(updatedBy);
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(updatedBy);
        pegawai.setNama(pegawaiSimpegVO.getNama());
        usulIzin.setUpdatedBy(pegawai);
        usulIzin.setUpdatedDate(LocalDateTime.now());
        usulIzinService.save(usulIzin);
        if (!kehadiranUtils.isLibur(usulIzin.getStartDate())) {
            izinService.create(new Izin(usulIzin, usulIzin.getStartDate()));
            kehadiranUtils.safeDeleteIzin(usulIzin.getStartDate(), usulIzin.getNip(), pegawaiSimpegVO);
        }
        if (null != usulIzin.getEndDate()) {
            List<LocalDate> tanggalList = usulIzin.getStartDate().plusDays(1).datesUntil(usulIzin.getEndDate().plusDays(1)).toList();
            tanggalList.forEach((tanggal) -> {
                if (!kehadiranUtils.isLibur(tanggal)) {
                    izinService.create(new Izin(usulIzin, tanggal));
                    kehadiranUtils.safeDeleteIzin(tanggal, usulIzin.getNip(), pegawaiSimpegVO);
                }
            });
        }
    }

    private void rejectUsulizin(String id, String updatedBy, String ket) {
        UsulIzin usulIzin = usulIzinService.findById(id);
        if (GlobalConstants.STATUS_USUL_IZIN_DIPROSES != usulIzin.getStatus()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "usulan izin sudah selesai tahap proses!");
        }
        usulIzin.setStatus(GlobalConstants.STATUS_USUL_IZIN_DITOLAK);
        Pegawai pegawai = new Pegawai();
        pegawai.setNip(updatedBy);
        String updatedByNama = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(updatedBy).getNama();
        pegawai.setNama(updatedByNama);
        usulIzin.setUpdatedBy(pegawai);
        usulIzin.setUpdatedDate(LocalDateTime.now());
        usulIzin.setKet(ket);
        usulIzinService.save(usulIzin);
    }

}
