package com.kamennova.lala.endpoint;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyFileUpload implements org.apache.commons.fileupload.UploadContext {
    private final InputStream postBody;
    private final String stringBody;
    private final String boundary;
    private final long contentLength;
    private final Map<String, String> parameters = new HashMap<>();

    public MyFileUpload(InputStream postBody, String filePath) throws Exception {
        this.postBody = postBody;
        byte [] data = postBody.readAllBytes();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }

        this.stringBody = new String(postBody.readAllBytes());
        System.out.println(this.stringBody);

        String temp = stringBody;
        this.boundary = temp.substring(2, temp.indexOf('\n')).trim();
        this.contentLength = temp.length();
        final FileItemFactory factory = new DiskFileItemFactory();
        FileUpload upload = new FileUpload(factory);

        List<FileItem> fileItems = upload.parseRequest(this);
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField()) {
                parameters.put(fileItem.getFieldName(), fileItem.getString());
            } else {
                System.out.println(fileItem.getContentType());
                if (!fileItem.getContentType().equals("audio/fmpeg") && fileItem.getSize() < 1000000) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                    writer.write(fileItem.getString());
                    writer.close();
                    parameters.put("recording", filePath);
                }
            }
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    // The methods below here are to implement the UploadContext interface.
    @Override
    public String getCharacterEncoding() {
        return "UTF-8"; // You should know the actual encoding.
    }

    // This is the deprecated method from RequestContext that unnecessarily
    // limits the length of the content to ~2GB by returning an int. 
    @Override
    public int getContentLength() {
        return -1; // Don't use this
    }

    @Override
    public String getContentType() {
        return "multipart/form-data, boundary=" + this.boundary;
    }

    @Override
    public InputStream getInputStream() {
        return postBody;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }
}