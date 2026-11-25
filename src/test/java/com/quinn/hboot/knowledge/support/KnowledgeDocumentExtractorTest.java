package com.quinn.hboot.knowledge.support;

import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.service.FileService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeDocumentExtractorTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private KnowledgeDocumentExtractor extractor;

    @Test
    void extractsTextCsvAndPdf() throws Exception {
        assertExtracted("md", "# 标题\nMarkdown 正文".getBytes(StandardCharsets.UTF_8), "Markdown 正文");
        assertExtracted("txt", "纯文本正文".getBytes(StandardCharsets.UTF_8), "纯文本正文");
        assertExtracted("csv", "姓名,内容\n张三,CSV 正文".getBytes(StandardCharsets.UTF_8), "CSV 正文");
        assertExtracted("pdf", pdf("PDF content"), "PDF content");
    }

    @Test
    void extractsWordPowerPointAndExcelFormats() throws Exception {
        assertExtracted("docx", docx("DOCX content"), "DOCX content");
        assertExtracted("ppt", ppt("PPT content"), "PPT content");
        assertExtracted("pptx", pptx("PPTX content"), "PPTX content");
        assertExtracted("xls", xls("XLS content"), "XLS content");
        assertExtracted("xlsx", xlsx("XLSX content"), "XLSX content");
    }

    private void assertExtracted(String extension, byte[] content, String expected) throws Exception {
        SysFile file = new SysFile();
        file.setFileExt(extension);
        file.setStoragePath("sample." + extension);
        when(fileService.get(file.getStoragePath())).thenReturn(new ByteArrayInputStream(content));

        String extracted = extractor.extract(file);

        assertTrue(extracted.contains(expected));
    }

    private byte[] pdf(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA, 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] docx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(text);
            document.write(output);
            return output.toByteArray();
        }
    }

    private byte[] ppt(String text) throws Exception {
        try (HSLFSlideShow presentation = new HSLFSlideShow();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            HSLFSlide slide = presentation.createSlide();
            HSLFTextBox box = new HSLFTextBox();
            box.setText(text);
            slide.addShape(box);
            presentation.write(output);
            return output.toByteArray();
        }
    }

    private byte[] pptx(String text) throws Exception {
        try (XMLSlideShow presentation = new XMLSlideShow();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSLFSlide slide = presentation.createSlide();
            XSLFTextBox box = slide.createTextBox();
            box.setText(text);
            presentation.write(output);
            return output.toByteArray();
        }
    }

    private byte[] xls(String text) throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet("Sheet1").createRow(0).createCell(0).setCellValue(text);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private byte[] xlsx(String text) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet("Sheet1").createRow(0).createCell(0).setCellValue(text);
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
