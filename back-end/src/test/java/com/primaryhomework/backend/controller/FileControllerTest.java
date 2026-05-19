package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.config.GlobalExceptionHandler;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "app.file.upload-dir=target/test-uploads",
        "app.file.public-base-url="
})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    void uploadEndpointShouldBeRegisteredInRuntime() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.txt",
                "text/plain",
                "demo".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("bizType", "student-homework"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }
}
