package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LaporanKehadiranVO {

	private LocalDate tanggal;
	private LocalTime jamDatang;
	private LocalTime jamPulang;
	private String keteranganDatang;
	private String keteranganPulang;
	private Integer telatDatang;
	private Integer cepatPulang;

}
