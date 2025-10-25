package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;
import id.ac.arraniry.simpegapi.repo.PotonganUnitGajiRepo;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PotonganUnitGajiServiceImpl implements PotonganUnitGajiService{

    private final PotonganUnitGajiRepo potonganUnitGajiRepo;

    public PotonganUnitGajiServiceImpl(PotonganUnitGajiRepo potonganUnitGajiRepo) {
        this.potonganUnitGajiRepo = potonganUnitGajiRepo;
    }

    @Override
    public List<PotonganUnitGaji> saveAll(List<PotonganUnitGaji> gajiList) {
        return potonganUnitGajiRepo.saveAll(gajiList);
    }

    @Override
    public void deleteByBulanAndTahunAndUnitGajiId(Integer bulan, Integer tahun, String unitGajiId) {
        potonganUnitGajiRepo.deleteByBulanAndTahunAndUnitGajiId(bulan, tahun, unitGajiId);
    }

    @Override
    public List<PotonganUnitGaji> findPotonganGajiPegawai(String nip, Integer tahun) {
        return potonganUnitGajiRepo.findByNipAndTahun(nip, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
    }

    @Override
    public PotonganUnitGaji findById(String id) {
        return potonganUnitGajiRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
