package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusSaatIniResponse {
	
	private String status;
	private LocalDateTime waktu;
	private boolean jamLembur;
	private boolean isRamadhan;

}
