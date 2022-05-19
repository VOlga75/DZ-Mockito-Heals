package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

public class MedicalServiceImplTest {
    private PatientInfoRepository patientInfoFileRep;
    private SendAlertService alertService;
    private static BloodPressure normBP = new BloodPressure(120, 80);
    private static BigDecimal normT = new BigDecimal("36.65");

    @BeforeEach
    public void init() {
        //???patientInfoFileRep = Mockito.mock(PatientInfoFileRepository.class);
        patientInfoFileRep = Mockito.mock(PatientInfoRepository.class);
        alertService = Mockito.mock(SendAlertService.class);
    }
    // Arguments.of("user3", new HealthInfo(new BigDecimal("36.6"), new BloodPressure(125, 78)));

    @ParameterizedTest
    @MethodSource("argsSourceBP")
    public void testCheckBloodPressure(String patientId, BloodPressure bloodPressure, int k) {
        PatientInfo patientInfo = new PatientInfo(patientId, "Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(normT, normBP));
        System.out.println("StartTestCheckBloodPressure!");
        Mockito.when(patientInfoFileRep.getById(patientId)).thenReturn(patientInfo);
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRep, alertService);
        medicalService.checkBloodPressure(patientId, bloodPressure);
        Mockito.verify(alertService, Mockito.times(k)).send(any());
    }

    private static Stream<Arguments> argsSourceBP() {
        return Stream.of(
                Arguments.of("user1", new BloodPressure(122, 78), 1),
                Arguments.of("user2", new BloodPressure(225, 178), 1),
                Arguments.of("user3", normBP, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("argsSourceT")
    public void testCheckTemperature(String patientId, BigDecimal temp, int k) {
        PatientInfo patientInfo = new PatientInfo(patientId, "Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(normT, normBP));
        System.out.println("StartTestCheckTemperature!");
        Mockito.when(patientInfoFileRep.getById(patientId)).thenReturn(patientInfo);
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRep, alertService);
        medicalService.checkTemperature(patientId, temp);
        Mockito.verify(alertService, Mockito.times(k)).send(any());
    }

    private static Stream<Arguments> argsSourceT() {
        return Stream.of(
                // Выявлена ошибка в программе. Для значений больше 36 не выводится сообщение, а должно
                Arguments.of("user2", new BigDecimal("32"), 1),
                Arguments.of("user3", normT, 0),
                Arguments.of("user1", new BigDecimal("38"), 1)
        );
    }

    @Test
    public void testSend() {
        String patientId = "user1";
        PatientInfo patientInfo = new PatientInfo(patientId, "Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(normT, normBP));
        Mockito.when(patientInfoFileRep.getById(patientId)).thenReturn(patientInfo);
        MedicalServiceImpl medicalService = new MedicalServiceImpl(patientInfoFileRep, alertService);
        medicalService.checkBloodPressure(patientId, normBP); // проверяем не посылку метода
        Mockito.verify(alertService, Mockito.times(0)).send(any());

        patientId = "user2";
        patientInfo = new PatientInfo(patientId, "Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("32"), new BloodPressure(122, 78)));
        Mockito.when(patientInfoFileRep.getById(patientId)).thenReturn(patientInfo);
        medicalService = new MedicalServiceImpl(patientInfoFileRep, alertService);
        medicalService.checkBloodPressure(patientId, normBP); // проверяем не посылку метода
        Mockito.verify(alertService, Mockito.times(1)).send(any());
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(alertService).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning,", argumentCaptor.getValue().substring(0, 8));
        medicalService.checkTemperature(patientId, normT); // проверяем не посылку метода
        Mockito.verify(alertService, Mockito.times(1)).send(any());
        Mockito.verify(alertService).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning,", argumentCaptor.getValue().substring(0, 8));


    }
}


       /* if (!patientInfo.getHealthInfo().getBloodPressure().equals(bloodPressure)) {
            String message = String.format("Warning, patient with id: %s, need help", patientInfo.getId());
            alertService.send(message);
        }


        void test_get_advice_in_bad_weather () {
            WeatherService weatherService = Mockito.mock(WeatherService.class);
            Mockito.when(weatherService.currentWeather()).thenReturn(Weather.STORMY);
            PreferencesService preferencesService = Mockito.mock(PreferencesService.class);
            Mockito.when(preferencesService.get(any())).thenReturn(Set.of(Preference.FOOTBALL))
            ;
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            AdviceService adviceService = new AdviceService(preferencesService, weatherService);
            adviceService.getAdvice("user1");
            Mockito.verify(preferencesService).get(argumentCaptor.capture());
            Assertions.assertEquals("user1", argumentCaptor.getValue());
        }
    }

    private static Stream<Arguments> argsSource() {
        return Stream.of(
                Arguments.of("user1", new HealthInfo(new BigDecimal("136.6"), new BloodPressure(125, 78))),
                Arguments.of("user2", new HealthInfo(new BigDecimal("36.6"), new BloodPressure(225, 178))),
                Arguments.of("user3", new HealthInfo(new BigDecimal("36.6"), new BloodPressure(125, 78)));
    }

    public void checkTemperature(String patientId, BigDecimal temperature) {
        PatientInfo patientInfo = getPatientInfo(patientId);
        if (patientInfo.getHealthInfo().getNormalTemperature().subtract(new BigDecimal("1.5")).compareTo(temperature) > 0) {
            String message = String.format("Warning, patient with id: %s, need help", patientInfo.getId());
            System.out.printf("Warning, patient with id: %s, need help", patientInfo.getId());
            alertService.send(message);
        }
    }

    private PatientInfo getPatientInfo(String patientId) {
/*        Mockito.when(patientInfoFileRep.getById(patientId))
                .thenReturn(new PatientInfo    (patientId,"Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
                //Mockito.when(localizationServiceRu.locale(RUSSIA))
        //                .thenReturn("Добро пожаловать");
     //   PatientInfo patientInfo = patientInfoRepository.getById(patientId);
        if (patientInfo == null) {
            throw new RuntimeException("Patient not found");
        }
        return patientInfo;

    }
}*/
