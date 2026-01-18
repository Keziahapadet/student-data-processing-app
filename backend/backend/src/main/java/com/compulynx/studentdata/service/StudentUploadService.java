package com.compulynx.studentdata.service;

import com.compulynx.studentdata.model.Student;
import com.compulynx.studentdata.repository.StudentRepository;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class StudentUploadService {

    private static final int PIPE_BUFFER_SIZE = 512 * 1024; // 512KB pipe buffer
    private static final int WRITE_BUFFER_SIZE = 128 * 1024; // 128KB write buffer

    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public StudentUploadService(StudentRepository studentRepository, JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.studentRepository = studentRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Transactional
    public int uploadCsv(MultipartFile file) throws IOException {
        AtomicInteger insertedCount = new AtomicInteger(0);
        AtomicReference<Exception> writerException = new AtomicReference<>();

        try (PipedInputStream pipeIn = new PipedInputStream(PIPE_BUFFER_SIZE);
             PipedOutputStream pipeOut = new PipedOutputStream(pipeIn)) {

            // Writer thread - reads CSV and writes transformed data to pipe
            Thread writerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), WRITE_BUFFER_SIZE);
                     Writer writer = new OutputStreamWriter(pipeOut, StandardCharsets.UTF_8)) {

                    StringBuilder batch = new StringBuilder(WRITE_BUFFER_SIZE);
                    String line;
                    boolean headerSkipped = false;
                    int count = 0;

                    while ((line = reader.readLine()) != null) {
                        if (!headerSkipped) {
                            headerSkipped = true;
                            continue;
                        }

                        String[] tokens = line.split(",", -1);
                        if (tokens.length < 6) {
                            continue;
                        }

                        // Transform and append to batch
                        appendTransformedRecord(batch, tokens);
                        count++;

                        // Flush batch periodically
                        if (count % 5000 == 0) {
                            writer.write(batch.toString());
                            batch.setLength(0);
                        }
                    }

                    // Write remaining records
                    if (batch.length() > 0) {
                        writer.write(batch.toString());
                    }

                    insertedCount.set(count);

                } catch (Exception e) {
                    writerException.set(e);
                }
            });

            writerThread.start();

            // Main thread - executes COPY command
            try (Connection connection = dataSource.getConnection()) {
                CopyManager copyManager = connection.unwrap(PGConnection.class).getCopyAPI();

                String copyCommand = """
                    COPY students (student_id, first_name, last_name, dob, class, score)
                    FROM STDIN WITH (FORMAT csv, NULL '')
                    """;

                copyManager.copyIn(copyCommand, pipeIn);
            }

            // Wait for writer to complete
            writerThread.join();

            // Check for writer exceptions
            if (writerException.get() != null) {
                throw new IOException("Error processing CSV", writerException.get());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        } catch (Exception e) {
            // If COPY fails, fall back to batch insert
            return fallbackBatchInsert(file);
        }

        return insertedCount.get();
    }

    private void appendTransformedRecord(StringBuilder batch, String[] tokens) {
        String studentId = tokens[0].trim();
        String firstName = tokens[1].trim();
        String lastName = tokens[2].trim();
        String dob = tokens[3].trim();
        String studentClass = tokens[4].trim();
        int baseScore = Integer.parseInt(tokens[5].trim());
        int transformedScore = baseScore + 5;

        batch.append(studentId).append(',');
        batch.append(firstName).append(',');
        batch.append(lastName).append(',');
        batch.append(dob).append(',');
        batch.append(studentClass).append(',');
        batch.append(transformedScore).append('\n');
    }

    private int fallbackBatchInsert(MultipartFile file) throws IOException {
        int inserted = 0;
        String sql = """
            INSERT INTO students (student_id, first_name, last_name, dob, class, score)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (student_id) DO UPDATE SET
                first_name = EXCLUDED.first_name,
                last_name = EXCLUDED.last_name,
                dob = EXCLUDED.dob,
                class = EXCLUDED.class,
                score = EXCLUDED.score
            """;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] tokens = line.split(",", -1);
                if (tokens.length < 6) {
                    continue;
                }

                Long studentId = Long.parseLong(tokens[0].trim());
                String firstName = tokens[1].trim();
                String lastName = tokens[2].trim();
                LocalDate dob = LocalDate.parse(tokens[3].trim());
                String studentClass = tokens[4].trim();
                int score = Integer.parseInt(tokens[5].trim()) + 5;

                jdbcTemplate.update(sql, studentId, firstName, lastName, dob, studentClass, score);
                inserted++;
            }
        }

        return inserted;
    }
}
