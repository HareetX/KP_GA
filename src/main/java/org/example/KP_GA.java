package org.example;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

public class KP_GA {

    private int[] v =
            { 220, 208, 198, 192, 180, 180, 165, 162, 160, 158,
              155, 130, 125, 122, 120, 118, 115, 110, 105, 101,
              100, 100, 98,  96,  95,  90,  88,  82,  80,  77,
              75,  73,  72,  70,  69,  66,  65,  63,  60,  58,
              56,  50,  30,  20,  15,  10,  8,   5,   3,   1};// 物品价值
    private int[][] b =
            {{ 80, 82, 85, 70, 72, 70, 66, 50, 55, 25,
               50, 55, 40, 48, 50, 32, 22, 60, 30, 32,
               40, 38, 35, 32, 25, 28, 30, 22, 50, 30,
               45, 30, 60, 50, 20, 65, 20, 25, 30, 10,
               20, 25, 15, 10, 10, 10, 4,  4,  2,  1},
             { 80, 82, 85, 70, 72, 70, 66, 50, 55, 25,
               50, 55, 40, 48, 50, 32, 22, 60, 30, 32,
               40, 38, 35, 32, 25, 28, 30, 22, 50, 30,
               45, 30, 60, 50, 20, 65, 20, 25, 30, 10,
               20, 25, 15, 10, 10, 10,  4,  4,  2, 1}};// 物品体积
    private int[] pb = {1000, 1000};// 背包容积

    private double[] efficiency;

    private int[] HD;

    private int[] HV;

    private int LL; // 染色体长度

    private int dimension; // 维度
    private int scale;// 种群规模
    private int MAX_GEN; // 运行代数

    private int bestT;// 最佳出现代数
    private int bestLength; // 最佳编码价值
    private int[] bestTour; // 最佳编码
    private int[] bestBackpack; // 最佳编码对应的背包重量

    // 初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
    private int[][] oldPopulation;
    private int[][] newPopulation;// 新的种群，子代种群
    private int[] fitness;// 种群适应度，表示种群中各个个体的适应度
    private int[][] backpack;// 种群背包重量

    private float[] Pi;// 种群中各个个体的累计概率
    private float Pc;// 交叉概率
    private float Pm;// 变异概率
    private int t;// 当前代数
    private Random random;

    private String test_case = null;

    // 种群规模，染色体长度,最大代数，交叉率，变异率
    public KP_GA(int s, int l, int d, int g, float c, float m) {
        scale = s;
        LL = l;
        dimension = d;
        MAX_GEN = g;
        Pc = c;
        Pm = m;
    }

    public KP_GA(int s, TestCase tc, int g, float c, float m, String test_case_name) {
        scale = s;
        LL = tc.LL;
        dimension = tc.dimension;
        v = tc.v;
        b = tc.b;
        pb = tc.pb;
        MAX_GEN = g;
        Pc = c;
        Pm = m;
        test_case = test_case_name;
    }

