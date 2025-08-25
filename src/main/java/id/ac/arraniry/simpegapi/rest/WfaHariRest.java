package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.WfaByHari;
import id.ac.arraniry.simpegapi.service.WfaByHariService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/wfa-hari")
public class WfaHariRest {
    private final WfaByHariService wfaByHariService;

    public WfaHariRest(WfaByHariService wfaByHariService) {
        this.wfaByHariService = wfaByHariService;
    }

    @GetMapping
    public List<WfaByHari> findAll() {
        return wfaByHariService.findAll();
    }

    @PutMapping("/{id}")
    void update(@PathVariable String id, @RequestParam Boolean wfa) {
        wfaByHariService.update(id, wfa);
    }

}
