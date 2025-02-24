package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.JamKerjaRequest;
import id.ac.arraniry.simpegapi.dto.JamKerjaResponse;
import id.ac.arraniry.simpegapi.service.JamKerjaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/jam-kerja")
public class JamKerjaRest {

    private final JamKerjaService jamKerjaService;

    public JamKerjaRest(JamKerjaService jamKerjaService) {
        this.jamKerjaService = jamKerjaService;
    }

    @Operation(summary = "Mendapatkan daftar jam kerja")
    @GetMapping
    List<JamKerjaResponse> getAll(@RequestParam(name = "isRamadhan", required = false) Boolean isRamadhan) {
        List<JamKerjaResponse> jamKerjaResponseList = new ArrayList<>();
        if (null != isRamadhan) {
            jamKerjaService.findByIsRamadhan(isRamadhan).forEach((row) -> {
                var jamKerjaResponse = new JamKerjaResponse(row);
                jamKerjaResponseList.add(jamKerjaResponse);
            });
        } else {
            jamKerjaService.findAll().forEach((row) -> {
                var jamKerjaResponse = new JamKerjaResponse(row);
                jamKerjaResponseList.add(jamKerjaResponse);
            });
        }
        return jamKerjaResponseList;
    }

    @Operation(summary = "Mendapatkan jam kerja berdasarkan id")
    @GetMapping("/{id}")
    JamKerjaResponse getById(@PathVariable String id) {
        return new JamKerjaResponse(jamKerjaService.findById(id));
    }

    @Operation(summary = "Mendapatkan jam kerja berdasarkan hari dan isRamadhan")
    @GetMapping("/search")
    JamKerjaResponse search(@RequestParam("hari") Integer hari, @RequestParam("isRamadhan") Boolean isRamadhan) {
        return new JamKerjaResponse(jamKerjaService.findByHariAndIsRamadhan(hari, isRamadhan));
    }

    @Operation(summary = "Mengubah jam kerja berdasarkan id")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editById(@PathVariable String id,
                  @RequestBody @Valid JamKerjaRequest jamKerja) {
        var existedJamKerja = jamKerjaService.findById(id);
        existedJamKerja.setJamDatangStart(LocalTime.of(jamKerja.getJamDatangStart().getHour(), jamKerja.getJamDatangStart().getMinute()).toString());
        existedJamKerja.setJamDatangEnd(LocalTime.of(jamKerja.getJamDatangEnd().getHour(), jamKerja.getJamDatangEnd().getMinute()).toString());
        existedJamKerja.setJamPulangStart(LocalTime.of(jamKerja.getJamPulangStart().getHour(), jamKerja.getJamPulangStart().getMinute()).toString());
        existedJamKerja.setJamPulangEnd(LocalTime.of(jamKerja.getJamPulangEnd().getHour(), jamKerja.getJamPulangEnd().getMinute()).toString());
        existedJamKerja.setJamLemburStart(LocalTime.of(jamKerja.getJamLemburStart().getHour(), jamKerja.getJamLemburStart().getMinute()).toString());
        existedJamKerja.setJamLemburEnd(LocalTime.of(jamKerja.getJamLemburEnd().getHour(), jamKerja.getJamLemburEnd().getMinute()).toString());
        existedJamKerja.setJamDatangBatas(LocalTime.of(jamKerja.getJamDatangBatas().getHour(), jamKerja.getJamDatangBatas().getMinute()).toString());
        existedJamKerja.setJamPulangBatas(LocalTime.of(jamKerja.getJamPulangBatas().getHour(), jamKerja.getJamPulangBatas().getMinute()).toString());
        jamKerjaService.update(existedJamKerja);
    }

    @Operation(summary = "Mengubah jam kerja hari berdasarkan isRamadhan")
    @PostMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editByHari(@RequestBody @Valid JamKerjaRequest request) {
//        for (int hari=1; hari <=7; hari++) {
//            if (hari == 5) continue;
//            updateJamKerja(hari, jamKerja);
//        }
        request.getHari().forEach(hari -> updateJamKerja(hari, request));
    }

//    @Operation(summary = "Mengubah jam kerja hari jumat berdasarkan isRamadhan")
//    @PutMapping("/jumat")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    void editJumat(@RequestBody @Valid JamKerjaRequest jamKerja) {
//        updateJamKerja(5, jamKerja);
//    }

    private void updateJamKerja(Integer hari, JamKerjaRequest jamKerja) {
        var existedJamKerja = jamKerjaService.findByHariAndIsRamadhan(hari, jamKerja.getIsRamadhan());
        existedJamKerja.setJamDatangStart(LocalTime.of(jamKerja.getJamDatangStart().getHour(), jamKerja.getJamDatangStart().getMinute()).toString());
        existedJamKerja.setJamDatangEnd(LocalTime.of(jamKerja.getJamDatangEnd().getHour(), jamKerja.getJamDatangEnd().getMinute()).toString());
        existedJamKerja.setJamPulangStart(LocalTime.of(jamKerja.getJamPulangStart().getHour(), jamKerja.getJamPulangStart().getMinute()).toString());
        existedJamKerja.setJamPulangEnd(LocalTime.of(jamKerja.getJamPulangEnd().getHour(), jamKerja.getJamPulangEnd().getMinute()).toString());
        existedJamKerja.setJamLemburStart(LocalTime.of(jamKerja.getJamLemburStart().getHour(), jamKerja.getJamLemburStart().getMinute()).toString());
        existedJamKerja.setJamLemburEnd(LocalTime.of(jamKerja.getJamLemburEnd().getHour(), jamKerja.getJamLemburEnd().getMinute()).toString());
        existedJamKerja.setJamDatangBatas(LocalTime.of(jamKerja.getJamDatangBatas().getHour(), jamKerja.getJamDatangBatas().getMinute()).toString());
        existedJamKerja.setJamPulangBatas(LocalTime.of(jamKerja.getJamPulangBatas().getHour(), jamKerja.getJamPulangBatas().getMinute()).toString());
        jamKerjaService.update(existedJamKerja);
    }

    @Operation(summary = "Mendapatkan jam kerja hari senin-kamis berdasarkan isRamadhan")
    @GetMapping("/senin-kamis")
    JamKerjaResponse getSeninkamis(@RequestParam("isRamadhan") Boolean isRamadhan) {
        return new JamKerjaResponse(jamKerjaService.findByHariAndIsRamadhan(1, isRamadhan));
    }

    @Operation(summary = "Mendapatkan jam kerja hari jumat berdasarkan isRamadhan")
    @GetMapping("/jumat")
    JamKerjaResponse getJumat(@RequestParam("isRamadhan") Boolean isRamadhan) {
        return new JamKerjaResponse(jamKerjaService.findByHariAndIsRamadhan(5, isRamadhan));
    }

}
