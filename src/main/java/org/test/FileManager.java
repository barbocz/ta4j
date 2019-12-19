package org.test;


import java.io.*;

import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileManager {




    public static int getLineNumber(String filePath) {
        // megszámolja, hogy hány sor van az adott file-ban
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(filePath));
            try {
                byte[] c = new byte[1024];
                int count = 0;
                int readChars = 0;
                boolean empty = true;
                while ((readChars = is.read(c)) != -1) {
                    empty = false;
                    for (int i = 0; i < readChars; ++i) {
                        if (c[i] == '\n') {
                            ++count;
                        }
                    }
                }
                return ((count == 0 && !empty) ? 1 : count);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (0);
    }

    public static void copyFiles(String sourcePath, String targetPath) throws IOException {

        String targetDirName=targetPath.substring(0,targetPath.lastIndexOf("\\")+1);
        File dir=new File(targetDirName);
        if (!dir.exists()) dir.mkdir();

        File sourceDir = new File(sourcePath);
        if (!sourceDir.exists()) throw new IOException();

//        File targetDir = new File(targetPath);
//        if (!targetDir.exists()) throw new Exception("Source folder " + sourcePath + " not exists");


        File source = new File(sourcePath);

        File target = new File(targetPath);
        Files.copy(source.toPath(), target.toPath(), REPLACE_EXISTING);

    }

    public static void exportToCsv(String fileName,StringBuffer content) throws Exception{
        BufferedWriter exportFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
        exportFile.write(content.toString());
        exportFile.close();
    }

    public static List<String> importCsvToList(String fileName) throws Exception{
        BufferedReader importFile = new BufferedReader(new FileReader(fileName));
        String line;
        List<String> list=new ArrayList();
        while ((line = importFile.readLine()) != null) {
            if (!list.contains(line)) list.add(line);

        }
        importFile.close();
        return list;
    }

    public static HashMap<String,String[]> importCsvToHashMap(String fileName,String regex) throws Exception{
        BufferedReader importFile = new BufferedReader(new FileReader(fileName));
        String line;
        HashMap<String,String[]> list=new HashMap<>();
        while ((line = importFile.readLine()) != null) {
            String part[]= line.split(regex);
            if (!list.containsKey(part[0])) list.put(part[0],part);
        }
        importFile.close();
        return list;
    }



}
