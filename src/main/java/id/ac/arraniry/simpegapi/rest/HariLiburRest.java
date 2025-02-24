package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.HariLibur;
import id.ac.arraniry.simpegapi.service.HariLiburService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Menambah hari libur")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@Valid @RequestBody HariLibur request) {
        return hariLiburService.create(request.getTanggal());
    }

    @Operation(summary = "Menghapus hari libur berdasarkan id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        hariLiburService.delete(id);
    }

}
