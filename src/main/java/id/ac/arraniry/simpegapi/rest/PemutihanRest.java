package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.dto.PemutihanCreateRequest;
import id.ac.arraniry.simpegapi.entity.Pemutihan;
import id.ac.arraniry.simpegapi.service.PemutihanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/pemutihan")
public class PemutihanRest {

    private final PemutihanService pemutihanService;

    public PemutihanRest(PemutihanService pemutihanService) {
        this.pemutihanService = pemutihanService;
    }

    @Operation(summary = "Mendapatkan daftar pemutihan bulanan")
    @GetMapping
    public List<Pemutihan> getBulanan(@RequestParam("bulan") Integer bulan, @RequestParam("tahun") Integer tahun) {
        return pemutihanService.findByBulanAndTahun(bulan, tahun);
    }

    @Operation(summary = "Membuat tanggal pemutihan baru")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResponse create(@Valid @RequestBody PemutihanCreateRequest request) {
        return new CreateResponse(pemutihanService.create(request));
    }

    @Operation(summary = "Menghapus pemutihan berdasarkan id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        pemutihanService.delete(id);
    }

}
