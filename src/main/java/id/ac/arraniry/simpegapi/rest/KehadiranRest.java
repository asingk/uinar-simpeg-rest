package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.helper.GlobalConstants;
import id.ac.arraniry.simpegapi.helper.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin
@RequestMapping("/kehadiran")
public class KehadiranRest {

    private static final Logger logger = LoggerFactory.getLogger(KehadiranRest.class);

    private final HijriahService hijriahService;
    private final JamKerjaService jamKerjaService;
    private final KehadiranUtils kehadiranUtils;
    private final PemutihanService pemutihanService;
    private final IzinService izinService;
    private final KehadiranService kehadiranService;
    private final JenisJabatanService jenisJabatanService;

    public KehadiranRest(HijriahService hijriahService, JamKerjaService jamKerjaService, KehadiranUtils kehadiranUtils, PemutihanService pemutihanService,
                         IzinService izinService, KehadiranService kehadiranService, JenisJabatanService jenisJabatanService) {
        this.hijriahService = hijriahService;
        this.jamKerjaService = jamKerjaService;
        this.kehadiranUtils = kehadiranUtils;
        this.pemutihanService = pemutihanService;
        this.izinService = izinService;
        this.kehadiranService = kehadiranService;
        this.jenisJabatanService = jenisJabatanService;
    }

    @Operation(summary = "mendapatkan status waktu di server saat ini")
    @GetMapping("/status-saat-ini")
    public StatusSaatIniResponse getStatusSaatIni() {
//		log.debug("getStatusSaatIni start " + System.currentTimeMillis());
        StatusSaatIniResponse status = new StatusSaatIniResponse();
        //TODO dev purpose only
//		LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 12, 30), LocalTime.of(17, 15, 0));
        LocalDateTime now = LocalDateTime.now();
        boolean isRamadhan = hijriahService.isRamadhan(HijrahDate.now().get(ChronoField.YEAR), now.toLocalDate());
        status.setWaktu(now);
        status.setStatus(getstatusJamKerja(now, isRamadhan));
        status.setJamLembur(kehadiranUtils.getStatusLembur(now));
        status.setRamadhan(isRamadhan);
//		log.debug("getStatusSaatIni stop " + System.currentTimeMillis());
        return status;
    }

    private String getstatusJamKerja(LocalDateTime now, boolean isRamadhan) {
//		log.debug("getstatusJamKerja start " + System.currentTimeMillis());
        String status;
        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(now.getDayOfWeek().getValue(),
                isRamadhan);
        LocalTime jamMasukAwal = LocalTime.parse(jamKerja.getJamDatangStart());
        LocalTime jamMasukAkhir = LocalTime.parse(jamKerja.getJamDatangEnd());
        LocalTime jamPulangAwal = LocalTime.parse(jamKerja.getJamPulangStart());
        LocalTime jamPulangAkhir = LocalTime.parse(jamKerja.getJamPulangEnd());
        LocalTime nowTime = now.toLocalTime();
        if(kehadiranUtils.isLibur(now.toLocalDate())) {
            status = GlobalConstants.STATUS_LIBUR;
        } else {
            if(!nowTime.isBefore(jamMasukAwal) && nowTime.isBefore(jamMasukAkhir)) {	// absen datang
                if (null != pemutihanService.findByTanggalAndStatus(now.toLocalDate(), GlobalConstants.STATUS_DATANG)) {
                    status = GlobalConstants.STATUS_PEMUTIHAN;
                } else {
                    status = GlobalConstants.STATUS_DATANG;
                }
            } else if(!nowTime.isBefore(jamPulangAwal) && nowTime.isBefore(jamPulangAkhir)) {	// absen pulang
                if (null != pemutihanService.findByTanggalAndStatus(now.toLocalDate(), GlobalConstants.STATUS_PULANG)) {
                    status = GlobalConstants.STATUS_PEMUTIHAN;
                } else {
                    status = GlobalConstants.STATUS_PULANG;
                }
            }
            else {	// di luar jam absen
                status = GlobalConstants.STATUS_TUTUP;
            }
        }
//		log.debug("getstatusJamKerja stop " + System.currentTimeMillis());
        return status;
    }

