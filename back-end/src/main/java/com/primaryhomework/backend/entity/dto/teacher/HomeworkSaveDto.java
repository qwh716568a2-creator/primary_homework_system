package com.primaryhomework.backend.entity.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkSaveDto {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "subjectCode is required")
    private String subjectCode;

    private String contentText;

    @NotEmpty(message = "classIds is required")
    private List<Long> classIds = new ArrayList<>();

    @NotNull(message = "deadlineAt is required")
    private LocalDateTime deadlineAt;

    private Boolean allowLateSubmit;
    private Boolean allowResubmit;
    private Boolean needParentConfirm;

    @NotEmpty(message = "submitTypes is required")
    private List<String> submitTypes = new ArrayList<>();

    private List<HomeworkAssetDto> attachments = new ArrayList<>();
    private Boolean publishNow;

    public void setDeadlineAt(String deadlineAt) {
        if (deadlineAt == null || deadlineAt.isBlank()) {
            this.deadlineAt = null;
            return;
        }
        String value = deadlineAt.trim();
        this.deadlineAt = parseDeadlineAt(value);
    }

    private LocalDateTime parseDeadlineAt(String value) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next common frontend date-time format.
            }
        }
        throw new IllegalArgumentException("deadlineAt format must be yyyy-MM-dd HH:mm:ss or yyyy-MM-dd'T'HH:mm:ss");
    }
}
