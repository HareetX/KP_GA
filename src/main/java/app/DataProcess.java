package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataProcess {
    TestData[] test_data_list;

    private int avg_bestT;// 最佳出现代数
    private double avgLength; // 最佳编码价值
    private int avg_t;
    private long avg_totalTime_ms;

    private TestData bestTestData;
    private int best_id;

    private boolean flag;

    public void loadTestData(String dir, String test_case, int start, int end) throws IOException {
        test_data_list = new TestData[end - start + 1];
        for (int i = start; i <= end; i++) {
            String filename = "log" + i + "_" + test_case + ".txt";
            test_data_list[i - start] = new TestData();
            test_data_list[i - start].readTestData(dir + filename);
        }
    }

    public int loadTestData(String dir, String test_case, int start, int end, int exp_id) throws IOException {
        test_data_list = new TestData[end - start + 1];
        int i;
        for (i = start; i <= end; i++) {
            String filename = "log" + i + "_" + exp_id + "_" + test_case + ".txt";
            File f = new File(dir + filename);
            if (f.isFile()) {
                test_data_list[i - start] = new TestData();
                test_data_list[i - start].readTestData(dir + filename);
            } else {
                return i;
            }
        }
        return i;
    }

    public void dataProcess() {
        if (test_data_list[0] != null) {
            bestTestData = test_data_list[0];
            best_id = 0;
            int sum_bestT = test_data_list[0].bestT;
            int sumLength = test_data_list[0].bestLength;
            int sum_t = test_data_list[0].t;
            long sum_totalTime_ms = test_data_list[0].totalTime_ms;
            int data_num = test_data_list.length;
            for (int i = 1; i < test_data_list.length; i++) {
                if (test_data_list[i] != null) {
                    if (bestTestData.bestLength < test_data_list[i].bestLength) {
                        bestTestData = test_data_list[i];
                        best_id = i;
                    }
                    sum_bestT += test_data_list[i].bestT;
                    sumLength += test_data_list[i].bestLength;
                    sum_t += test_data_list[i].t;
                    sum_totalTime_ms += test_data_list[i].totalTime_ms;
                } else {
                    data_num = i;
                    break;
                }
            }
            avg_bestT = sum_bestT / data_num;
            avgLength = (double) sumLength / data_num;
            avg_t = sum_t / data_num;
            avg_totalTime_ms = sum_totalTime_ms / data_num;

            flag = true;
        } else {
            flag = false;
        }
    }

    public void saveDataProcess(String dir, String filename) throws IOException {
        if (flag) {
            File log_dir = new File(dir);
            if (!(log_dir.exists())) {
                log_dir.mkdir();
            }
            File file = new File(dir + filename);
            //如果文件不存在，创建文件
            if (!file.exists())
                file.createNewFile();
            //创建FileWriter对象
            FileWriter writer = new FileWriter(file);

            //向文件中写入avg
            writer.write("平均最佳编码出现代数：\n" + avg_bestT + "\n");

            writer.write("平均编码价值：\n" + avgLength + "\n");

            writer.write("平均执行代数：\n" + avg_t + "\n");

            writer.write("平均执行时间：\n" + avg_totalTime_ms + "\n");

            //向文件中写入best
            writer.write("最佳测试结果" + best_id + " 如下：\n");

            writer.write("最佳编码出现代数：\n" + bestTestData.bestT + "\n");

            writer.write("最佳编码价值：\n" + bestTestData.bestLength + "\n");

            writer.write("最佳编码：\n");
            for (int i = 0; i < bestTestData.bestTour.length; i++) {
                //向文件中写入内容
                writer.write(bestTestData.bestTour[i] + ",");
            }
            writer.write("\n");

            writer.write("最佳编码重量：\n");
            for (int i = 0; i < bestTestData.bestBackpack.length; i++) {
                //向文件中写入内容
                writer.write(bestTestData.bestBackpack[i] + " ");
            }

            writer.write("\n执行代数：\n" + bestTestData.t + "\n");

            writer.write("执行时间：\n" + bestTestData.totalTime_ms + "\n");

            writer.flush();
            writer.close();
        }
    }

    public void GA_R_DataProcess() throws IOException {
        DataProcess dp = new DataProcess();
        for (int i = 0; i < 25; i++) {
            dp.loadTestData("logs/GA/Large/", "OR30x250-0.25_1.dat", i*50+173, i*50+49+173);
            dp.dataProcess();
            dp.saveDataProcess("logs/GA/Large/", "test"+(i+1)+".txt");
            //System.out.println(dp.avgLength);
        }
    }

    public void GA_RO_DataProcess() throws IOException {
        DataProcess dp = new DataProcess();
        for (int i = 0; i < 11; i++) {
            dp.loadTestData("logs/GA1/Small/", "OR5x250-0.25_1.dat", i*50, i*50+49);
            dp.dataProcess();
            dp.saveDataProcess("logs/GA1/Small/", "test"+(i+1)+".txt");
            System.out.println(dp.bestTestData.bestLength);//dp.avgLength);
        }
    }

    public void GA_ROL_DataProcess() throws IOException {
        DataProcess dp = new DataProcess();
        for (int i = 0; i < 5; i++) {
            dp.loadTestData("logs/GA2/Large/", "OR30x250-0.25_1.dat", i*50, i*50+49);
            dp.dataProcess();
            dp.saveDataProcess("logs/GA2/Large/", "test"+(i+1)+".txt");
            System.out.println(dp.bestTestData.bestLength);//dp.avgLength);
        }
    }

    public void GA_ROSA_DataProcess() throws IOException {
        DataProcess dp = new DataProcess();
        int start = 0;
        for (int i = 0; i < 25; i++) {
            start = dp.loadTestData("logs/OR5x250-0.25_1/", "OR5x250-0.25_1.dat", start, start+49, i);
            dp.dataProcess();
            dp.saveDataProcess("logs/OR5x250-0.25_1/", "test"+(i+1)+".txt");
            if (dp.flag) {
                System.out.println((i+1) + " " + dp.bestTestData.bestLength);//dp.avgLength);
            } else {
                System.out.println(i+1);
            }
        }
    }

    public void IGA_scale() throws IOException {
        DataProcess dp = new DataProcess();
        for (int i = 0; i < 10; i++) {
            dp.loadTestData("logs/IGA_scale/OR30x250-0.25_1/", "OR30x250-0.25_1.dat", i*50, i*50+49);
            dp.dataProcess();
            dp.saveDataProcess("logs/IGA_scale/OR30x250-0.25_1/", "test"+(i+1)+".txt");
            System.out.println(dp.avg_t);//dp.avgLength//dp.bestTestData.bestLength);
        }
    }

    public void IGA_Pcm() throws IOException {
        DataProcess dp = new DataProcess();
        for (int i = 0; i < 5; i++) {
            dp.loadTestData("logs/IGA_Pm/OR30x250-0.25_1/", "OR30x250-0.25_1.dat", i*50, i*50+49);
            dp.dataProcess();
            dp.saveDataProcess("logs/IGA_Pm/OR30x250-0.25_1/", "test"+(i+1)+".txt");
            System.out.println(dp.avgLength);//dp.avgLength//dp.bestTestData.bestLength);
        }
    }

    public void IGA_genetic() throws IOException {
        DataProcess dp = new DataProcess();
        for (int i = 0; i < 3; i++) {
            dp.loadTestData("logs/IGA_select/OR5x250-0.25_1/", "OR5x250-0.25_1.dat", i*50, i*50+49);
            dp.dataProcess();
            dp.saveDataProcess("logs/IGA_select/OR5x250-0.25_1/", "test"+(i+1)+".txt");
            System.out.println(dp.avgLength);//dp.avgLength//dp.bestTestData.bestLength);
        }
    }

    public void IGA_Ph() throws IOException {
        DataProcess dp = new DataProcess();
        String[] d_list = {"5", "10", "30"};
        String[] n_list = {"100", "250", "500"};
        String[] r_list = {"0.25", "0.50", "0.75"};
        String[] i_list = {"1", "10"};
        int file_num = d_list.length * n_list.length * r_list.length * i_list.length;
        String[] dir_path_list = new String[file_num];
        String[] file_name_list = new String[file_num];
        int k = 0;
        for (String d : d_list) {
            for (String n : n_list) {
                for (String r : r_list) {
                    for (String i : i_list) {
                        dir_path_list[k] = "logs/IGA_RO/OR"+d+"x"+n+"-"+r+"_"+i+"/";
                        file_name_list[k] = "OR"+d+"x"+n+"-"+r+"_"+i+".dat";
                        k++;
                    }
                }
            }
        }
        for (int j = 0; j < file_num; j++) {
            dp.loadTestData(dir_path_list[j], file_name_list[j], 0, 10);
            dp.dataProcess();
            dp.saveDataProcess(dir_path_list[j], "test" + 1 + ".txt");
            System.out.println(dp.best_id);//dp.avgLength//dp.bestTestData.bestLength);
        }
    }

    public static void main(String[] args) throws IOException {
        DataProcess dp = new DataProcess();
        String[] d_list = {"10"};
        String[] n_list = {"100", "250", "500"};
        String[] r_list = {"0.25", "0.50", "0.75"};
        String[] i_list = {"1", "10"};
        int file_num = d_list.length * n_list.length * r_list.length * i_list.length;
        String[] dir_path_list = new String[file_num];
        String[] file_name_list = new String[file_num];
        int k = 0;
        for (String d : d_list) {
            for (String n : n_list) {
                for (String r : r_list) {
                    for (String i : i_list) {
                        dir_path_list[k] = "logs/OR"+d+"x"+n+"-"+r+"_"+i+"/";
                        file_name_list[k] = "OR"+d+"x"+n+"-"+r+"_"+i+".dat";
                        k++;
                    }
                }
            }
        }

        double[][] df = new double[file_num][2*3];
        for (int j = 0; j < file_num; j++) {
            for (int i = 0; i < 3; i++) {
                dp.loadTestData(dir_path_list[j], file_name_list[j], i*50, i*50+49);
                dp.dataProcess();
                dp.saveDataProcess(dir_path_list[j], "test" + (i+1) + ".txt");
                df[j][2*i] = dp.bestTestData.bestLength;
                df[j][2*i+1] = dp.avgLength;
            }
        }
        System.out.println(df);
    }
}
