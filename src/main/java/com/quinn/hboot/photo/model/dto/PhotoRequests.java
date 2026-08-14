package com.quinn.hboot.photo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class PhotoRequests {
    private PhotoRequests() {}

    public record AlbumSave(
        @NotBlank(message = "相册名称不能为空") String name,
        String description,
        LocalDate albumDate,
        String locationName
    ) {}

    public record MediaMove(
        Long sourceAlbumId,
        @NotNull(message = "目标相册不能为空") Long targetAlbumId,
        @NotEmpty(message = "请选择照片或视频") List<Long> mediaIds,
        @NotBlank(message = "操作模式不能为空") String mode
    ) {}

    public record TagSave(
        @NotNull(message = "标签不能为空") List<String> tags
    ) {}

    public record MediaUpdate(
        String displayName,
        String description,
        String locationName,
        Boolean favorite
    ) {}
}
