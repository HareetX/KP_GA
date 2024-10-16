package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestData {
    private String file_path;

    int bestT;// 最佳出现代数
    int bestLength; // 最佳编码价值
    int[] bestTour; // 最佳编码
    int[] bestBackpack; // 最佳编码对应的背包重量
    int t;
    long totalTime_ms;

    public void readTestData(String f_path) throws IOException {
        file_path = f_path;
        FileReader reader = new FileReader(file_path);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line=br.readLine()) != null) {
            if (line.equals("最佳编码出现代数：")) {
                line = br.readLine();
                bestT = Integer.valueOf(line);
            } else if (line.equals("最佳编码价值：")) {
                line = br.readLine();
                bestLength = Integer.valueOf(line);
            } else if (line.equals("最佳编码：")) {
                line = br.readLine();
                String[] tmp_tour = line.split(",");
                bestTour = new int[tmp_tour.length];
                for (int i = 0; i < tmp_tour.length; i++) {
                    bestTour[i] = Integer.valueOf(tmp_tour[i]);
                }
            } else if (line.equals("最佳编码重量：")) {
                line = br.readLine();
                String[] tmp_backpack = line.split(" ");
                bestBackpack = new int[tmp_backpack.length];
                for (int i = 0; i < tmp_backpack.length; i++) {
                    bestBackpack[i] = Integer.valueOf(tmp_backpack[i]);
                }
            } else if (line.equals("执行代数：")) {
                line = br.readLine();
                t = Integer.valueOf(line);
            } else if (line.equals("执行时间：")) {
                line = br.readLine();
                totalTime_ms = Integer.valueOf(line);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        TestData td = new TestData();
        td.readTestData("logs/GA/Middle/log0_OR10x250-0.25_1.dat.txt");
    }
}
