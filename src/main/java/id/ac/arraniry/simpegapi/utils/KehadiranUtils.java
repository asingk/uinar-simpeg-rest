package id.ac.arraniry.simpegapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import id.ac.arraniry.simpegapi.dto.StatusPegawaiSimpegVO;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

@Component
public class KehadiranUtils {

    private static final Logger log = LoggerFactory.getLogger(KehadiranUtils.class);
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;

    private final HariLiburTapiKerjaService hariLiburTapiKerjaService;
    private final HariLiburService hariLiburService;
    private final JamKerjaService jamKerjaService;
    private final HijriahService hijriahService;
    private final Environment environment;
    private final KehadiranService kehadiranService;

    public KehadiranUtils(HariLiburTapiKerjaService hariLiburTapiKerjaService, HariLiburService hariLiburService, JamKerjaService jamKerjaService,
                          HijriahService hijriahService, Environment environment, KehadiranService kehadiranService) {
        this.hariLiburTapiKerjaService = hariLiburTapiKerjaService;
        this.hariLiburService = hariLiburService;
        this.jamKerjaService = jamKerjaService;
        this.hijriahService = hijriahService;
        this.environment = environment;
        this.kehadiranService = kehadiranService;
    }

    public boolean isLibur(LocalDate tanggal) {

        List<HariLiburTapiKerja> hariLiburTapiKerjaList = hariLiburTapiKerjaService.findAll();
        for(HariLiburTapiKerja hari : hariLiburTapiKerjaList) {
            if(tanggal.isEqual(hari.getTanggal())) {
                return false;
            }
        }
        if(tanggal.getDayOfWeek().getValue() > 5)
            return true;
        List<HariLibur> hariLiburList = hariLiburService.findAll();
        for(HariLibur hari : hariLiburList) {
            if(tanggal.isEqual(hari.getTanggal())) {
                return true;
            }
        }

        return false;
    }

    public boolean getStatusLembur(LocalDateTime now) {

//		log.debug("getStatusLembur start " + System.currentTimeMillis());
        LocalTime nowTime = now.toLocalTime();
        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(now.getDayOfWeek().getValue(),
                hijriahService.isRamadhan(HijrahDate.now().get(ChronoField.YEAR), now.toLocalDate()));
        LocalTime jamLemburAwal = LocalTime.parse(jamKerja.getJamLemburStart());
        LocalTime jamLemburAkhir = LocalTime.parse(jamKerja.getJamLemburEnd());
        LocalTime jamMasukAwal = LocalTime.parse(jamKerja.getJamDatangStart());
        LocalTime jamPulangAkhir = LocalTime.parse(jamKerja.getJamPulangEnd());
        boolean isLembur = false;
        // jam lembur hari libur
        if(isLibur(now.toLocalDate()) && nowTime.isAfter(jamMasukAwal.minusSeconds(1)) && nowTime.isBefore(jamPulangAkhir.plusSeconds(1))) {
            isLembur = true;
        }
        // jam lembur hari kerja
        else if(!isLibur(now.toLocalDate()) && nowTime.isAfter(jamLemburAwal.minusSeconds(1)) && nowTime.isBefore(jamLemburAkhir.plusSeconds(1))) {
            isLembur = true;
        }

//		log.debug("getStatusLembur stop " + System.currentTimeMillis());

        return isLembur;
    }

    public PegawaiSimpegVO getProfilPegawaiFromSimpegGraphql(String idPegawai) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", environment.getProperty("env.data.secret-key"));
        String query = "{\"query\":\"query Pegawai($id: ID!) {" +
                "  pegawai(id: $id) {" +
                "    id" +
                "    nama" +
                "  statusAktif{" +
                "    id" +
                "  }" +
                "  jenisJabatan" +
                "}}\",\"variables\":{\"id\":\"" + idPegawai + "\"},\"operationName\":\"Pegawai\"}";
        ResponseEntity<String> response = restTemplate.postForEntity(Objects.requireNonNull(environment.getProperty("env.data.simpeg-graphql-url")), new HttpEntity<>(query, headers), String.class);
        if(200 != response.getStatusCode().value()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan!");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
//            log.debug(response.getBody());
            JsonNode actualObj = mapper.readTree(response.getBody());
            JsonNode pegawai = actualObj.get("data").get("pegawai");
            if(pegawai.isNull())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai " + idPegawai + " tidak ditemukan!");
            PegawaiSimpegVO pegawaiProfilVO = new ObjectMapper().readValue(pegawai.toString(), PegawaiSimpegVO.class);
//            log.debug(pegawaiProfilVO.toString());
            if(pegawaiProfilVO.getStatusAktif().getId() > 1)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pegawai tidak aktif!");
            if(null == pegawaiProfilVO.getJenisJabatan())
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak punya jenis jabatan!");
            return pegawaiProfilVO;
        } catch (JsonProcessingException jpe) {
            log.error(jpe.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan!");
        }
    }

