package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.dto.WfaTanggalRequest;
import id.ac.arraniry.simpegapi.entity.WfaByTanggal;
import id.ac.arraniry.simpegapi.service.WfaByTanggalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/wfa-tanggal")
public class WfaTanggalRest {
    private final WfaByTanggalService wfaByTanggalService;

    public WfaTanggalRest(WfaByTanggalService wfaByTanggalService) {
        this.wfaByTanggalService = wfaByTanggalService;
    }

    @GetMapping
    public List<WfaByTanggal> getByTahun(@RequestParam Integer tahun) {
        return wfaByTanggalService.findByTahun(tahun);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResponse create(@Valid @RequestBody WfaTanggalRequest request) {
        var response = new CreateResponse();
        response.setId(wfaByTanggalService.create(request.getTanggal()));
        return response;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable String id) {
        wfaByTanggalService.deleteById(id);
    }

}
