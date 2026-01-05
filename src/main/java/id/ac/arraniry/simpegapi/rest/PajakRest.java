package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.PajakRequest;
import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import id.ac.arraniry.simpegapi.entity.Pajak;
import id.ac.arraniry.simpegapi.service.PajakService;
import id.ac.arraniry.simpegapi.utils.SimpegGraphUtils;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pajak")
@CrossOrigin
public class PajakRest {

    private final PajakService pajakService;
    private final Environment environment;

    public PajakRest(PajakService pajakService, Environment environment) {
        this.pajakService = pajakService;
        this.environment = environment;
    }

    @GetMapping
    public List<Pajak> getAll() {
        return pajakService.findAll();
    }

    @GetMapping("/{id}")
    public Pajak get(@PathVariable String id) {
        return pajakService.findById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @Valid @RequestBody PajakRequest request) {
        PegawaiSimpegVO pegawaiSimpegVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getUpdatedBy(), environment);
        Pajak pajak = pajakService.findById(id);
        pajak.setPersen(request.getPersen());
        pajak.setLastModifiedBy(pegawaiSimpegVO.getNama());
        pajak.setLastModifiedDate(LocalDateTime.now());
        pajakService.save(pajak);
    }

}
