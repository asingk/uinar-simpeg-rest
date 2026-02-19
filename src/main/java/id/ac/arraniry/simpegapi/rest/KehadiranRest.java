package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.*;
import id.ac.arraniry.simpegapi.utils.SimpegGraphUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;

@RestController
@CrossOrigin
@RequestMapping("/kehadiran")
public class KehadiranRest {

    private final KehadiranUtils kehadiranUtils;
    private final KehadiranService kehadiranService;
    private final Environment environment;

    public KehadiranRest(KehadiranUtils kehadiranUtils,
                         KehadiranService kehadiranService,
                         Environment environment) {
        this.kehadiranUtils = kehadiranUtils;
        this.kehadiranService = kehadiranService;
        this.environment = environment;
    }

    @Operation(summary = "mendapatkan status waktu di server saat ini")
    @GetMapping("/status-saat-ini")
    public StatusSaatIniResponse getStatusSaatIni() {
        return kehadiranService.getStatusSaatIni();
    }

    @Operation(summary = "Merekam kehadiran pengguna saat ini")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaveResponse create(
            HttpServletRequest request, @RequestHeader("User-Agent") String userAgent, @Valid @RequestBody KehadiranCreateRequest createRequest) {
        return kehadiranService.create(request, userAgent, createRequest);
    }

    @Operation(summary = "Menambahkan kehadiran")
    @PostMapping("/tambah")
    @ResponseStatus(HttpStatus.CREATED)
    public SaveResponse addKehadiran(@Valid @RequestBody KehadiranAddRequest request) {
        return kehadiranService.add(request);
    }

    @Operation(summary = "menghapus kehadiran")
    @PostMapping("/safe-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody KehadiranUpdateRequest request) {
        kehadiranUtils.safeDeleteIzin(request.getTanggal(), request.getIdPegawai(),
                SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getUpdatedBy(), environment));
    }

    @Operation(summary = "Membatalkan kehadiran yang sudah di hapus")
    @PostMapping("/undelete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void undelete(@Valid @RequestBody KehadiranUpdateRequest request) {
        kehadiranUtils.undeleteKehadiran(request.getTanggal(), request.getIdPegawai());
    }

    @Operation(summary = "Membatalkan kehadiran yang sudah ditambahkan(hard delete)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDelete(@PathVariable String id) {
        KehadiranVO hadir = kehadiranService.findById(id);
        if(null != hadir.getIsAdded() && hadir.getIsAdded()) {
            kehadiranService.delete(hadir);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kehadiran ini tidak bisa di hard delete!");
        }
    }

}
