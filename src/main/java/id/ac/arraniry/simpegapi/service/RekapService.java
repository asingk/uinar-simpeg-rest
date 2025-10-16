package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.RemunPegawaiVO;
import id.ac.arraniry.simpegapi.dto.UangMakanCreateRequest;
import id.ac.arraniry.simpegapi.dto.UangMakanPegawaiVO;
import id.ac.arraniry.simpegapi.entity.Rekap;
import net.sf.jasperreports.engine.JRException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public interface RekapService {
    Rekap save(Rekap rekap);
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitGaji(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId);
    Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitRemun(String jenisRekap, Integer tahun, Integer bulan, String unitRemunId);
    List<Rekap> findByJenisRekapAndTahun(String jenisRekap, Integer tahun);
    void processGajiFile(MultipartFile file, String createdBy);
    List<Rekap> findByJenisRekapAndTahunAndKodeAnakSatker(String jenisRekap, Integer tahun, String kodeAnakSatker);
    void processRekap(UangMakanCreateRequest request);
    SseEmitter streamGenerate(Integer tahun, Integer bulan, String unitGajiId, String jenisRekap, String unitRemunId);
    UangMakanPegawaiVO getUangMakanByNipDetail(String id);
    RemunPegawaiVO getRemunByNipDetail(String id);
    File generateFile(String jenisRekap, String fileType, String unitGaji, String unitRemun, Integer tahun, Integer bulan)
            throws JRException, FileNotFoundException;
    void processPotonganGajiFile(MultipartFile file, String createdBy, String kodeAnakSatker, Integer tahun, Integer bulan);
}
