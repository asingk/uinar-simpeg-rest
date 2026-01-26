package id.ac.arraniry.simpegapi.entity;

import lombok.Data;

@Data
public class JamKerjaHarian {
	
	private String id;
	private String datangStart;
	private String datangEnd;
	private String pulangStart;
	private String pulangEnd;
	private String lemburStart;
	private String lemburEnd;
	private String jadwalDatang;
	private String jadwalPulang;
	private String dateString;

}
