package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Gaji;
import id.ac.arraniry.simpegapi.repo.GajiRepo;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GajiServiceImpl implements GajiService {
    private final GajiRepo gajiRepo;

    public GajiServiceImpl(GajiRepo gajiRepo) {
        this.gajiRepo = gajiRepo;
    }

    @Override
    public List<Gaji> saveAll(List<Gaji> gajiList) {
        return gajiRepo.saveAll(gajiList);
    }

    @Override
    public void deleteByBulanAndTahunAndKodeAnakSatker(Integer bulan, Integer tahun, String kodeUnitGaji) {
        gajiRepo.deleteByBulanAndTahunAndKodeAnakSatker(bulan, tahun, kodeUnitGaji);
    }

    @Override
    public List<Gaji> findByNipAndTahun(String nip, Integer tahun) {
        return gajiRepo.findByNipAndTahun(nip, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
    }

    @Override
    public Gaji findById(String id) {
        return gajiRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
