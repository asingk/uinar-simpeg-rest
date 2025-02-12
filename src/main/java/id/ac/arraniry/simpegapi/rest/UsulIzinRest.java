package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.UsulIzinEditRequest;
import id.ac.arraniry.simpegapi.entity.Izin;
import id.ac.arraniry.simpegapi.entity.Pegawai;
import id.ac.arraniry.simpegapi.entity.UsulIzin;
import id.ac.arraniry.simpegapi.helper.GlobalConstants;
import id.ac.arraniry.simpegapi.helper.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.IzinService;
import id.ac.arraniry.simpegapi.service.UsulIzinService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    public UsulIzinRest(UsulIzinService usulIzinService, KehadiranUtils kehadiranUtils, IzinService izinService) {
        this.usulIzinService = usulIzinService;
        this.kehadiranUtils = kehadiranUtils;
        this.izinService = izinService;
    }

    @Operation(summary = "dapatkan usulan izin berdasarkan id")
    @GetMapping("/{id}")
    public UsulIzin getById(@PathVariable String id) {
        return new UsulIzin();
    }

    @Operation(summary = "Mengubah usulan izin pegawai")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUsulizin(@PathVariable String id, @Valid @RequestBody UsulIzinEditRequest request) {
        System.out.println(request.getUpdatedBy());
        UsulIzin usulIzin = usulIzinService.findById(id);
        if (request.getStatus() == GlobalConstants.STATUS_USUL_IZIN_DIBATALKAN) {
            cancelUsulIzin(usulIzin, request.getUpdatedBy());
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

}
