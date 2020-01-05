package org.util;

import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.myEntryStrategies.KeltnerEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitTest {
    public static void main(String[] args) throws Exception {

        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD_3MONTH.csv","yyyy.MM.dd HH:mm");
        Strategy strategy=new KeltnerEntry();

        String packages[] = strategy.toString().split("\\.");
        String path = System.getProperty("user.dir") + "\\ta4j-core\\src\\main\\java";
        for (int i = 0; i < packages.length - 1; i++) path += "\\" + packages[i];
//        File file = new File(path+"\\"+strategyElement.getClass().getSimpleName()+".java'");
        String data = "";

        try {
            Stream<String> lines = Files.lines(Paths.get(path + "\\" + strategy.getClass().getSimpleName() + ".java"));
            data = lines.collect(Collectors.joining("\n"));
            lines.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(data);
    }
}
