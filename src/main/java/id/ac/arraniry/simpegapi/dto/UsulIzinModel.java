package id.ac.arraniry.simpegapi.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class UsulIzinModel extends RepresentationModel<UsulIzinModel> {
    private String id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String nip;
    private String nama;
    private String izinCategoryId;
    private String izinCategoryDesc;
    private Integer status;
    private String fileName;
    private String file;
    private String ket;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private LocalDateTime createdDate;
    private String createdBy;
}
