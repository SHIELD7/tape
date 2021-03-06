package site.imcu.tape.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.imcu.tape.pojo.Clip;
import site.imcu.tape.pojo.ResponseData;
import site.imcu.tape.pojo.User;
import site.imcu.tape.security.jwt.TokenProvider;
import site.imcu.tape.service.impl.ClipServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author : MengHe
 * @date : 2020/2/23 15:35
 */
@RestController
@RequestMapping("/api/clip")
@Slf4j
public class ClipController {

    @Value("${tape.video-source-file-path}")
    private String videoSourceFilePath;
    @Autowired
    ClipServiceImpl clipService;
    @Autowired
    TokenProvider tokenProvider;

    @PostMapping("/create")
    public ResponseData createClip(@RequestParam("video") MultipartFile video, String title, String coverTime) throws IOException {

        String fileType = FileUtil.extName(video.getOriginalFilename());
        String fileId = IdUtil.fastSimpleUUID();
        String sourceFilename = StrUtil.format("{}.{}", fileId, fileType);
        File sourceFile = new File(videoSourceFilePath, sourceFilename);
        video.transferTo(sourceFile);

        Clip clip = new Clip();
        clip.setCoverTime(coverTime);
        clip.setSourceFile(sourceFilename);
        clip.setCoverPath(StrUtil.format("{}.png", fileId));
        clip.setClipPath(StrUtil.format("{}.m3u8", fileId));
        clip.setTitle(title);
        clip.setCreator(tokenProvider.getCurrentUser().getId());
        clip.setDeleted(0);
        clip.setCreateMan(clip.getCreator());
        clip.setCreateTime(new Date());
        Integer result = clipService.createClip(clip);

        return ResponseData.builder().code(result == 1 ? 1 : -1).build();
    }

    @GetMapping("/show/{id}")
    public ResponseData showClip(@PathVariable Long id) {
        User currentUser = tokenProvider.getCurrentUser();
        Clip clip = clipService.getClipById(id, currentUser);
        return ResponseData.builder().code(clip != null ? 1 : -1).data(clip).build();

    }

    @PostMapping("/list")
    public ResponseData listClip(@RequestBody Clip clip) {
        clip.setDeleted(0);
        Page<Clip> pageParam = getPageParam(clip);
        IPage<Clip> clipPage = clipService.getClipPage(pageParam, clip, tokenProvider.getCurrentUser());
        return ResponseData.builder().code(1).data(clipPage).build();
    }

    @GetMapping("/hot")
    public ResponseData hotClip() {
        List<Clip> clipList = clipService.hotClip(tokenProvider.getCurrentUser());
        return ResponseData.builder().code(1).data(clipList).build();
    }

    @PostMapping("/list-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseData getAllClip(@RequestBody Clip clip) {
        Page<Clip> pageParam = getPageParam(clip);
        IPage<Clip> clipPage = clipService.getClipPage(pageParam, clip, tokenProvider.getCurrentUser());
        return ResponseData.builder().code(1).data(clipPage).build();
    }


    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseData updateClip(@RequestBody Clip clip) {
        Long userId = tokenProvider.getCurrentUser().getId();
        clip.setUpdateMan(userId);
        clip.setUpdateTime(new Date());
        Integer result = clipService.updateClip(clip);
        return ResponseData.builder().code(result == 1 ? 1 : -1).build();

    }

    private Page<Clip> getPageParam(Clip clip) {
        Page<Clip> page = new Page<>();
        if (clip.getSize() == null || clip.getCurrent() == null) {
            return page;
        }
        BeanUtils.copyProperties(clip, page);
        return page;
    }


}
