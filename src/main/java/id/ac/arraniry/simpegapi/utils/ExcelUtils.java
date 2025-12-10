package id.ac.arraniry.simpegapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.arraniry.simpegapi.dto.UnitGajiVO;
import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;
import id.ac.arraniry.simpegapi.entity.PotonganUnitGajiItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*;

public class ExcelUtils {
    private static final DataFormatter formatter = new DataFormatter();

    // getString yang memakai evaluator
    public static String getString(Row row, int colIndex, FormulaEvaluator evaluator) {
        if (row == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        // gunakan formatter dengan evaluator supaya formula dievaluasi
        return formatter.formatCellValue(cell, evaluator).trim();
    }

    // getBigDecimal yang memakai evaluator
    public static BigDecimal getBigDecimal(Row row, int colIndex, FormulaEvaluator evaluator) {
        if (row == null) return BigDecimal.ZERO;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return BigDecimal.ZERO;

        String rawValue = formatter.formatCellValue(cell, evaluator);

        if (rawValue == null || rawValue.isEmpty()) return BigDecimal.ZERO;

        String cleaned = rawValue.replaceAll("[^0-9\\-]", "").trim();
        if (cleaned.isEmpty()) return BigDecimal.ZERO;

        try {
            return new BigDecimal(cleaned).setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Fungsi untuk mendapatkan data Unit Gaji dari GraphQL
     */
    private static UnitGajiVO getUnitGajiFromGraphQL(String unitGajiId, Environment environment) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", environment.getProperty("env.data.secret-key"));
        
        String query = "{\"query\":\"query UnitGaji($unitGajiId: ID!) {" +
                "  unitGaji(id: $unitGajiId) {" +
                "    id" +
                "    nama" +
                "    kodeAnakSatker" +
                "}}\",\"variables\":{\"unitGajiId\":\"" + unitGajiId + "\"},\"operationName\":\"UnitGaji\"}";
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                Objects.requireNonNull(environment.getProperty("env.data.simpeg-graphql-url")), 
                new HttpEntity<>(query, headers), 
                String.class);
        
        if(200 != response.getStatusCode().value()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan mengakses GraphQL!");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(response.getBody());
            JsonNode unitGaji = actualObj.get("data").get("unitGaji");
            
            if(unitGaji == null || unitGaji.isNull()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Unit Gaji dengan ID " + unitGajiId + " tidak ditemukan!");
            }
            
            return mapper.readValue(unitGaji.toString(), UnitGajiVO.class);
        } catch (JsonProcessingException jpe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error parsing response GraphQL: " + jpe.getMessage());
        }
    }

    // helper: ambil nilai merged cell pakai evaluator
    private static String getMergedCellValue(Sheet sheet, int rowIndex, int colIndex, FormulaEvaluator evaluator) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowIndex, colIndex)) {
                Row firstRow = sheet.getRow(region.getFirstRow());
                if (firstRow != null) {
                    Cell firstCell = firstRow.getCell(region.getFirstColumn());
                    if (firstCell != null) {
                        return formatter.formatCellValue(firstCell, evaluator).trim();
                    }
                }
            }
        }

        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(colIndex);
            if (cell != null) return formatter.formatCellValue(cell, evaluator).trim();
        }
        return "";
    }

    // getPotonganHeaders yang memakai evaluator
    private static List<String> getPotonganHeaders(Sheet sheet, int start, int end, FormulaEvaluator evaluator) {
        Row rowStart = sheet.getRow(start);
        Row rowEnd = sheet.getRow(end);
        List<String> headers = new ArrayList<>();

        if (rowStart == null && rowEnd == null) return headers;

        int maxCols = Math.max(
                rowStart != null ? rowStart.getLastCellNum() : 0,
                rowEnd != null ? rowEnd.getLastCellNum() : 0
        );

        for (int i = 0; i < maxCols; i++) {
            String upper = getMergedCellValue(sheet, start, i, evaluator);
            String lower = getMergedCellValue(sheet, end, i, evaluator);

            String combined;
            if (!upper.isEmpty() && !lower.isEmpty()) {
                combined = upper.equalsIgnoreCase(lower) ? upper.trim() : (upper + " " + lower).trim();
            } else if (!upper.isEmpty()) {
                combined = upper.trim();
            } else {
                combined = lower.trim();
            }

            // bersihkan string (newline, tab, spasi ganda)
            combined = combined.replaceAll("[\\n\\r\\t]+", " ").replaceAll("\\s{2,}", " ").trim();
            headers.add(combined.isEmpty() ? "Kolom" + i : combined);
        }
        return headers;
    }

    /**
     * Membaca dan convert Excel file menjadi List PotonganUnitGaji
     * berdasarkan konfigurasi kode anak satker
     */
    public static List<PotonganUnitGaji> readPotonganExcel(MultipartFile file, String unitGajiId,
                                                           Integer tahun, Integer bulan,
                                                           LocalDateTime now, String nama,
                                                           Environment environment, String rekapId) {

        // Ambil data Unit Gaji dari GraphQL
        UnitGajiVO unitGajiVO = getUnitGajiFromGraphQL(unitGajiId, environment);

        ExcelConfig config = getExcelConfig(unitGajiId);
        List<PotonganUnitGaji> list = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(config.sheetIndex);
            List<String> headers = getPotonganHeaders(sheet, config.headerRow1, config.headerRow2, evaluator);

            Iterator<Row> rowIterator = sheet.iterator();
            int rowIndex = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowIndex++;

                // Lewati header
                if (rowIndex <= config.skipRows) continue;

                // Validasi kolom NO
                Cell noCell = row.getCell(config.noColumn);
                if (!isValidNoCell(noCell)) break;

                // Ambil nomor urut dari kolom NO
                String noUrut = getString(row, config.noColumn, evaluator);

                // Validasi field wajib
                String nip = getString(row, config.nipColumn, evaluator);
                BigDecimal gajiBersih = getBigDecimal(row, config.gajiBersihColumn, evaluator);
                BigDecimal thp = getBigDecimal(row, config.thpColumn, evaluator);

                List<String> rowErrors = new ArrayList<>();
                if (nip == null || nip.trim().isEmpty()) {
                    rowErrors.add("NIP kosong");
                }
                if (gajiBersih == null || gajiBersih.compareTo(BigDecimal.ZERO) == 0) {
                    rowErrors.add("Gaji Bersih kosong atau 0");
                }
                if (thp == null || thp.compareTo(BigDecimal.ZERO) == 0) {
                    rowErrors.add("THP kosong atau 0");
                }

                if (!rowErrors.isEmpty()) {
                    String errorMsg = "No. " + noUrut + ": " + String.join(", ", rowErrors);
                    errorMessages.add(errorMsg);
                    continue; // Skip baris ini, lanjut ke baris berikutnya
                }

                PotonganUnitGaji potonganUnitGaji = new PotonganUnitGaji();
                potonganUnitGaji.setNip(nip);
                potonganUnitGaji.setNama(getString(row, config.namaColumn, evaluator));
                potonganUnitGaji.setGajiBersih(gajiBersih);

                // Potongan: kolom dinamis
                List<PotonganUnitGajiItem> potonganList = new ArrayList<>();
                for (int c = config.potonganStartCol; c <= config.potonganEndCol; c++) {
                    if (config.skipColumns.contains(c)) continue;
                    String namaKolom = headers.size() > c ? headers.get(c) : "Kolom" + c;
                    BigDecimal nilai = getBigDecimal(row, c, evaluator);
                    potonganList.add(new PotonganUnitGajiItem(namaKolom, nilai));
                }
                potonganUnitGaji.setPotongan(potonganList);

                // Jumlah Potongan dan THP
                potonganUnitGaji.setJumlahPotongan(getBigDecimal(row, config.jumlahPotonganColumn, evaluator));
                potonganUnitGaji.setThp(thp);

                potonganUnitGaji.setUnitGajiId(unitGajiId);
                potonganUnitGaji.setUnitGajiNama(unitGajiVO.getNama()); // Simpan nama dari GraphQL
                potonganUnitGaji.setTahun(tahun);
                potonganUnitGaji.setBulan(bulan);
                potonganUnitGaji.setCreatedBy(nama);
                potonganUnitGaji.setCreatedDate(now);
                potonganUnitGaji.setIsNipExist(isPegawaiExistInSimpeg(potonganUnitGaji.getNip(), environment));
                potonganUnitGaji.setRekapId(rekapId);

                list.add(potonganUnitGaji);
            }

            // Jika ada error, throw exception dengan semua error message
            if (!errorMessages.isEmpty()) {
                String allErrors = String.join("; ", errorMessages);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Validasi gagal: " + allErrors);
            }

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error membaca file XLSX: " + e.getMessage());
        }
        return list;
    }

    /**
     * Mendapatkan konfigurasi Excel berdasarkan kode anak satker
     */
    private static ExcelConfig getExcelConfig(String unitGajiId) {
        return switch (unitGajiId) {
            case "BAUPK" -> // Biro
                    new ExcelConfig(1, 4, 5, 7, 1, 2, 4, 5, 17, 18, 19, 22, Set.of(7));
            case "FSH" -> // Syariah
                    new ExcelConfig(1, 3, 4, 6, 0, 1, 2, 3, 18, 19, 20, 24, Set.of());
            case "FTK" -> // Tarbiyah
                    new ExcelConfig(1, 2, 2, 5, 0, 1, 2, 3, 21, 22, 23, 24, Set.of());
            case "FUF" -> // Ushuluddin
                    new ExcelConfig(1, 3, 4, 6, 0, 2, 3, 4, 19, 20, 21, 1, Set.of());
            case "FAH" -> // Adab
                    new ExcelConfig(0, 3, 3, 6, 0, 2, 5, 6, 24, 25, 26, 1, Set.of());
            case "FDK" -> // Dakwah
                    new ExcelConfig(9, 5, 6, 7, 0, 2, 3, 4, 27, 28, 29, 1, Set.of());
            case "FEBI" -> // Febi
                    new ExcelConfig(0, 4, 5, 6, 0, 1, 3, 4, 14, 15, 16, 20, Set.of(13));
            case "FST" -> // Sains
                    new ExcelConfig(0, 4, 5, 6, 0, 2, 3, 4, 19, 21, 22, 1, Set.of());
            case "FPSI" -> // Psikologi
                    new ExcelConfig(0, 4, 5, 6, 0, 2, 4, 5, 21, 22, 23, 1, Set.of());
            case "FISIP" -> // Psikologi
                    new ExcelConfig(0, 4, 5, 6, 0, 2, 4, 5, 18, 19, 20, 1, Set.of());
            case "P3K" -> // PPPK
                    new ExcelConfig(0, 4, 5, 7, 1, 2, 4, 5, 15, 16, 17, 20, Set.of());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Kode anak satker tidak didukung: " + unitGajiId);
        };
    }

    /**
     * Validasi apakah cell NO valid (numeric atau formula yang menghasilkan numeric)
     */
    private static boolean isValidNoCell(Cell noCell) {
        if (noCell == null) return false;
        CellType type = noCell.getCellType();
        return type == CellType.NUMERIC ||
                (type == CellType.FORMULA && noCell.getCachedFormulaResultType() == CellType.NUMERIC);
    }

    /**
         * Record class untuk konfigurasi Excel per unit
         */
        private record ExcelConfig(int sheetIndex, int headerRow1, int headerRow2, int skipRows, int noColumn,
                                   int namaColumn, int gajiBersihColumn, int potonganStartCol, int potonganEndCol,
                                   int jumlahPotonganColumn, int thpColumn, int nipColumn, Set<Integer> skipColumns) {
    }

    private static boolean isPegawaiExistInSimpeg(String idPegawai, Environment environment) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", environment.getProperty("env.data.secret-key"));

        String query = "{\"query\":\"query Pegawai($id: ID!) {" +
                "  pegawai(id: $id) {" +
                "    id" +
                "}}\",\"variables\":{\"id\":\"" + idPegawai + "\"},\"operationName\":\"Pegawai\"}";

        ResponseEntity<String> response = restTemplate.postForEntity(
                Objects.requireNonNull(environment.getProperty("env.data.simpeg-graphql-url")),
                new HttpEntity<>(query, headers),
                String.class);

        if(200 != response.getStatusCode().value()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan mengakses GraphQL!");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(response.getBody());
            JsonNode pegawai = actualObj.get("data").get("pegawai");

            return pegawai != null && !pegawai.isNull();
        } catch (JsonProcessingException jpe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error parsing response GraphQL: " + jpe.getMessage());
        }
    }
    
}