    public void uploadFile(String filename, InputStream inputStream, String cdnSubfolder) {
        log.debug("---uploadFile start---");
        String remoteDir = environment.getProperty("env.data.cdn-home-folder") + cdnSubfolder + File.separator;
        try {
            ChannelSftp channelSftp = setupJsch();
            channelSftp.connect(CHANNEL_TIMEOUT);
            channelSftp.put(inputStream, remoteDir + filename);
            channelSftp.exit();
        } catch (JSchException | SftpException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ChannelSftp setupJsch() throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts("/home/asingk/.ssh/known_hosts");
        Session jschSession = jsch.getSession(environment.getProperty("env.data.cdn-username"), environment.getProperty("env.data.cdn-host"));
        jschSession.setPassword(environment.getProperty("env.data.cdn-password"));
        jschSession.connect(SESSION_TIMEOUT);
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public void undeleteKehadiran(LocalDate tanggal, String nip) {
        List<KehadiranVO> hadir = kehadiranService.findByNipAndTanggal(nip, tanggal);
        for(KehadiranVO row: hadir) {
            row.setIsDeleted(false);
            kehadiranService.save(row);
        }
    }

    public String namaBulan(int bulan) {
        return switch (bulan) {
            case 1 -> "Januari";
            case 2 -> "Februari";
            case 3 -> "Maret";
            case 4 -> "April";
            case 5 -> "Mei";
            case 6 -> "Juni";
            case 7 -> "Juli";
            case 8 -> "Agustus";
            case 9 -> "September";
            case 10 -> "Oktober";
            case 11 -> "November";
            case 12 -> "Desember";
            default -> "salah bulan";
        };
    }

    public void safeDeleteIzin(LocalDate tanggal, String nip, PegawaiSimpegVO pegawaiSimpegVO) {
        List<KehadiranVO> hadir = kehadiranService.findByNipAndTanggal(nip, tanggal);
        for(KehadiranVO row: hadir) {
            row.setIsDeleted(true);
            row.setDeletedByNip(pegawaiSimpegVO.getId());
            row.setDeletedByNama(pegawaiSimpegVO.getNama());
            row.setDeletedDate(LocalDateTime.now());
            kehadiranService.save(row);
        }
    }

    public StatusPegawaiSimpegVO getNamaStatusPegawaiByIdFromSimpegGraphql(Integer id) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", environment.getProperty("env.data.secret-key"));
        String query = "{\"query\":\"query StatusPegawai($id: Int!) {" +
                "  statusPegawai(id: $id) {" +
                "    id" +
                "    nama" +
                "  }" +
                "}\",\"variables\":{\"id\":" + id + "},\"operationName\":\"StatusPegawai\"}";
        ResponseEntity<String> response = restTemplate.postForEntity(Objects.requireNonNull(environment.getProperty("env.data.simpeg-graphql-url")), new HttpEntity<>(query, headers), String.class);
        if(200 != response.getStatusCode().value()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan!");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
//            log.debug(response.getBody());
            JsonNode actualObj = mapper.readTree(response.getBody());
            JsonNode statusPegawai = actualObj.get("data").get("statusPegawai");
            if(statusPegawai.isNull())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Status pegawai tidak ditemukan!");
            //            log.debug(pegawaiProfilVO.toString());
            return new ObjectMapper().readValue(statusPegawai.toString(), StatusPegawaiSimpegVO.class);
        } catch (JsonProcessingException jpe) {
            log.error(jpe.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan!");
        }
    }

}