    @Operation(summary = "Merekam kehadiran pengguna saat ini")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaveResponse save(
            HttpServletRequest request, @RequestHeader("User-Agent") String userAgent, @Valid @RequestBody KehadiranCreateRequest createRequest) {
//		log.debug("--- save start ---");
//		log.debug("nip: " + nip);
        String trimmedNip = createRequest.getIdPegawai();

        //TODO dev purpose only
//		String nip = "198703222019031010";
//		LocalDateTime now = LocalDateTime.of(LocalDate.of(2023, 9, 12), LocalTime.of(17, 15));
        LocalDateTime now = LocalDateTime.now();
        var statusSaatIni = getStatusSaatIni();
        if (!(statusSaatIni.getStatus().equals(GlobalConstants.STATUS_DATANG) || statusSaatIni.getStatus().equals(GlobalConstants.STATUS_PULANG))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, statusSaatIni.getStatus());
        }
        Optional<Izin> izinOptional = izinService.findByNipAndTanggal(createRequest.getIdPegawai(), now.toLocalDate());
        if (izinOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hari ini Anda " + izinOptional.get().getIzinCategoryDesc());
        }
        PegawaiSimpegVO pegawaiProfilVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(trimmedNip);
        Kehadiran kehadiran = paramToSave(now, createRequest.getLongitude(), createRequest.getLatitude(), userAgent);
        if (null == createRequest.getLongitude() && null == createRequest.getLatitude() && isHarusDiKampus(pegawaiProfilVO)) {
            validasiIpPrivate(request.getRemoteAddr());
        }
        kehadiran.setPegawai(new Pegawai(pegawaiProfilVO.getId(), pegawaiProfilVO.getNama()));
        var existingOpt = kehadiranService.findByNipAndStatusAndTanggal(trimmedNip, kehadiran.getStatus(), now.toLocalDate());
        if (existingOpt.isPresent()) {
            if(kehadiran.getStatus().equals(GlobalConstants.STATUS_DATANG)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anda sudah datang!");
            }
            kehadiran.setId(existingOpt.get().getId());
        }

//		log.debug("--- save end ---");
        return kehadiranService.save(new KehadiranVO(kehadiran));
    }

    private Kehadiran paramToSave(LocalDateTime now, Double longitude, Double latitude, String userAgent) {
        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(now.getDayOfWeek().getValue(),
                hijriahService.isRamadhan(HijrahDate.now().get(ChronoField.YEAR), now.toLocalDate()));
        LocalTime jamMasukAwal = LocalTime.parse(jamKerja.getJamDatangStart());
        LocalTime jamMasukAkhir = LocalTime.parse(jamKerja.getJamDatangEnd());
        LocalTime jamPulangAwal = LocalTime.parse(jamKerja.getJamPulangStart());
        LocalTime jamPulangAkhir = LocalTime.parse(jamKerja.getJamPulangEnd());
        LocalTime nowTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();
        Kehadiran kehadiran = new Kehadiran();
        kehadiran.setWaktu(now);

        if(nowTime.isAfter(jamMasukAwal.minusSeconds(1)) && nowTime.isBefore(jamMasukAkhir.plusSeconds(1))) {
            kehadiran.setStatus(GlobalConstants.STATUS_DATANG);
        }else if(nowTime.isAfter(jamPulangAwal.minusSeconds(1)) && nowTime.isBefore(jamPulangAkhir.plusSeconds(1))) {
            kehadiran.setStatus(GlobalConstants.STATUS_PULANG);
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Saat ini di luar jam absen!");
        }
        kehadiran.setKurangMenit(kurangMenit(kehadiran.getStatus(), now.toLocalDate(), now.toLocalTime()));
        if(null != longitude && null != latitude) {
            Location location = new Location();
            location.setType(GlobalConstants.LOCATION_TYPE_POINT);
            List<Double> coordinates = new ArrayList<>();
            coordinates.add(longitude);
            coordinates.add(latitude);
            location.setCoordinates(coordinates);
            kehadiran.setLocation(location);
        }
        kehadiran.setUserAgent(userAgent);
        kehadiran.setTanggal(today.toString());
        return kehadiran;
    }

    private int kurangMenit(String status, LocalDate tanggal, LocalTime jam) {
        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(tanggal.getDayOfWeek().getValue(),
                hijriahService.isRamadhan(HijrahDate.from(tanggal).get(ChronoField.YEAR), tanggal));
        LocalTime jamDatang = LocalTime.parse(jamKerja.getJamDatangBatas());
        LocalTime jamPulang = LocalTime.parse(jamKerja.getJamPulangBatas());

        int kurang;
        long kurangLong;
        if(GlobalConstants.STATUS_DATANG.equals(status)) {
            kurangLong = Duration.between(jamDatang, LocalTime.of(jam.getHour(), jam.getMinute())).toMinutes();
        } else {
            kurangLong = Duration.between(LocalTime.of(jam.getHour(), jam.getMinute()), jamPulang).toMinutes();
        }
        kurang = Math.toIntExact(kurangLong);
        return Math.max(kurang, 0);
    }

    private boolean isHarusDiKampus(PegawaiSimpegVO pegawaiProfilVO) {
        return jenisJabatanService.findById(pegawaiProfilVO.getJenisJabatan()).getIsHarusDiKampus();
    }

    private void validasiIpPrivate(String ip) {
        Pattern pattern = Pattern.compile("(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)");
        Matcher matcher = pattern.matcher(ip);
        if(!matcher.find()){
            logger.trace("{} harus menggunakan jaringan kampus!", ip);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Harus menggunakan jaringan kampus!");
        }
        logger.trace("{} sukses menggukanan jaringan kampus", ip);
    }

}
