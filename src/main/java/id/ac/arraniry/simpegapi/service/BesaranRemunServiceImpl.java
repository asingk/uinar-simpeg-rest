package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.BesaranRemun;
import id.ac.arraniry.simpegapi.repo.BesaranRemunRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BesaranRemunServiceImpl implements BesaranRemunService{
    private final BesaranRemunRepo repo;

    public BesaranRemunServiceImpl(BesaranRemunRepo repo) {
        this.repo = repo;
    }

    @Override
    public List<BesaranRemun> findAll() {
        return repo.findAll();
    }

    @Override
    public BesaranRemun findById(String id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public BesaranRemun save(BesaranRemun besaranRemun) {
        return repo.save(besaranRemun);
    }
}
