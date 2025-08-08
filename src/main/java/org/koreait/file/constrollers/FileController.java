package org.koreait.file.constrollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.koreait.file.entities.FileInfo;
import org.koreait.file.services.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/file", "/file"})
@Tag(name="파일 API", description = "파일 업로드, 다운로드, 조회기능을 제공")
public class FileController {
    private final FileUploadService uploadService;
    private final FileDeleteService deleteService;
    private final FileInfoService infoService;
    private final FileDownloadService downloadService;
    private final ThumbnailService thumbnailService;

    @Operation(summary = "파일 업로드 처리", method = "MULTIPART")
    @Parameters({
            @Parameter(name="gid", required = true, in=ParameterIn.QUERY, description = "그룹 아이디"),
            @Parameter(name="location", in=ParameterIn.QUERY, description = "그룹 내에서 위치 구문 문자열"),
            @Parameter(name="single", in=ParameterIn.QUERY, description = "단일 파일 업로드, 기 업로드된 동일한 gid + location의 파일은 삭제가 되고 새로 업로드"),
            @Parameter(name="imageOnly", in=ParameterIn.QUERY, description = "이미지 형식으로만 파일 업로드를 제한")
    })
    @ApiResponse(responseCode = "201", description = "파일 업로드 성공시 업로드한 파일 목록이 출력, 파일 업로드 후 후속 처리시 활용")
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public List<FileInfo> upload(RequestUpload form, @RequestPart("file") MultipartFile[] files) {
        form.setFiles(files);
        List<FileInfo> items = uploadService.process(form);

        return items;
    }

    @Operation(summary = "파일 목록 조회", description = "/list/그룹아이디 - 그룹아이디로 목록 조회, /list/그룹아이디/위치문자열 - 그룹아이디 + 위치 문자열로 목록 조회, 파일은 그룹작업이 완료된 파일만 노출된다.")
    @Parameters({
            @Parameter(name="gid", required = true, in=ParameterIn.PATH, description = "그룹아이디"),
            @Parameter(name="location", in=ParameterIn.PATH, description = "그룹 내에서 위치 구분 문자열")
    })
    @GetMapping({"/list/{gid}", "/list/{gid}/{location}"})
    public List<FileInfo> list(@PathVariable("gid") String gid, @PathVariable(name="location", required = false) String location) {

        List<FileInfo> items = infoService.getList(gid, location);

        return items;
    }

    @Operation(summary = "파일 등록번호로 파일 정보 한개 조회")
    @Parameters({
            @Parameter(name="seq", in= ParameterIn.PATH, required = true, description = "파일 등록 번호")
    })
    @GetMapping("/info/{seq}")
    public FileInfo info(@PathVariable("seq") Long seq) {
        FileInfo item = infoService.get(seq);

        return item;
    }
    
    @Operation(summary = "파일 한개 삭제", description = "파일 등록번호(seq)로 삭제")
    @Parameter(name="seq", required = true, in=ParameterIn.PATH)
    @ApiResponse(responseCode = "200", description = "삭제된 파일 정보가 반환, 삭제 후 후속처리시 활용")
    @DeleteMapping("/delete/{seq}")
    public FileInfo delete(@PathVariable("seq") Long seq) {
        FileInfo item = deleteService.process(seq);

        return item;
    }

    @Operation(summary = "파일 목록 삭제", description = "그룹아이디 또는 그룹아이디 + 그룹내 위치 구분문자열 조합으로 목록 삭제")
    @Parameters({
            @Parameter(name="gid", required = true, in=ParameterIn.PATH, description = "그룹아이디"),
            @Parameter(name="location", in=ParameterIn.PATH, description = "그룹 내에서 위치 구분 문자열")
    })
    @ApiResponse(responseCode = "200", description = "파일 삭제 후 삭제된 파일 목록 정보 반환, , 삭제 후 후속처리시 활용")
    @DeleteMapping({"/deletes/{gid}", "/deletes/{gid}/{location}"})
    public List<FileInfo> deletes(@PathVariable("gid") String gid, @PathVariable(name="location", required = false) String location) {
        List<FileInfo> items = deleteService.process(gid, location);

        return items;
    }
    /**
     * 파일 다운로드
     *
     *
     *
     */
    @Operation(summary = "파일 다운로드", description = "파일 등록번호로 원래 업로드한 파일명으로 다운로드")
    @Parameter(name="seq", required = true, in=ParameterIn.PATH, description = "파일 등록번호")
    @GetMapping("/download/{seq}")
    public void download(@PathVariable("seq") Long seq) {
        downloadService.process(seq);
    }

    @Operation(summary = "썸네일 이미지 출력", description = "파일 등록번호(seq)와 너비(width), 높이(height), crop 옵션으로 원하는 사이즈로 출력")
    @Parameters({
            @Parameter(name="seq", required = true, in=ParameterIn.QUERY, description = "파일 등록번호"),
            @Parameter(name="width", in=ParameterIn.QUERY, description = "너비"),
            @Parameter(name="height", in=ParameterIn.QUERY, description = "높이"),
            @Parameter(name="crop", in=ParameterIn.QUERY, description = "크롭 이미지 생성 여부, true / false 문자열로 설정")
    })
    @GetMapping("/thumb")
    public void thumb(RequestThumb form, HttpServletResponse response) {
        String path = thumbnailService.create(form);
        if (!StringUtils.hasText(path)) {
            return;
        }

        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            String contentType = Files.probeContentType(file.toPath()); // 이미지 파일 형식
            response.setContentType(contentType);

            OutputStream out = response.getOutputStream();
            out.write(bis.readAllBytes());

        } catch (IOException e) {}
    }
    
    @Operation(summary = "파일 등록번호로 원본 이미지를 출력")
    @Parameter(name="seq", required = true, in=ParameterIn.PATH, description = "파일 등록번호, 반드시 이미지 파일 등록번호만 설정")
    @GetMapping("/image/{seq}")
    public ResponseEntity<byte[]> showImage(@PathVariable("seq") Long seq) {
        FileInfo item = infoService.get(seq);

        String contentType = item.getContentType();
        byte[] bytes = null;
        File file = new File(item.getFilePath());
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            bytes = bis.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(contentType));

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}