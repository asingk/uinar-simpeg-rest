package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.HariLibur;
import id.ac.arraniry.simpegapi.service.HariLiburService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/hari-libur")
public class HariLiburRest {

    private final HariLiburService hariLiburService;

    public HariLiburRest(HariLiburService hariLiburService) {
        this.hariLiburService = hariLiburService;
    }

    @Operation(summary = "Mendapatkan daftar hari libur tahunan")
    @GetMapping
    public List<HariLibur> getByTahun(@RequestParam("tahun") Integer tahun) {
        return hariLiburService.findByTahun(tahun);
    }
}
