package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.KategoriIzin;
import id.ac.arraniry.simpegapi.service.KategoriIzinService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/kategori-izin")
public class KategoriIzinRest {

    private final KategoriIzinService kategoriIzinService;

    public KategoriIzinRest(KategoriIzinService kategoriIzinService) {
        this.kategoriIzinService = kategoriIzinService;
    }

    @Operation(summary = "Mendapatkan daftar kategori izin")
    @GetMapping
    public List<KategoriIzin> getAll() {
        return kategoriIzinService.findAll();
    }
}
