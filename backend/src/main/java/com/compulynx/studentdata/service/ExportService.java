package com.compulynx.studentdata.service;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
public class ExportService {

    private final StudentRepository studentRepository;

    public ExportService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public byte[] exportExcel() throws IOException {
        List<Student> students = studentRepository.findAll();
        try (Workbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");
            createHeader(sheet);
            int rowIdx = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getDob() != null ? student.getDob().toString() : "");
                row.createCell(4).setCellValue(student.getStudentClass());
                row.createCell(5).setCellValue(student.getScore());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportCsv() throws IOException {
        List<Student> students = studentRepository.findAll();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            String[] header = {"studentId", "firstName", "lastName", "dob", "class", "score"};
            csvWriter.writeNext(header, false);
            for (Student student : students) {
                csvWriter.writeNext(new String[]{
                        String.valueOf(student.getStudentId()),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDob() != null ? student.getDob().toString() : "",
                        student.getStudentClass(),
                        student.getScore() != null ? student.getScore().toString() : ""
                }, false);
            }
            csvWriter.flush();
            return out.toByteArray();
        }
    }

    public byte[] exportPdf() throws IOException {
        List<Student> students = studentRepository.findAll();
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float y = writeTitle(contentStream, page);
            for (Student student : students) {
                if (y < 50) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    y = writeTitle(contentStream, page);
                }
                contentStream.beginText();
                contentStream.newLineAtOffset(40, y);
                contentStream.showText(formatStudent(student));
                contentStream.endText();
                y -= 14;
            }
            contentStream.close();
            document.save(out);
            return out.toByteArray();
        }
    }

    private String formatStudent(Student student) {
        return String.format("%d | %s %s | %s | %s | %s",
                student.getStudentId(),
                student.getFirstName(),
                student.getLastName(),
                student.getDob() != null ? student.getDob() : "",
                student.getStudentClass(),
                student.getScore());
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("studentId");
        header.createCell(1).setCellValue("firstName");
        header.createCell(2).setCellValue("lastName");
        header.createCell(3).setCellValue("dob");
        header.createCell(4).setCellValue("class");
        header.createCell(5).setCellValue("score");
    }

    private float writeTitle(PDPageContentStream contentStream, PDPage page) throws IOException {
        float y = page.getMediaBox().getHeight() - 40;
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(40, y);
        contentStream.showText("Student Report");
        contentStream.endText();
        y -= 20;
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        return y;
    }
}
