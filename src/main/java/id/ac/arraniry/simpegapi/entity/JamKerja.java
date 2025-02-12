package id.ac.arraniry.simpegapi.entity;

import lombok.Data;

@Data
public class JamKerja {
	
	private String id;
	private Integer hari;
	private String jamDatangStart;
	private String jamDatangEnd;
	private String jamPulangStart;
	private String jamPulangEnd;
	private String jamLemburStart;
	private String jamLemburEnd;
	private Boolean isRamadhan;
	private String jamDatangBatas;
	private String jamPulangBatas;

}