    private static int[] descendingSortedIndexes(double[] array) {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer index1, Integer index2) {
                // 根据数组的值进行降序排序
                return Double.compare(array[index2], array[index1]);
            }
        });

        // 将Integer数组转换为int数组
        int[] sortedIndexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            sortedIndexes[i] = indexes[i];
        }

        return sortedIndexes;
    }

    private void init() {
        bestLength = 0;
        bestTour = new int[LL];
        bestT = 0;
        bestBackpack = new int[dimension];
        t = 0;

        newPopulation = new int[scale][LL];
        oldPopulation = new int[scale][LL];
        backpack = new int[scale][dimension];
        fitness = new int[scale];
        Pi = new float[scale];

        random = new Random(System.currentTimeMillis());

        efficiency = new double[LL];
        for (int i = 0; i < LL; i++) {
            int sum_b_i = 0;
            for (int j = 0; j < dimension; j++) {
                sum_b_i += b[j][i];
            }
            efficiency[i] = ((double) v[i]) / ((double) sum_b_i);
        }

        HD = descendingSortedIndexes(efficiency);

        //HV = descendingSortedIndexes(v);
    }

    // 初始化种群
    void initGroup() {
        int k, i;
        for (k = 0; k < scale; k++)// 种群数
        {
            // 01编码
            for (i = 0; i < LL; i++) {
                oldPopulation[k][i] = random.nextInt(65535) % 2;
            }
        }
    }

    void op_repair(int[] chromosome) {
        // 修复不可行解个体chromosome为可行解
        int[] tmp_chromosome = new int[LL];
        for (int i = 0; i < LL; i++) {
            tmp_chromosome[i] = 0;
        }

        int[] bb = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            bb[i] = 0;
        }
        boolean st = true;  // 未超出背包容量的标志
        for (int i = 0; i < LL; i++) {
            // 按HD顺序遍历物品
            int index = HD[i];
            if(chromosome[index] == 1) {
                // chromosome已选中第index个物品
                for (int j = 0; j < dimension; j++) {
                    bb[j] += b[j][index];
                    if (bb[j] > pb[j]) {
                        st = false;
                        break;
                    }
                }
                if (!st) {
                    break;
                } else {
                    tmp_chromosome[index] = 1;
                }
            }
        }

        // 更新chromosome
        for (int i = 0; i < LL; i++) {
            chromosome[i] = tmp_chromosome[i];
        }
    }

    void op_optimize(int[] chromosome, int[] bb) {
        // 优化可行解个体chromosome，使其充分利用背包资源
        for (int i = 0; i < LL; i++) {
            // 按HD顺序遍历物品
            int index = HD[i];
            if(chromosome[index] == 0) {
                // chromosome未选中第index个物品
                boolean st = true;  // 选择第index个物品后，仍未超出背包容量的标志
                for (int j = 0; j < dimension; j++) {
                    if (bb[j] + b[j][index] > pb[j]) {
                        st = false;
                        break;
                    }
                }
                if (st) {
                    for (int j = 0; j < dimension; j++) {
                        bb[j] += b[j][index];
                    }
                    chromosome[index] = 1;
                }
            }
        }
    }

    public int evaluate(int[] chromosome, int[] backpack) {
        // chromosome对应的总价值vv和总体积bb
        int vv = 0;
        int[] bb = new int[dimension];
        for (int j = 0; j < dimension; j++) {
            bb[j] = 0;
        }

        boolean st = true;
        for (int i = 0; i < LL; i++) {
            if (chromosome[i] == 1) {
                vv += v[i];
                for (int j = 0; j < dimension; j++) {
                    bb[j] += b[j][i];
                    if (bb[j] > pb[j]) {
                        st = false;
                        break;
                    }
                }
                if (!st) {
                    break;
                }
            }
        }
        if (!st) {
            // 超出背包体积
            for (int i = 0; i < dimension; i++) {
                backpack[i] = 0;
            }
            return 0;
        } else {
            for (int i = 0; i < dimension; i++) {
                backpack[i] = bb[i];
            }
            return vv;
        }
    }

    // 计算种群中各个个体的累积概率，前提是已经计算出各个个体的适应度fitness[max]，作为赌轮选择策略一部分，Pi[max]
    void countRate() {
        int k;
        double sumFitness = 0;// 适应度总和

        int[] tempf = new int[scale];

        for (k = 0; k < scale; k++) {
            tempf[k] = fitness[k];
            sumFitness += tempf[k];
        }

        if (sumFitness != 0) {
            Pi[0] = (float) (tempf[0] / sumFitness);
            for (k = 1; k < scale; k++) {
                Pi[k] = (float) (tempf[k] / sumFitness + Pi[k - 1]);
            }
        }
        else {
            Pi[0] = (float) (1. / scale);
            for (k = 1; k < scale; k++) {
                Pi[k] = (float) (1. / scale + Pi[k - 1]);
            }
        }
    }

    // 挑选某代种群中适应度最高的个体，直接复制到子代中
    // 前提是已经计算出各个个体的适应度Fitness[max]
    public void selectBestGh() {
        int k, i, maxid;
        int maxevaluation;

        maxid = 0;
        maxevaluation = fitness[0];
        for (k = 1; k < scale; k++) {
            if (maxevaluation < fitness[k]) {
                maxevaluation = fitness[k];
                maxid = k;
            }
        }

        if (bestLength < maxevaluation) {
            bestLength = maxevaluation;
            bestT = t;// 最好的染色体出现的代数;
            for (i = 0; i < LL; i++) {
                bestTour[i] = oldPopulation[maxid][i];
            }
            for (i = 0; i < dimension; i++) {
                bestBackpack[i] = backpack[maxid][i];
            }
        }

        // 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
        copyGh(0, maxid);// 将当代种群中适应度最高的染色体k复制到新种群中，排在第一位0
    }

    // 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
    public void copyGh(int k, int kk) {
        int i;
        for (i = 0; i < LL; i++) {
            newPopulation[k][i] = oldPopulation[kk][i];
        }
    }

    // 赌轮选择策略挑选
    public void select() {
        int k, i, selectId;
        float ran1;
        for (k = 1; k < scale; k++) {
            ran1 = (float) (random.nextInt(65535) % 1000 / 1000.0);
            // System.out.println("概率"+ran1);
            // 产生方式
            for (i = 0; i < scale; i++) {
                if (ran1 <= Pi[i]) {
                    break;
                }
            }
            selectId = i;
            copyGh(k, selectId);
        }
    }

    public void evolution() {
        int k;
        // 挑选某代种群中适应度最高的个体
        selectBestGh();
        // 赌轮选择策略挑选scale-1个下一代个体
        select();
        float r;

        // 交叉方法
        for (k = 0; k < scale; k = k + 1) {
            r = random.nextFloat();// 产生概率
            // System.out.println("交叉率..." + r);
            if (r < Pc) {
                int k_cross = random.nextInt(65535) % scale;
                while (k_cross == k) {
                    k_cross = random.nextInt(65535) % scale;
                }
                // System.out.println(k + "与" + k_cross + "进行交叉...");
                OXCross(k, k_cross);// 进行交叉
            }
        }

        // 变异方法
        for (k = 0; k < scale; k = k + 1) {
            r = random.nextFloat();// 产生概率
            // System.out.println("变异率..." + r);
            if (r < Pm) {
                // System.out.println(k + "变异...");
                OnCVariation(k);
            }
        }
    }


    // 两点交叉算子
    void OXCross(int k1, int k2) {
        int i, j, flag;
        int ran1, ran2, temp = 0;

        ran1 = random.nextInt(65535) % LL;
        ran2 = random.nextInt(65535) % LL;

        while (ran1 == ran2) {
            ran2 = random.nextInt(65535) % LL;
        }
        if (ran1 > ran2)// 确保ran1<ran2
        {
            temp = ran1;
            ran1 = ran2;
            ran2 = temp;
        }
        flag = ran2 - ran1 + 1;// 个数
        for (i = 0, j = ran1; i < flag; i++, j++) {
            temp = newPopulation[k1][j];
            newPopulation[k1][j] = newPopulation[k2][j];
            newPopulation[k2][j] = temp;
        }

    }

    // 多次对换变异算子
    public void OnCVariation(int k) {
        int ran1, ran2, temp;
        int count;// 对换次数
        count = random.nextInt(65535) % LL;

        for (int i = 0; i < count; i++) {

            ran1 = random.nextInt(65535) % LL;
            ran2 = random.nextInt(65535) % LL;
            while (ran1 == ran2) {
                ran2 = random.nextInt(65535) % LL;
            }
            temp = newPopulation[k][ran1];
            newPopulation[k][ran1] = newPopulation[k][ran2];
            newPopulation[k][ran2] = temp;
        }
    }

    public void solve() throws IOException {
        int i;
        int k;

        // 初始化种群
        initGroup();
        // 计算初始化种群适应度，Fitness[max]
        for (k = 0; k < scale; k++) {
            fitness[k] = evaluate(oldPopulation[k], backpack[k]);
            if (fitness[k] == 0) {
                // 修复不可行解个体
                op_repair(oldPopulation[k]);
                fitness[k] = evaluate(oldPopulation[k], backpack[k]);
            }
            // System.out.println(fitness[k]);
        }

        for (k = 0; k < scale; k++) {
            // 优化背包资源利用不充分的可行解个体
            op_optimize(oldPopulation[k], backpack[k]);
            fitness[k] = evaluate(oldPopulation[k], backpack[k]);
        }

        // 计算初始化种群中各个个体的累积概率，Pi[max]
        countRate();
        System.out.println("初始种群...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < LL; i++) {
                System.out.print(oldPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("----" + fitness[k] + " " + Pi[k]);
        }

        // 迭代进化
        for (t = 0; t < MAX_GEN; t++) {
            evolution();
            // 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
            for (k = 0; k < scale; k++) {
                for (i = 0; i < LL; i++) {
                    oldPopulation[k][i] = newPopulation[k][i];
                }
            }
            // 计算种群适应度
            for (k = 0; k < scale; k++) {
                fitness[k] = evaluate(oldPopulation[k], backpack[k]);
                if (fitness[k] == 0) {
                    // 修复不可行解个体
                    op_repair(oldPopulation[k]);
                    fitness[k] = evaluate(oldPopulation[k], backpack[k]);
                }
            }

            for (k = 0; k < scale; k++) {
                // 优化背包资源利用不充分的可行解个体
                op_optimize(oldPopulation[k], backpack[k]);
                fitness[k] = evaluate(oldPopulation[k], backpack[k]);
            }

            // 计算种群中各个个体的累积概率
            countRate();
        }

        System.out.println("最后种群...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < LL; i++) {
                System.out.print(oldPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("---" + fitness[k] + " " + Pi[k]);
        }

        String dir = "logs/";
        String[] file_list = new File(dir).list();
        int count = 0;
        for (String file : file_list) {
            if (file.contains(test_case)) {
                count += 1;
            }
        }
        String filename = "log" + count + "_" + test_case + ".txt";
        File file = new File(dir + filename);
        //如果文件不存在，创建文件
        if (!file.exists())
            file.createNewFile();
        //创建FileWriter对象
        FileWriter writer = new FileWriter(file);

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        writer.write(formatter.format(date) + "\n");

        System.out.println("最佳编码出现代数：");
        System.out.println(bestT);
        //向文件中写入内容
        writer.write("最佳编码出现代数：\n" + bestT + "\n");

        System.out.println("最佳编码价值");
        System.out.println(bestLength);
        //向文件中写入内容
        writer.write("最佳编码价值：\n" + bestLength + "\n");

        System.out.println("最佳编码：");
        //向文件中写入内容
        writer.write("最佳编码价值：\n");
        for (i = 0; i < LL; i++) {
            System.out.print(bestTour[i] + ",");
            //向文件中写入内容
            writer.write(bestTour[i] + ",");
        }
        System.out.println();
        //向文件中写入内容
        writer.write("\n");

        System.out.println("最佳编码重量：");
        //向文件中写入内容
        writer.write("最佳编码重量：\n");
        for (i = 0; i < dimension; i++) {
            System.out.println(bestBackpack[i] + " ");
            //向文件中写入内容
            writer.write(bestBackpack[i] + " ");
        }

        writer.flush();
        writer.close();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Start....");

        TestCase tc = new TestCase();
        String test_case_dir = "testcases/IHGA/";
        String test_case_name = "IHGA_2.dat";
        tc.readTestCase(test_case_dir + test_case_name);

        //GA2 ga = new GA2(20, 50, 2, 500, 0.8f, 0.9f);
        KP_GA ga = new KP_GA(20, tc, 500, 0.8f, 0.9f, test_case_name);
        ga.init();
        ga.solve();
    }
}