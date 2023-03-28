package br.com.ifce.easyflow.service;

import br.com.ifce.easyflow.controller.dto.daily.DailyRequestSaveDTO;
import br.com.ifce.easyflow.controller.dto.daily.DailyRequestSaveFeedbackDTO;
import br.com.ifce.easyflow.controller.dto.daily.DailyRequestUpdateDTO;
import br.com.ifce.easyflow.controller.dto.daily.DailyResponseDTO;
import br.com.ifce.easyflow.model.Daily;
import br.com.ifce.easyflow.model.Person;
import br.com.ifce.easyflow.repository.DailyRepository;
import br.com.ifce.easyflow.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyService {
    private final DailyRepository dailyRepository;
    private final PersonRepository personRepository;

    public Page<DailyResponseDTO> listAll(Pageable pageable) {
        List<Daily> dailyList = dailyRepository.findAll(pageable).getContent();
        List<DailyResponseDTO> dailyResponseDTOList = dailyList.stream()
                .map(DailyResponseDTO::toResponseDTO)
                .collect(Collectors.toList());
        return turningListOfDailyResponseDTOIntoPage(dailyResponseDTOList, pageable);
    }

    public DailyResponseDTO findById(Long id) {
        Daily daily = dailyRepository.findById(id).orElseThrow();

        return DailyResponseDTO.toResponseDTO(daily);
    }

    public Page<DailyResponseDTO> listByPersonId(Long id, Pageable pageable) {
        List<DailyResponseDTO> dailyResponseDTOList = dailyRepository.findByPersonId(id, pageable).stream()
                .map(DailyResponseDTO::toResponseDTO)
                .collect(Collectors.toList());
        return turningListOfDailyResponseDTOIntoPage(dailyResponseDTOList, pageable);
    }

    public Page<DailyResponseDTO> listByDate(String date, Pageable pageable) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<DailyResponseDTO> dailyResponseDTOList = dailyRepository.findByDate(localDate)
                .stream()
                .map(DailyResponseDTO::toResponseDTO)
                .collect(Collectors.toList());
        return turningListOfDailyResponseDTOIntoPage(dailyResponseDTOList, pageable);
    }

    public Page<DailyResponseDTO> listByPersonIdAndDate(Long id, String date, Pageable pageable) {

        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<DailyResponseDTO> dailyResponseDTOList = dailyRepository.findByPersonIdAndDate(id, localDate)
                .stream()
                .map(DailyResponseDTO::toResponseDTO)
                .collect(Collectors.toList());
        return turningListOfDailyResponseDTOIntoPage(dailyResponseDTOList, pageable);
    }

    @Transactional
    public DailyResponseDTO save(DailyRequestSaveDTO dailyRequestSaveDTO) {
        Person person = personRepository.findById(dailyRequestSaveDTO.getPersonId()).orElseThrow();

        Daily daily = Daily.builder()
                .dailyTaskStatusEnum(dailyRequestSaveDTO.getDailyTaskStatusEnum())
                .whatWasDoneTodayMessage(dailyRequestSaveDTO.getWhatWasDoneTodayMessage())
                .anyQuestionsMessage(dailyRequestSaveDTO.getAnyQuestionsMessage())
                .date(dailyRequestSaveDTO.getDate())
                .person(person)
                .build();
        return DailyResponseDTO.toResponseDTO(dailyRepository.save(daily));


    }

    @Transactional
    public DailyResponseDTO update(Long dailyId, DailyRequestUpdateDTO dailyRequestUpdateDTO) {
        Daily dailySaved = dailyRepository.findById(dailyId).orElseThrow();

        Daily dailyToUpdate = updateDailyWithDailyUpdateDto(dailySaved, dailyRequestUpdateDTO);
        return DailyResponseDTO.toResponseDTO(dailyRepository.save(dailyToUpdate));


    }

    @Transactional
    public DailyResponseDTO saveFeedback(Long id, DailyRequestSaveFeedbackDTO dailyRequestSaveFeedbackDTO) {
        Daily dailySaved = dailyRepository.findById(id).orElseThrow();

        Daily dailyToSaveFeedback = updateFeedbackDaily(dailySaved, dailyRequestSaveFeedbackDTO);

        return DailyResponseDTO.toResponseDTO(dailyRepository.save(dailyToSaveFeedback));

    }

    @Transactional
    public void delete(Long id) {
        dailyRepository.findById(id).orElseThrow();
        dailyRepository.deleteById(id);
    }


    private Daily updateDailyWithDailyUpdateDto(Daily daily, DailyRequestUpdateDTO dailyRequestUpdateDTO) {
        daily.setAnyQuestionsMessage(dailyRequestUpdateDTO.getAnyQuestionsMessage());
        daily.setWhatWasDoneTodayMessage(dailyRequestUpdateDTO.getWhatWasDoneTodayMessage());
        return daily;
    }

    private Daily updateFeedbackDaily(Daily daily, DailyRequestSaveFeedbackDTO dailyRequestSaveFeedbackDTO){
        daily.setFeedbackMessage(dailyRequestSaveFeedbackDTO.getFeedbackMessage());
        return daily;
    }

    private Page<DailyResponseDTO> turningListOfDailyResponseDTOIntoPage(List<DailyResponseDTO> dailyResponseDTOS, Pageable pageable) {
        return new PageImpl<DailyResponseDTO>(dailyResponseDTOS, pageable, dailyResponseDTOS.size());
    }



}