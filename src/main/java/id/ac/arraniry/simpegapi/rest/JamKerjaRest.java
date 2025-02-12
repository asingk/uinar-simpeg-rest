package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.JamKerjaResponse;
import id.ac.arraniry.simpegapi.service.JamKerjaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

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

}
