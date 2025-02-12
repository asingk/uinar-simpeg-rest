package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.service.HariLiburTapiKerjaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/hari-libur-tapi-kerja")
public class HariLiburTapiKerjaRest {

    private final HariLiburTapiKerjaService hariLiburTapiKerjaService;

    public HariLiburTapiKerjaRest(HariLiburTapiKerjaService hariLiburTapiKerjaService) {
        this.hariLiburTapiKerjaService = hariLiburTapiKerjaService;
    }

    @Operation(summary = "Mendapatkan daftar hari libur tapi kerja")
    @GetMapping
    public List<HariLiburTapiKerja> getByTahun(@RequestParam("tahun") Integer tahun) {
        return hariLiburTapiKerjaService.findByTahun(tahun);
    }
}
