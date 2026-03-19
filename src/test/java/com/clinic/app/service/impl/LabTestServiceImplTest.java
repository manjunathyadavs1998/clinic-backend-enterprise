package com.clinic.app.service.impl;

import com.clinic.app.dto.labtest.CreateLabTestRequest;
import com.clinic.app.dto.labtest.UpdateLabTestRequest;
import com.clinic.app.dto.labtest.LabTestResponse;
import com.clinic.app.entity.LabTest;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.LabTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabTestServiceImplTest {

    @Mock
    private LabTestRepository labTestRepository;

    @InjectMocks
    private LabTestServiceImpl labTestService;

    private LabTest labTest;

    @BeforeEach
    void setUp() {
        labTest = LabTest.builder()
                .id(1L)
                .name("Blood Test")
                .cost(new BigDecimal("100.00"))
                .active(true)
                .build();
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        CreateLabTestRequest request = new CreateLabTestRequest();
        request.setName("Blood Test");
        request.setCost(new BigDecimal("100.00"));

        when(labTestRepository.save(any(LabTest.class))).thenReturn(labTest);

        LabTestResponse response = labTestService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Blood Test");
        assertThat(response.getCost()).isEqualByComparingTo("100.00");
        assertThat(response.getActive()).isTrue();
        verify(labTestRepository).save(any(LabTest.class));
    }

    @Test
    void getAll_shouldReturnAllTests() {
        when(labTestRepository.findAll()).thenReturn(List.of(labTest));

        List<LabTestResponse> result = labTestService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Blood Test");
    }

    @Test
    void getById_shouldReturnResponse() {
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));

        LabTestResponse response = labTestService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Blood Test");
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(labTestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labTestService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateNameAndCostAndActive() {
        UpdateLabTestRequest request = new UpdateLabTestRequest();
        request.setName("X-Ray");
        request.setCost(new BigDecimal("200.00"));
        request.setActive(false);

        LabTest updated = LabTest.builder().id(1L).name("X-Ray").cost(new BigDecimal("200.00")).active(false).build();
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.save(any())).thenReturn(updated);

        LabTestResponse response = labTestService.update(1L, request);

        assertThat(response.getName()).isEqualTo("X-Ray");
        assertThat(response.getCost()).isEqualByComparingTo("200.00");
        assertThat(response.getActive()).isFalse();
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(labTestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labTestService.update(99L, new UpdateLabTestRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldNotOverwriteNullFields() {
        UpdateLabTestRequest request = new UpdateLabTestRequest();
        request.setName("X-Ray");
        // cost and active are null — should not change

        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.save(any())).thenReturn(labTest);

        labTestService.update(1L, request);

        assertThat(labTest.getCost()).isEqualByComparingTo("100.00");
        assertThat(labTest.getActive()).isTrue();
    }

    @Test
    void delete_shouldDeleteTest() {
        when(labTestRepository.existsById(1L)).thenReturn(true);

        labTestService.delete(1L);

        verify(labTestRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(labTestRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> labTestService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(labTestRepository, never()).deleteById(any());
    }
}
