package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

@Data
public class PegawaiSimpegVO {

	private String id;
	private String nama;
	private StatusAktifPegawaiVO statusAktif;
	private String jenisJabatan;

}
