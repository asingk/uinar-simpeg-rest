package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.dto.HijriahRequest;
import id.ac.arraniry.simpegapi.entity.Hijriah;
import id.ac.arraniry.simpegapi.service.HijriahService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/hijriah")
public class HijriahRest {
    private final HijriahService hijriahService;

    public HijriahRest(HijriahService hijriahService) {
        this.hijriahService = hijriahService;
    }

    @Operation(summary = "Mendapatkan daftar hijriah")
    @GetMapping
    public List<Hijriah> getAll() {
        return hijriahService.findAll();
    }

    @Operation(summary = "Mendapatkan hijriah berdasarkan id")
    @GetMapping("/{id}")
    public Hijriah findById(@PathVariable String id) {
        return hijriahService.findById(id);
    }

    @Operation(summary = "Menambah hijriah")
    @PostMapping
    public CreateResponse create(@Valid @RequestBody HijriahRequest request) {
        return new CreateResponse(hijriahService.create(request.getTahun(), request.getAwalRamadhan(), request.getAwalSyawal()));
    }

    @Operation(summary = "Mengubah hijriah berdasarkan id")
    @PutMapping("/{id}")
    public void update(@PathVariable String id, @Valid @RequestBody HijriahRequest request) {
        hijriahService.update(id, request.getTahun(), request.getAwalRamadhan(), request.getAwalSyawal());
    }

    @Operation(summary = "Menghapus hijriah berdasarkan id")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        hijriahService.delete(id);
    }

}
