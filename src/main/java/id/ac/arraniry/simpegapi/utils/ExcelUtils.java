package id.ac.arraniry.simpegapi.utils;

import id.ac.arraniry.simpegapi.entity.PotonganUnitGaji;
import id.ac.arraniry.simpegapi.entity.PotonganUnitGajiItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.http.HttpStatus;
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
import java.util.Set;

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

        String rawValue;
        try {
            rawValue = formatter.formatCellValue(cell, evaluator);
        } catch (Exception ex) {
            // fallback ke string mentah (misal rumus eksternal)
            rawValue = cell.toString();
        }

        if (rawValue == null || rawValue.isEmpty()) return BigDecimal.ZERO;

        String cleaned = rawValue.replaceAll("[^0-9\\-]", "").trim();
        if (cleaned.isEmpty()) return BigDecimal.ZERO;

        try {
            return new BigDecimal(cleaned).setScale(0, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
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
    public static List<String> getPotonganHeaders(Sheet sheet, int start, int end, FormulaEvaluator evaluator) {
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
    public static List<PotonganUnitGaji> readPotonganExcel(MultipartFile file, String kodeAnakSatker,
                                                           Integer tahun, Integer bulan,
                                                           LocalDateTime now, String nama) {
        ExcelConfig config = getExcelConfig(kodeAnakSatker);
        List<PotonganUnitGaji> list = new ArrayList<>();

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

                PotonganUnitGaji potonganUnitGaji = new PotonganUnitGaji();
                potonganUnitGaji.setNama(getString(row, config.namaColumn, evaluator));
                potonganUnitGaji.setGajiBersih(getBigDecimal(row, config.gajiBersihColumn, evaluator));

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
                potonganUnitGaji.setThp(getBigDecimal(row, config.thpColumn, evaluator));
                potonganUnitGaji.setNip(getString(row, config.nipColumn, evaluator));

                potonganUnitGaji.setKodeAnakSatker(kodeAnakSatker);
                potonganUnitGaji.setTahun(tahun);
                potonganUnitGaji.setBulan(bulan);
                potonganUnitGaji.setCreatedBy(nama);
                potonganUnitGaji.setCreatedDate(now);

                list.add(potonganUnitGaji);
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
    private static ExcelConfig getExcelConfig(String kodeAnakSatker) {
        return switch (kodeAnakSatker) {
            case "01" -> // Biro
                    new ExcelConfig(1, 4, 5, 7, 1, 2, 4, 5, 17, 18, 19, 22, Set.of(7));
            case "02" -> // Syariah
                    new ExcelConfig(1, 3, 4, 6, 0, 1, 2, 3, 18, 19, 20, 24, Set.of());
            case "03" -> // Tarbiyah
                    new ExcelConfig(1, 2, 2, 5, 0, 1, 2, 3, 21, 22, 23, 24, Set.of());
            case "07" -> // Febi
                    new ExcelConfig(0, 4, 5, 6, 0, 1, 3, 4, 14, 15, 16, 20, Set.of(13));
            case "P1" -> // PPPK
                    new ExcelConfig(0, 4, 5, 7, 1, 2, 4, 5, 15, 16, 17, 20, Set.of());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Kode anak satker tidak didukung: " + kodeAnakSatker);
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
     * Inner class untuk konfigurasi Excel per unit
     */
    private static class ExcelConfig {
        final int sheetIndex;
        final int headerRow1;
        final int headerRow2;
        final int skipRows;
        final int noColumn;
        final int namaColumn;
        final int gajiBersihColumn;
        final int potonganStartCol;
        final int potonganEndCol;
        final int jumlahPotonganColumn;
        final int thpColumn;
        final int nipColumn;
        final Set<Integer> skipColumns;

        ExcelConfig(int sheetIndex, int headerRow1, int headerRow2, int skipRows,
                    int noColumn, int namaColumn, int gajiBersihColumn,
                    int potonganStartCol, int potonganEndCol,
                    int jumlahPotonganColumn, int thpColumn, int nipColumn,
                    Set<Integer> skipColumns) {
            this.sheetIndex = sheetIndex;
            this.headerRow1 = headerRow1;
            this.headerRow2 = headerRow2;
            this.skipRows = skipRows;
            this.noColumn = noColumn;
            this.namaColumn = namaColumn;
            this.gajiBersihColumn = gajiBersihColumn;
            this.potonganStartCol = potonganStartCol;
            this.potonganEndCol = potonganEndCol;
            this.jumlahPotonganColumn = jumlahPotonganColumn;
            this.thpColumn = thpColumn;
            this.nipColumn = nipColumn;
            this.skipColumns = skipColumns;
        }
    }
}
