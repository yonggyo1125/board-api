package org.koreait.file.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.koreait.file.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private FileUploadService uploadService;

    @Autowired
    private MockMvc mockMvc;

    private MultipartFile file1;
    private MultipartFile file2;

    @BeforeEach
    void init() throws Exception{
        FileInputStream fis = new FileInputStream("C:/data/1.png");
        file1 = new MockMultipartFile("file", "image1.png", MediaType.IMAGE_PNG_VALUE, fis);
        file2 = new MockMultipartFile("file", "image2.png", MediaType.IMAGE_PNG_VALUE, fis);
    }

}

