package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;
import id.ac.arraniry.simpegapi.repo.PotonganUnitGajiRepo;
import org.springframework.stereotype.Service;

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
    public void deleteByBulanAndTahunAndKodeAnakSatker(Integer bulan, Integer tahun, String kodeUnitGaji) {
        potonganUnitGajiRepo.deleteByBulanAndTahunAndKodeAnakSatker(bulan, tahun, kodeUnitGaji);
    }
}
