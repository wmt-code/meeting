package org.lzg.meeting.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * FFmpeg 处理组件：生成图片缩略图与视频首帧封面
 */
@Component
@Slf4j
public class FFmpegComponent {

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${ffmpeg.thumbnail.image.width:200}")
    private int imageThumbWidth;

    @Value("${ffmpeg.thumbnail.video.width:480}")
    private int videoThumbWidth;

    /**
     * 生成图片缩略图（jpg 格式，等比缩放到指定宽度）
     */
    public byte[] createImageThumbnail(MultipartFile imageFile) throws IOException {
        File tempIn = null;
        try {
            String suffix = getSuffix(imageFile.getOriginalFilename(), ".img");
            tempIn = Files.createTempFile("img_in_", suffix).toFile();
            imageFile.transferTo(tempIn);

            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegPath);
            cmd.add("-y");
            cmd.add("-i");
            cmd.add(tempIn.getAbsolutePath());
            cmd.add("-vf");
            cmd.add("scale=" + imageThumbWidth + ":-1");
            cmd.add("-frames:v");
            cmd.add("1");
            cmd.add("-f");
            cmd.add("image2");
            cmd.add("pipe:1");

            return runToBytes(cmd);
        } finally {
            if (tempIn != null && tempIn.exists()) {
                // noinspection ResultOfMethodCallIgnored
                tempIn.delete();
            }
        }
    }

    /**
     * 从视频提取首帧作为封面（jpg 格式，等比缩放到指定宽度）
     */
    public byte[] createVideoCover(MultipartFile videoFile) throws IOException {
        File tempIn = null;
        try {
            String suffix = getSuffix(videoFile.getOriginalFilename(), ".mp4");
            tempIn = Files.createTempFile("video_in_", suffix).toFile();
            videoFile.transferTo(tempIn);

            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegPath);
            cmd.add("-y");
            cmd.add("-ss");
            cmd.add("00:00:01");
            cmd.add("-i");
            cmd.add(tempIn.getAbsolutePath());
            cmd.add("-vf");
            cmd.add("scale=" + videoThumbWidth + ":-1");
            cmd.add("-frames:v");
            cmd.add("1");
            cmd.add("-f");
            cmd.add("image2");
            cmd.add("pipe:1");

            return runToBytes(cmd);
        } finally {
            if (tempIn != null && tempIn.exists()) {
                // noinspection ResultOfMethodCallIgnored
                tempIn.delete();
            }
        }
    }

    private byte[] runToBytes(List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        try (InputStream is = proc.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            int exit = waitFor(proc);
            if (exit != 0) {
                log.warn("ffmpeg exit code={} command={}", exit, String.join(" ", command));
                throw new IOException("ffmpeg failed, exitCode=" + exit);
            }
            byte[] out = bos.toByteArray();
            if (out.length == 0) {
                // 某些情况下 ffmpeg 可能返回 0 字节但退出码为 0，此时也视为失败以触发上层回退
                log.warn("ffmpeg produced empty output, command={}", String.join(" ", command));
                throw new IOException("ffmpeg produced empty output");
            }
            return out;
        } finally {
            proc.destroy();
        }
    }

    private int waitFor(Process proc) {
        try {
            return proc.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private String getSuffix(String name, String def) {
        if (name == null)
            return def;
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx) : def;
    }
}
