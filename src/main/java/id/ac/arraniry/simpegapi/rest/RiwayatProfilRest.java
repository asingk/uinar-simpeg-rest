package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.JabBulUpdateRequest;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.service.JabatanBulananService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
@RequestMapping("/riwayat-profil")
public class RiwayatProfilRest {

    private final JabatanBulananService jabatanBulananService;

    @Autowired
    public RiwayatProfilRest(JabatanBulananService jabatanBulananService) {
        this.jabatanBulananService = jabatanBulananService;
    }

    @Operation(summary = "Mendapatkan riwayat profil pegawai berdasarkan id")
    @GetMapping("/{id}")
    public JabatanBulanan get(@PathVariable String id) {
        return jabatanBulananService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tidak ditemukan!"));
    }

    @Operation(summary = "Memperbaharui riwayat profil pegawai")
    @PutMapping("/{id}")
    public void update(@PathVariable String id, @Valid @RequestBody JabBulUpdateRequest request) {
        jabatanBulananService.update(id, request);
    }

    @Operation(summary = "Menghapus riwayat jabatan pegawai bulanan")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        jabatanBulananService.deleteById(id);
    }

}
