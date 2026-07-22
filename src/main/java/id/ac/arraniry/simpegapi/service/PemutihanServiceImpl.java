package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import id.ac.arraniry.simpegapi.dto.PemutihanCreateRequest;
import id.ac.arraniry.simpegapi.entity.Pemutihan;
import id.ac.arraniry.simpegapi.repo.PemutihanRepository;
import id.ac.arraniry.simpegapi.utils.SimpegGraphUtils;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PemutihanServiceImpl implements PemutihanService {

    private final PemutihanRepository pemutihanRepo;
    private final Environment environment;

    public PemutihanServiceImpl(PemutihanRepository pemutihanRepo, Environment environment) {
        this.pemutihanRepo = pemutihanRepo;
        this.environment = environment;
    }

    @Override
    public Pemutihan findByDateStringAndStatus(LocalDate tanggal, String status) {
        return pemutihanRepo.findByDateStringAndStatus(tanggal.toString(), status);
    }

    @Override
    public List<Pemutihan> findByBulanAndTahun(Integer bulan, Integer tahun) {
//        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
//        YearMonth yearMonth = YearMonth.of(tahun, bulan);
//        LocalDate endDate = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        String pattern = String.format("%d-%02d-", tahun, bulan);
        return pemutihanRepo.findBulanan(pattern, Sort.by(Sort.Direction.ASC, "dateString"));
    }

    @Override
    public List<Pemutihan> findByBulanAndTahunAndStatus(Integer bulan, Integer tahun, String status) {
        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate endDate = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        return pemutihanRepo.findByTanggalBetweenAndStatus(startDate, endDate, status, Sort.by(Sort.Direction.ASC, "tanggal"));
    }

    @Override
    public String create(PemutihanCreateRequest request) {
        PegawaiSimpegVO pegawaiSimpegVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getCreatedBy(), environment);
        try {
            return pemutihanRepo.save(new Pemutihan(request, pegawaiSimpegVO.getNama())).getId();
        } catch (DuplicateKeyException dke) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "tanggal sudah ada!");
        }
    }

    @Override
    public void delete(String id) {
        pemutihanRepo.deleteById(id);
    }

}
