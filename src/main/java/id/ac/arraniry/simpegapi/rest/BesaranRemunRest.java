package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.BesaranRemunRequest;
import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import id.ac.arraniry.simpegapi.entity.BesaranRemun;
import id.ac.arraniry.simpegapi.service.BesaranRemunService;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/implementasi-remun")
@CrossOrigin
public class BesaranRemunRest {

    private final BesaranRemunService besaranRemunService;
    private final KehadiranUtils kehadiranUtils;

    public BesaranRemunRest(BesaranRemunService besaranRemunService, KehadiranUtils kehadiranUtils) {
        this.besaranRemunService = besaranRemunService;
        this.kehadiranUtils = kehadiranUtils;
    }

    @GetMapping
    public List<BesaranRemun> getAll() {
        return besaranRemunService.findAll();
    }

    @GetMapping("/{id}")
    public BesaranRemun getById(@PathVariable String id) {
        return besaranRemunService.findById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @Valid @RequestBody BesaranRemunRequest request) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(request.getUpdatedBy());
        BesaranRemun besaranRemun = besaranRemunService.findById(id);
        besaranRemun.setPersen(request.getPersen());
        besaranRemun.setLastModifiedBy(pegawaiSimpegVO.getNama());
        besaranRemun.setLastModifiedDate(LocalDateTime.now());
        besaranRemunService.save(besaranRemun);
    }
}
