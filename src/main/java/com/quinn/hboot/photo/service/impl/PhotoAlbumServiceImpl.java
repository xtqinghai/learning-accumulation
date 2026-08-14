package com.quinn.hboot.photo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.photo.mapper.*;
import com.quinn.hboot.photo.model.dto.PhotoRequests;
import com.quinn.hboot.photo.model.entity.*;
import com.quinn.hboot.photo.model.vo.PhotoMediaVO;
import com.quinn.hboot.photo.service.PhotoAlbumService;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.service.FileService;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoAlbumServiceImpl implements PhotoAlbumService {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "tif", "tiff", "heic");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv", "webm", "m4v", "mpeg", "mpg");

    private final PhotoSpaceMapper spaceMapper;
    private final PhotoSpaceMemberMapper memberMapper;
    private final PhotoAlbumMapper albumMapper;
    private final PhotoMediaMapper mediaMapper;
    private final PhotoAlbumMediaMapper albumMediaMapper;
    private final PhotoTagMapper tagMapper;
    private final PhotoMediaTagMapper mediaTagMapper;
    private final FileService fileService;

    @Override
    public PhotoSpace getMySpace() {
        Long userId = currentUserId();
        PhotoSpace space = spaceMapper.selectOne(new LambdaQueryWrapper<PhotoSpace>()
            .eq(PhotoSpace::getOwnerId, userId)
            .eq(PhotoSpace::getType, "PERSONAL")
            .eq(PhotoSpace::getStatus, 1));
        if (space != null) {
            ensureOwnerMembership(space.getId(), userId);
            return space;
        }

        PhotoSpace created = new PhotoSpace();
        created.setName("我的家庭相册");
        created.setType("PERSONAL");
        created.setOwnerId(userId);
        created.setUsedBytes(0L);
        created.setStatus(1);
        created.setCreateTime(LocalDateTime.now());
        created.setUpdateTime(LocalDateTime.now());
        try {
            spaceMapper.insert(created);
            space = created;
        } catch (DuplicateKeyException ignored) {
            space = spaceMapper.selectOne(new LambdaQueryWrapper<PhotoSpace>()
                .eq(PhotoSpace::getOwnerId, userId)
                .eq(PhotoSpace::getType, "PERSONAL"));
        }
        ensureOwnerMembership(Objects.requireNonNull(space).getId(), userId);
        return space;
    }

    @Override
    public List<PhotoAlbum> listAlbums() {
        PhotoSpace space = getMySpace();
        return albumMapper.selectList(new LambdaQueryWrapper<PhotoAlbum>()
            .eq(PhotoAlbum::getSpaceId, space.getId())
            .eq(PhotoAlbum::getStatus, 1)
            .orderByAsc(PhotoAlbum::getSortOrder)
            .orderByDesc(PhotoAlbum::getCreateTime));
    }

    @Override
    public PhotoAlbum createAlbum(PhotoRequests.AlbumSave request) {
        PhotoSpace space = getMySpace();
        PhotoAlbum album = new PhotoAlbum();
        applyAlbum(album, request);
        album.setSpaceId(space.getId());
        album.setCreatedBy(currentUserId());
        album.setSortOrder(0);
        album.setMediaCount(0);
        album.setTotalSize(0L);
        album.setStatus(1);
        album.setCreateTime(LocalDateTime.now());
        album.setUpdateTime(LocalDateTime.now());
        albumMapper.insert(album);
        return album;
    }

    @Override
    public PhotoAlbum updateAlbum(Long id, PhotoRequests.AlbumSave request) {
        PhotoAlbum album = requireAlbum(id);
        applyAlbum(album, request);
        album.setUpdateTime(LocalDateTime.now());
        albumMapper.updateById(album);
        return album;
    }

    @Override
    public void deleteAlbum(Long id) {
        PhotoAlbum album = requireAlbum(id);
        albumMediaMapper.delete(new LambdaQueryWrapper<PhotoAlbumMedia>().eq(PhotoAlbumMedia::getAlbumId, id));
        album.setStatus(0);
        album.setDeletedAt(LocalDateTime.now());
        album.setUpdateTime(LocalDateTime.now());
        albumMapper.updateById(album);
    }

    @Override
    public IPage<PhotoMediaVO> pageMedia(Long albumId, String keyword, String mediaType, Long tagId, long page, long size) {
        PhotoSpace space = requireCurrentSpace();
        List<Long> filterIds = null;
        if (albumId != null) {
            PhotoAlbum album = requireAlbum(albumId);
            filterIds = albumMediaMapper.selectList(new LambdaQueryWrapper<PhotoAlbumMedia>()
                    .eq(PhotoAlbumMedia::getAlbumId, album.getId()))
                .stream().map(PhotoAlbumMedia::getMediaId).toList();
            if (filterIds.isEmpty()) {
                return new Page<>(page, size, 0);
            }
        }
        if (tagId != null) {
            PhotoTag tag = tagMapper.selectById(tagId);
            if (tag == null || !Objects.equals(tag.getSpaceId(), space.getId())) {
                throw new BusinessException("标签不存在");
            }
            List<Long> taggedIds = mediaTagMapper.selectList(new LambdaQueryWrapper<PhotoMediaTag>()
                    .eq(PhotoMediaTag::getTagId, tagId))
                .stream().map(PhotoMediaTag::getMediaId).toList();
            filterIds = intersect(filterIds, taggedIds);
            if (filterIds.isEmpty()) {
                return new Page<>(page, size, 0);
            }
        }

        LambdaQueryWrapper<PhotoMedia> query = new LambdaQueryWrapper<PhotoMedia>()
            .eq(PhotoMedia::getSpaceId, space.getId())
            .eq(PhotoMedia::getStatus, 1)
            .eq(StringUtils.hasText(mediaType), PhotoMedia::getMediaType, mediaType == null ? null : mediaType.toUpperCase(Locale.ROOT))
            .in(filterIds != null, PhotoMedia::getId, filterIds)
            .and(StringUtils.hasText(keyword), q -> q.like(PhotoMedia::getDisplayName, keyword)
                .or().like(PhotoMedia::getDescription, keyword)
                .or().like(PhotoMedia::getLocationName, keyword)
                .or().like(PhotoMedia::getCameraModel, keyword))
            .orderByDesc(PhotoMedia::getCapturedAt)
            .orderByDesc(PhotoMedia::getId);
        Page<PhotoMedia> mediaPage = mediaMapper.selectPage(new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 100)), query);
        Page<PhotoMediaVO> result = new Page<>(mediaPage.getCurrent(), mediaPage.getSize(), mediaPage.getTotal());
        result.setRecords(mediaPage.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    @Override
    public PhotoMediaVO getMedia(Long id) {
        return toVO(requireMedia(id));
    }

    @Override
    public PhotoMediaVO upload(MultipartFile file, Long albumId, String relativePath) throws Exception {
        PhotoSpace space = getMySpace();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String mediaType = detectMediaType(file);
        PhotoAlbum album = albumId == null ? null : requireAlbum(albumId);
        SysFile sysFile = fileService.upload(file, "PHOTO_ORIGINAL");
        PhotoMedia media = mediaMapper.selectOne(new LambdaQueryWrapper<PhotoMedia>()
            .eq(PhotoMedia::getSpaceId, space.getId())
            .eq(PhotoMedia::getOriginalFileId, sysFile.getId()));
        if (media == null) {
            media = new PhotoMedia();
            media.setSpaceId(space.getId());
            media.setUploaderId(currentUserId());
            media.setOriginalFileId(sysFile.getId());
            media.setDisplayName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "未命名文件");
            media.setRelativePath(normalizeRelativePath(relativePath));
            media.setMediaType(mediaType);
            media.setFavorite(0);
            media.setCapturedAt(LocalDateTime.now());
            media.setTimeSource("UPLOAD");
            media.setProcessStatus("PENDING");
            media.setStatus(1);
            media.setCreateTime(LocalDateTime.now());
            media.setUpdateTime(LocalDateTime.now());
            mediaMapper.insert(media);
            extractMetadata(media, sysFile);
            space.setUsedBytes(space.getUsedBytes() + Optional.ofNullable(sysFile.getFileSize()).orElse(0L));
            spaceMapper.updateById(space);
        }
        if (album != null) {
            attach(album.getId(), media.getId());
            refreshAlbumStats(album.getId());
        }
        return toVO(mediaMapper.selectById(media.getId()));
    }

    @Override
    public PhotoMediaVO updateMedia(Long id, PhotoRequests.MediaUpdate request) {
        PhotoMedia media = requireMedia(id);
        if (StringUtils.hasText(request.displayName())) media.setDisplayName(request.displayName().trim());
        if (request.description() != null) media.setDescription(request.description().trim());
        if (request.locationName() != null) media.setLocationName(request.locationName().trim());
        if (request.favorite() != null) media.setFavorite(Boolean.TRUE.equals(request.favorite()) ? 1 : 0);
        media.setUpdateTime(LocalDateTime.now());
        mediaMapper.updateById(media);
        return toVO(media);
    }

    @Override
    public void deleteMedia(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        Set<Long> affectedAlbums = new HashSet<>();
        for (Long id : ids) {
            PhotoMedia media = requireMedia(id);
            albumMediaMapper.selectList(new LambdaQueryWrapper<PhotoAlbumMedia>().eq(PhotoAlbumMedia::getMediaId, id))
                .forEach(relation -> affectedAlbums.add(relation.getAlbumId()));
            albumMediaMapper.delete(new LambdaQueryWrapper<PhotoAlbumMedia>().eq(PhotoAlbumMedia::getMediaId, id));
            mediaTagMapper.delete(new LambdaQueryWrapper<PhotoMediaTag>().eq(PhotoMediaTag::getMediaId, id));
            media.setStatus(0);
            media.setDeletedAt(LocalDateTime.now());
            media.setUpdateTime(LocalDateTime.now());
            mediaMapper.updateById(media);
        }
        affectedAlbums.forEach(this::refreshAlbumStats);
        refreshTagUsage();
    }

    @Override
    public void copyOrMove(PhotoRequests.MediaMove request) {
        String mode = request.mode().toUpperCase(Locale.ROOT);
        if (!Set.of("COPY", "MOVE").contains(mode)) throw new BusinessException("操作模式只支持 COPY 或 MOVE");
        PhotoAlbum target = requireAlbum(request.targetAlbumId());
        PhotoAlbum source = request.sourceAlbumId() == null ? null : requireAlbum(request.sourceAlbumId());
        if (source != null && Objects.equals(source.getId(), target.getId())) throw new BusinessException("源相册和目标相册不能相同");
        for (Long mediaId : new LinkedHashSet<>(request.mediaIds())) {
            requireMedia(mediaId);
            attach(target.getId(), mediaId);
            if ("MOVE".equals(mode) && source != null) {
                albumMediaMapper.delete(new LambdaQueryWrapper<PhotoAlbumMedia>()
                    .eq(PhotoAlbumMedia::getAlbumId, source.getId())
                    .eq(PhotoAlbumMedia::getMediaId, mediaId));
            }
        }
        refreshAlbumStats(target.getId());
        if (source != null) refreshAlbumStats(source.getId());
    }

    @Override
    public List<PhotoTag> listTags() {
        PhotoSpace space = requireCurrentSpace();
        return tagMapper.selectList(new LambdaQueryWrapper<PhotoTag>()
            .eq(PhotoTag::getSpaceId, space.getId())
            .orderByDesc(PhotoTag::getUsageCount)
            .orderByAsc(PhotoTag::getName));
    }

    @Override
    public List<PhotoTag> setTags(Long mediaId, List<String> names) {
        PhotoMedia media = requireMedia(mediaId);
        mediaTagMapper.delete(new LambdaQueryWrapper<PhotoMediaTag>().eq(PhotoMediaTag::getMediaId, mediaId));
        Set<String> cleaned = names == null ? Set.of() : names.stream()
            .filter(StringUtils::hasText).map(String::trim)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String name : cleaned) {
            String normalized = name.toLowerCase(Locale.ROOT);
            PhotoTag tag = tagMapper.selectOne(new LambdaQueryWrapper<PhotoTag>()
                .eq(PhotoTag::getSpaceId, media.getSpaceId())
                .eq(PhotoTag::getNormalizedName, normalized));
            if (tag == null) {
                tag = new PhotoTag();
                tag.setSpaceId(media.getSpaceId());
                tag.setName(name);
                tag.setNormalizedName(normalized);
                tag.setUsageCount(0);
                tag.setCreatedBy(currentUserId());
                tag.setCreateTime(LocalDateTime.now());
                tag.setUpdateTime(LocalDateTime.now());
                tagMapper.insert(tag);
            }
            PhotoMediaTag relation = new PhotoMediaTag();
            relation.setMediaId(mediaId);
            relation.setTagId(tag.getId());
            relation.setCreatedBy(currentUserId());
            relation.setCreateTime(LocalDateTime.now());
            mediaTagMapper.insert(relation);
        }
        refreshTagUsage();
        return tagsFor(mediaId);
    }

    @Override
    public void writeContent(Long mediaId, HttpServletResponse response, boolean download) throws Exception {
        PhotoMedia media = requireMedia(mediaId);
        SysFile file = fileService.getById(media.getOriginalFileId());
        if (file == null || file.getStatus() == null || file.getStatus() != 1) throw new BusinessException("原始文件不存在");
        response.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream");
        if (file.getFileSize() != null) response.setContentLengthLong(file.getFileSize());
        String disposition = download ? "attachment" : "inline";
        response.setHeader("Content-Disposition", disposition + "; filename*=UTF-8''" + URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8));
        try (InputStream in = fileService.get(file.getStoragePath()); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }

    @Override
    public void exportAlbum(Long albumId, HttpServletResponse response) throws Exception {
        PhotoAlbum album = requireAlbum(albumId);
        List<Long> mediaIds = albumMediaMapper.selectList(new LambdaQueryWrapper<PhotoAlbumMedia>()
                .eq(PhotoAlbumMedia::getAlbumId, albumId)
                .orderByAsc(PhotoAlbumMedia::getSortOrder)
                .orderByAsc(PhotoAlbumMedia::getAddedAt))
            .stream().map(PhotoAlbumMedia::getMediaId).toList();
        List<PhotoMediaVO> items = mediaIds.stream()
            .map(mediaMapper::selectById)
            .filter(item -> item != null && item.getStatus() == 1)
            .map(this::toVO)
            .toList();

        String exportName = safeDownloadName(album.getName()) + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(exportName, StandardCharsets.UTF_8));
        List<ExportEntry> entries = new ArrayList<>();
        List<String> checksums = new ArrayList<>();
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
            for (int index = 0; index < items.size(); index++) {
                PhotoMediaVO item = items.get(index);
                SysFile file = fileService.getById(item.getOriginalFileId());
                if (file == null) continue;
                String ext = StringUtils.hasText(file.getFileExt()) ? "." + file.getFileExt().toLowerCase(Locale.ROOT) : "";
                String path = String.format(Locale.ROOT, "media/%04d%s", index + 1, ext);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                zip.putNextEntry(new ZipEntry(path));
                try (InputStream raw = fileService.get(file.getStoragePath()); DigestInputStream input = new DigestInputStream(raw, digest)) {
                    input.transferTo(zip);
                }
                zip.closeEntry();
                String hash = HexFormat.of().formatHex(digest.digest());
                checksums.add(hash + "  " + path);
                entries.add(new ExportEntry(item, path, hash));
            }
            writeZipText(zip, "index.html", buildOfflineHtml(album, entries));
            writeZipText(zip, "album.json", buildAlbumJson(album, entries));
            writeZipText(zip, "checksums.sha256", String.join("\n", checksums) + "\n");
        }
    }

    private void extractMetadata(PhotoMedia media, SysFile file) {
        try {
            if ("IMAGE".equals(media.getMediaType())) extractImageMetadata(media, file);
            else extractVideoMetadata(media, file);
            media.setProcessStatus("READY");
            media.setProcessError(null);
        } catch (Exception e) {
            media.setProcessStatus("FAILED");
            media.setProcessError(limit(e.getMessage(), 1000));
        }
        media.setUpdateTime(LocalDateTime.now());
        mediaMapper.updateById(media);
    }

    private void extractImageMetadata(PhotoMedia media, SysFile file) throws Exception {
        try (InputStream input = fileService.get(file.getStoragePath())) {
            BufferedImage image = ImageIO.read(input);
            if (image != null) {
                media.setWidth(image.getWidth());
                media.setHeight(image.getHeight());
            }
        }
        try (InputStream input = fileService.get(file.getStoragePath())) {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) {
                media.setOrientation(ifd0.getInteger(ExifIFD0Directory.TAG_ORIENTATION));
                media.setCameraMake(ifd0.getString(ExifIFD0Directory.TAG_MAKE));
                media.setCameraModel(ifd0.getString(ExifIFD0Directory.TAG_MODEL));
            }
            ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (sub != null) {
                Date captured = sub.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (captured != null) {
                    media.setCapturedAt(LocalDateTime.ofInstant(captured.toInstant(), ZoneId.systemDefault()));
                    media.setTimeSource("EXIF");
                }
                media.setLensModel(sub.getString(ExifSubIFDDirectory.TAG_LENS_MODEL));
            }
            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gps != null) {
                GeoLocation location = gps.getGeoLocation();
                if (location != null && !location.isZero()) {
                    media.setLatitude(BigDecimal.valueOf(location.getLatitude()).setScale(7, RoundingMode.HALF_UP));
                    media.setLongitude(BigDecimal.valueOf(location.getLongitude()).setScale(7, RoundingMode.HALF_UP));
                }
                RationalAltitude altitude = readAltitude(gps);
                if (altitude.value != null) media.setAltitude(altitude.value);
            }
        }
    }

    private void extractVideoMetadata(PhotoMedia media, SysFile file) throws Exception {
        try (InputStream input = fileService.get(file.getStoragePath()); FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();
            media.setWidth(grabber.getImageWidth());
            media.setHeight(grabber.getImageHeight());
            media.setDurationMs(grabber.getLengthInTime() / 1000);
            media.setVideoCodec(grabber.getVideoCodecName());
            media.setAudioCodec(grabber.getAudioCodecName());
            media.setBitRate((long) grabber.getVideoBitrate());
            media.setFrameRate(BigDecimal.valueOf(grabber.getFrameRate()).setScale(3, RoundingMode.HALF_UP));
            grabber.stop();
        }
    }

    private RationalAltitude readAltitude(GpsDirectory gps) {
        try {
            com.drew.lang.Rational rational = gps.getRational(GpsDirectory.TAG_ALTITUDE);
            return new RationalAltitude(rational == null ? null : BigDecimal.valueOf(rational.doubleValue()).setScale(2, RoundingMode.HALF_UP));
        } catch (Exception ignored) {
            return new RationalAltitude(null);
        }
    }

    private PhotoMediaVO toVO(PhotoMedia media) {
        PhotoMediaVO vo = new PhotoMediaVO();
        BeanUtils.copyProperties(media, vo);
        SysFile file = fileService.getById(media.getOriginalFileId());
        if (file != null) {
            vo.setFileName(file.getFileName());
            vo.setContentType(file.getContentType());
            vo.setFileSize(file.getFileSize());
        }
        vo.setTags(tagsFor(media.getId()));
        return vo;
    }

    private List<PhotoTag> tagsFor(Long mediaId) {
        List<Long> tagIds = mediaTagMapper.selectList(new LambdaQueryWrapper<PhotoMediaTag>()
                .eq(PhotoMediaTag::getMediaId, mediaId))
            .stream().map(PhotoMediaTag::getTagId).toList();
        return tagIds.isEmpty() ? List.of() : tagMapper.selectBatchIds(tagIds);
    }

    private void refreshTagUsage() {
        PhotoSpace space = requireCurrentSpace();
        for (PhotoTag tag : tagMapper.selectList(new LambdaQueryWrapper<PhotoTag>().eq(PhotoTag::getSpaceId, space.getId()))) {
            Long count = mediaTagMapper.selectCount(new LambdaQueryWrapper<PhotoMediaTag>().eq(PhotoMediaTag::getTagId, tag.getId()));
            tag.setUsageCount(count.intValue());
            tag.setUpdateTime(LocalDateTime.now());
            tagMapper.updateById(tag);
        }
    }

    private void attach(Long albumId, Long mediaId) {
        Long count = albumMediaMapper.selectCount(new LambdaQueryWrapper<PhotoAlbumMedia>()
            .eq(PhotoAlbumMedia::getAlbumId, albumId).eq(PhotoAlbumMedia::getMediaId, mediaId));
        if (count > 0) return;
        PhotoAlbumMedia relation = new PhotoAlbumMedia();
        relation.setAlbumId(albumId);
        relation.setMediaId(mediaId);
        relation.setSortOrder(0);
        relation.setAddedBy(currentUserId());
        relation.setAddedAt(LocalDateTime.now());
        albumMediaMapper.insert(relation);
    }

    private void refreshAlbumStats(Long albumId) {
        PhotoAlbum album = albumMapper.selectById(albumId);
        if (album == null) return;
        List<Long> mediaIds = albumMediaMapper.selectList(new LambdaQueryWrapper<PhotoAlbumMedia>()
                .eq(PhotoAlbumMedia::getAlbumId, albumId))
            .stream().map(PhotoAlbumMedia::getMediaId).toList();
        long total = 0;
        for (Long mediaId : mediaIds) {
            PhotoMedia media = mediaMapper.selectById(mediaId);
            if (media != null && media.getStatus() == 1) {
                SysFile file = fileService.getById(media.getOriginalFileId());
                total += file == null || file.getFileSize() == null ? 0 : file.getFileSize();
            }
        }
        album.setMediaCount(mediaIds.size());
        album.setTotalSize(total);
        album.setUpdateTime(LocalDateTime.now());
        albumMapper.updateById(album);
    }

    private PhotoSpace requireCurrentSpace() {
        PhotoSpace space = getMySpace();
        assertMembership(space.getId());
        return space;
    }

    private PhotoAlbum requireAlbum(Long id) {
        PhotoAlbum album = albumMapper.selectById(id);
        if (album == null || album.getStatus() == null || album.getStatus() != 1) throw new BusinessException("相册不存在");
        assertMembership(album.getSpaceId());
        return album;
    }

    private PhotoMedia requireMedia(Long id) {
        PhotoMedia media = mediaMapper.selectById(id);
        if (media == null || media.getStatus() == null || media.getStatus() != 1) throw new BusinessException("照片或视频不存在");
        assertMembership(media.getSpaceId());
        return media;
    }

    private void assertMembership(Long spaceId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<PhotoSpaceMember>()
            .eq(PhotoSpaceMember::getSpaceId, spaceId)
            .eq(PhotoSpaceMember::getUserId, currentUserId())
            .eq(PhotoSpaceMember::getStatus, 1));
        if (count == 0) throw new BusinessException("无权访问该相册空间");
    }

    private void ensureOwnerMembership(Long spaceId, Long userId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<PhotoSpaceMember>()
            .eq(PhotoSpaceMember::getSpaceId, spaceId).eq(PhotoSpaceMember::getUserId, userId));
        if (count > 0) return;
        PhotoSpaceMember member = new PhotoSpaceMember();
        member.setSpaceId(spaceId);
        member.setUserId(userId);
        member.setRole("OWNER");
        member.setStatus(1);
        member.setJoinedAt(LocalDateTime.now());
        try {
            memberMapper.insert(member);
        } catch (DuplicateKeyException ignored) {
            // Concurrent first access may create the same owner membership.
        }
    }

    private Long currentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) throw new BusinessException("请先登录");
        return userId;
    }

    private String detectMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if ((contentType != null && contentType.startsWith("image/")) || IMAGE_EXTENSIONS.contains(ext)) return "IMAGE";
        if ((contentType != null && contentType.startsWith("video/")) || VIDEO_EXTENSIONS.contains(ext)) return "VIDEO";
        throw new BusinessException("仅支持图片和视频文件");
    }

    private String normalizeRelativePath(String path) {
        if (!StringUtils.hasText(path)) return null;
        String normalized = path.replace('\\', '/').replaceAll("^/+", "");
        if (normalized.contains("../") || normalized.equals("..")) throw new BusinessException("目录相对路径不合法");
        return limit(normalized, 1000);
    }

    private void applyAlbum(PhotoAlbum album, PhotoRequests.AlbumSave request) {
        album.setName(request.name().trim());
        album.setDescription(request.description() == null ? null : request.description().trim());
        album.setAlbumDate(request.albumDate());
        album.setLocationName(request.locationName() == null ? null : request.locationName().trim());
    }

    private List<Long> intersect(List<Long> left, List<Long> right) {
        if (left == null) return right;
        Set<Long> rightSet = new HashSet<>(right);
        return left.stream().filter(rightSet::contains).toList();
    }

    private String limit(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    private void writeZipText(ZipOutputStream zip, String path, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String buildOfflineHtml(PhotoAlbum album, List<ExportEntry> entries) {
        StringBuilder content = new StringBuilder();
        for (ExportEntry entry : entries) {
            PhotoMediaVO item = entry.media();
            String media = "VIDEO".equals(item.getMediaType())
                ? "<video controls preload=\"metadata\" src=\"" + entry.path() + "\"></video>"
                : "<a href=\"" + entry.path() + "\" target=\"_blank\"><img loading=\"lazy\" src=\"" + entry.path() + "\" alt=\"" + html(item.getDisplayName()) + "\"></a>";
            content.append("<article>").append(media)
                .append("<div><strong>").append(html(item.getDisplayName())).append("</strong><span>")
                .append(html(item.getCapturedAt() == null ? "" : item.getCapturedAt().toString().replace('T', ' ')))
                .append("</span><small>").append(html(Optional.ofNullable(item.getLocationName()).orElse("")))
                .append("</small></div></article>");
        }
        return """
            <!doctype html><html lang="zh-CN"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
            <title>%s</title><style>body{margin:0;background:#f4f5f6;color:#24272c;font:14px system-ui,sans-serif}header{padding:28px 4vw;background:#fff;border-bottom:1px solid #ddd}h1{margin:0 0 7px;font-size:26px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:18px;padding:24px 4vw}article{overflow:hidden;border:1px solid #dcdfe3;border-radius:6px;background:#fff}img,video{display:block;width:100%%;aspect-ratio:4/3;object-fit:contain;background:#17191d}article div{display:grid;gap:4px;padding:11px}span,small{color:#727983}</style></head>
            <body><header><h1>%s</h1><div>%s</div></header><main class="grid">%s</main></body></html>
            """.formatted(html(album.getName()), html(album.getName()), html(Optional.ofNullable(album.getDescription()).orElse("")), content);
    }

    private String buildAlbumJson(PhotoAlbum album, List<ExportEntry> entries) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("format", "h-boot-family-album-v1");
        root.put("exportedAt", LocalDateTime.now().toString());
        root.put("album", album);
        root.put("media", entries.stream().map(entry -> Map.of(
            "path", entry.path(),
            "sha256", entry.sha256(),
            "metadata", entry.media()
        )).toList());
        return cn.hutool.json.JSONUtil.toJsonPrettyStr(root);
    }

    private String html(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String safeDownloadName(String value) {
        String safe = value == null ? "album" : value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return safe.isEmpty() ? "album" : safe;
    }

    private record RationalAltitude(BigDecimal value) {}
    private record ExportEntry(PhotoMediaVO media, String path, String sha256) {}
}
