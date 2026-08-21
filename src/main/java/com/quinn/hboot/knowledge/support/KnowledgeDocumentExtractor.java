package com.quinn.hboot.knowledge.support;

import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.service.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class KnowledgeDocumentExtractor {
    private final FileService fileService;

    public String extract(SysFile file) throws IOException {
        String extension = file.getFileExt().toLowerCase(Locale.ROOT);
        String text;
        try (InputStream raw = fileService.get(file.getStoragePath());
             BufferedInputStream input = new BufferedInputStream(raw)) {
            text = switch (extension) {
                case "md", "txt" -> extractUtf8Text(input);
                case "pdf" -> extractPdf(input);
                case "docx" -> extractDocx(input);
                case "pptx" -> extractPptx(input);
                default -> throw new BusinessException("不支持解析该文件类型");
            };
        }
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            throw new BusinessException("未从文件中提取到可总结的文字内容");
        }
        return normalized;
    }

    private String extractUtf8Text(InputStream input) throws IOException {
        String value = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        return value.startsWith("\uFEFF") ? value.substring(1) : value;
    }

    private String extractPdf(BufferedInputStream input) throws IOException {
        requireSignature(input, new byte[] {'%', 'P', 'D', 'F', '-'}, "文件内容不是有效的 PDF");
        try (PDDocument document = PDDocument.load(input)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractDocx(BufferedInputStream input) throws IOException {
        requireZipSignature(input, "文件内容不是有效的 docx");
        try (XWPFDocument document = new XWPFDocument(input);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractPptx(BufferedInputStream input) throws IOException {
        requireZipSignature(input, "文件内容不是有效的 pptx");
        try (XMLSlideShow slides = new XMLSlideShow(input);
             SlideShowExtractor<XSLFShape, XSLFTextParagraph> extractor = new SlideShowExtractor<>(slides)) {
            extractor.setNotesByDefault(true);
            return extractor.getText();
        }
    }

    private void requireZipSignature(BufferedInputStream input, String message) throws IOException {
        requireSignature(input, new byte[] {'P', 'K'}, message);
    }

    private void requireSignature(BufferedInputStream input, byte[] expected, String message) throws IOException {
        input.mark(expected.length + 1);
        byte[] actual = input.readNBytes(expected.length);
        input.reset();
        if (actual.length != expected.length) {
            throw new BusinessException(message);
        }
        for (int index = 0; index < expected.length; index++) {
            if (actual[index] != expected[index]) {
                throw new BusinessException(message);
            }
        }
    }

    private String normalize(String text) {
        return text
            .replace("\u0000", "")
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replaceAll("[\\t\\x0B\\f]+", " ")
            .replaceAll("\n{4,}", "\n\n\n")
            .trim();
    }
}
