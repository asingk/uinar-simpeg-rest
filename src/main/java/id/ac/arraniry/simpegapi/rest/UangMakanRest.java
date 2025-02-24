package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import id.ac.arraniry.simpegapi.dto.UangMakanRequest;
import id.ac.arraniry.simpegapi.entity.UangMakan;
import id.ac.arraniry.simpegapi.service.UangMakanService;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/uang-makan")
@CrossOrigin
public class UangMakanRest {

    private final UangMakanService uangMakanService;
    private final KehadiranUtils kehadiranUtils;

    public UangMakanRest(UangMakanService uangMakanService, KehadiranUtils kehadiranUtils) {
        this.uangMakanService = uangMakanService;
        this.kehadiranUtils = kehadiranUtils;
    }

    @GetMapping
    public List<UangMakan> getAll() {
        return uangMakanService.findAll();
    }

    @GetMapping("/{id}")
    public UangMakan get(@PathVariable String id) {
        return uangMakanService.findById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @Valid @RequestBody UangMakanRequest request) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(request.getUpdatedBy());
        UangMakan uangMakan = uangMakanService.findById(id);
        uangMakan.setJumlah(request.getJumlah());
        uangMakan.setLastModifiedDate(LocalDateTime.now());
        uangMakan.setLastModifiedBy(pegawaiSimpegVO.getNama());
        uangMakanService.save(uangMakan);
    }

}
