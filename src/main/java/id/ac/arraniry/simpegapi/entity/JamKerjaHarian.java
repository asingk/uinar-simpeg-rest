package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class JamKerjaHarian {
	
	private String id;
	private LocalDate tanggal;
	private String datangStart;
	private String datangEnd;
	private String pulangStart;
	private String pulangEnd;
	private String lemburStart;
	private String lemburEnd;
	private String jadwalDatang;
	private String jadwalPulang;
	private String dateString;

	public JamKerjaHarian(JamKerja jamKerja, LocalDate tanggal) {
		this.tanggal = tanggal;
		this.datangStart = jamKerja.getJamDatangStart();
		this.datangEnd = jamKerja.getJamDatangEnd();
		this.pulangStart = jamKerja.getJamPulangStart();
		this.pulangEnd = jamKerja.getJamPulangEnd();
		this.lemburStart = jamKerja.getJamLemburStart();
		this.lemburEnd = jamKerja.getJamLemburEnd();
		this.jadwalDatang = jamKerja.getJamDatangBatas();
		this.jadwalPulang = jamKerja.getJamPulangBatas();
		this.dateString = tanggal.toString();
	}

}
