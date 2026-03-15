package com.clinic.app.dto.consultation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDoctorNotesRequest {
    @NotBlank
    private String doctorNotes;
}
