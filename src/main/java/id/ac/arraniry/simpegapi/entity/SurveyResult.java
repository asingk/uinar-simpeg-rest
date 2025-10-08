package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class SurveyResult {
    @Id
    private String id;
    private String nip;
    private String namaPegawai;
    private String surveyId;
    private String optionId;
    private LocalDateTime answeredAt;
}
