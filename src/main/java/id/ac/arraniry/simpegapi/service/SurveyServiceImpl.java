package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.entity.Survey;
import id.ac.arraniry.simpegapi.entity.SurveyResult;
import id.ac.arraniry.simpegapi.repo.SurveyRepo;
import id.ac.arraniry.simpegapi.repo.SurveyResultRepo;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SurveyServiceImpl implements SurveyService {
    private final SurveyResultRepo surveyResultRepo;
    private final SurveyRepo surveyRepo;
    private final KehadiranUtils kehadiranUtils;

    public SurveyServiceImpl(SurveyResultRepo surveyResultRepo, SurveyRepo surveyRepo, KehadiranUtils kehadiranUtils) {
        this.surveyResultRepo = surveyResultRepo;
        this.surveyRepo = surveyRepo;
        this.kehadiranUtils = kehadiranUtils;
    }

    @Override
    public SurveyResult findTodayAnswerByNip(String nip) {
        return surveyResultRepo.findTodayAnswerByNip(nip).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey result not found"));
    }

    @Override
    public Survey findToday() {
        return surveyRepo.findToday().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));
    }

    @Override
    public CreateResponse answer(String nip, String surveyId, String answer) {
        Optional<SurveyResult> result = surveyResultRepo.findByNipAndSurveyId(nip, surveyId);
        if (result.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Survey result already exist");
        }
        SurveyResult surveyResult = new SurveyResult();
        surveyResult.setNip(nip);
        surveyResult.setNamaPegawai(kehadiranUtils.getProfilPegawaiFromSimpegGraphql(nip).getNama());
        surveyResult.setSurveyId(surveyId);
        surveyResult.setOptionId(answer);
        surveyResult.setAnsweredAt(LocalDateTime.now());
        return new CreateResponse(surveyResultRepo.save(surveyResult).getId());
    }
}
