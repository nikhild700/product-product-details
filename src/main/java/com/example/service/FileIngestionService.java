package com.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.dto.CreateProductDTO;

public class FileIngestionService {

    private final ExecutorService executor;
    private final ProductService productService;

    public FileIngestionService(ProductService productService) {
        this.executor = Executors.newFixedThreadPool(10);
        this.productService = productService;
    }

    private List<List<CreateProductDTO>> splitIntoChunks(Path path, int chunkSize) throws IOException {
        List<List<CreateProductDTO>> chunks = new ArrayList<>();
        List<CreateProductDTO> current = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                current.add(new CreateProductDTO(
                        parts[0], parts[1], parts[2], Double.parseDouble(parts[3])));

                if (current.size() == chunkSize) {
                    chunks.add(current);
                    current = new ArrayList<>();
                }
            }

            if (!current.isEmpty()) {
                chunks.add(current);
            }
        }

        return chunks;
    }

    public void processFileMultiThread() throws Exception {
        Path path = Paths.get("data/products.txt");

        List<List<CreateProductDTO>> chunks = splitIntoChunks(path, 10000);

        for (List<CreateProductDTO> chunk : chunks) {
            executor.submit(() -> {
                try {
                    this.productService.insertBatch(chunk);
                    System.out.println("Inserted chunk of size: " + chunk.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
