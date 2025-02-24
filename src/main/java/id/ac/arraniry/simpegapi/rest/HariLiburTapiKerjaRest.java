package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.dto.HariLiburRequest;
import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.service.HariLiburTapiKerjaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Menambah hari libur tapi kerja")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResponse create(@Valid @RequestBody HariLiburRequest request) {
        var response = new CreateResponse();
        response.setId(hariLiburTapiKerjaService.create(request.getTanggal()));
        return response;
    }

    @Operation(summary = "Menghapus hari libur tapi kerja berdasarkan id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        hariLiburTapiKerjaService.delete(id);
    }

}
