package com.docintel.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {

    public String storeFile(MultipartFile file){
        String filename = file.getOriginalFilename();
        System.out.println("storing file: " +filename);
        return "stored file: " +filename;
    }

}
