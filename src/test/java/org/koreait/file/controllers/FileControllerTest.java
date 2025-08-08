package org.koreait.file.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.koreait.file.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private FileUploadService uploadService;

    @Autowired
    private MockMvc mockMvc;

    private MockMultipartFile file1;
    private MockMultipartFile file2;

    @BeforeEach
    void init() throws Exception{
        FileInputStream fis = new FileInputStream("C:/data/1.png");
        file1 = new MockMultipartFile("file", "image1.png", MediaType.IMAGE_PNG_VALUE, fis);
        file2 = new MockMultipartFile("file", "image2.png", MediaType.IMAGE_PNG_VALUE, fis);
    }
    
    @Test
    @DisplayName("파일 업로드 테스트")
    void fileUploadTest() throws Exception {

        String body = mockMvc.perform(multipart("/api/v1/file/upload")
                .file(file1)
                .file(file2)
                .param("gid", "testgid")
                .param("location", "testLocation"))
                .andDo(print())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);

        System.out.println(body);
    }
}

