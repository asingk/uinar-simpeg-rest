package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.UangMakan;
import id.ac.arraniry.simpegapi.repo.UangMakanRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UangMakanServiceImpl implements UangMakanService {

    private final UangMakanRepo uangMakanRepo;

    public UangMakanServiceImpl(UangMakanRepo uangMakanRepo) {
        this.uangMakanRepo = uangMakanRepo;
    }

    @Override
    public List<UangMakan> findAll() {
        return uangMakanRepo.findAll();
    }

    @Override
    public UangMakan findById(String id) {
        return uangMakanRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uang makan tidak ditemukan!"));
    }

    @Override
    public UangMakan save(UangMakan uangMakan) {
        return uangMakanRepo.save(uangMakan);
    }

}
