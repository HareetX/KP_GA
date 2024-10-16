package test;


import app.IntegerTestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class KP_IGA_RO {

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
    private long totalTime_ms;
    private ArrayList<Double> averageFitnessList;
    private ArrayList<Integer> maxFitnessList;

    // 初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
    private int[][] oldPopulation;
    private int[][] newPopulation;// 新的种群，子代种群
    private int[] fitness;// 种群适应度，表示种群中各个个体的适应度
    private int[][] backpack;// 种群背包重量

    private float[] Pi;// 种群中各个个体的累计概率
    private float Pc;// 交叉概率
    private float Pm;// 变异概率
    private float Ph;// 混合优化概率
    private int cross_id;// 1-单点 2-双点 0-均匀
    private int mutate_id;// 1-基位 2-散播 3-移位 0-插入
    private int select_id;// 1-赌轮 2-最佳 0-最佳赌轮
    private int t;// 当前代数
    private Random random;

    private String test_case = null;

    // 种群规模，染色体长度,最大代数，交叉率，变异率
    public KP_IGA_RO(int s, int l, int d, int g, float c, float m, float h) {
        scale = s;
        LL = l;
        dimension = d;
        MAX_GEN = g;
        Pc = c;
        Pm = m;
        Ph = h;
    }

    public KP_IGA_RO(int s, IntegerTestCase tc, int g, float c, float m, float h, String test_case_name) {
        scale = s;
        LL = tc.LL;
        dimension = tc.dimension;
        v = tc.v;
        b = tc.b;
        pb = tc.pb;
        MAX_GEN = g;
        Pc = c;
        Pm = m;
        Ph = h;
        test_case = test_case_name;
    }

    public KP_IGA_RO(int s, IntegerTestCase tc, int g, float c, float m, float h,
                     int cross, int mutate, int select, String test_case_name) {
        scale = s;
        LL = tc.LL;
        dimension = tc.dimension;
        v = tc.v;
        b = tc.b;
        pb = tc.pb;
        MAX_GEN = g;
        Pc = c;
        Pm = m;
        Ph = h;
        cross_id = cross % 3;
        mutate_id = mutate % 4;
        select_id = select % 3;
        test_case = test_case_name;
    }

    /* 获取降序排序索引
     * input:  data array
     * output: index array
     * Return the index array of the sorted array.
     */
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

    private static int[] descendingSortedIndexes(int[] array) {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer index1, Integer index2) {
                // 根据数组的值进行降序排序
                return Integer.compare(array[index2], array[index1]);
            }
        });

        // 将Integer数组转换为int数组
        int[] sortedIndexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            sortedIndexes[i] = indexes[i];
        }

        return sortedIndexes;
    }

    public static void sortPopulationByFitness(int[][] population, int[] fitness, int[][] backpack) {
        Integer[] indexes = new Integer[fitness.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        // 根据适应度数组对索引数组进行降序排序
        Arrays.sort(indexes, Comparator.comparingInt(index -> -fitness[index]));

        // 根据排序后的索引数组重新排列种群数组和适应度数组
        int[][] newPopulation = new int[population.length][];
        int[] newFitness = new int[fitness.length];
        int[][] newBackpack = new int[backpack.length][];
        for (int i = 0; i < indexes.length; i++) {
            newPopulation[i] = population[indexes[i]];
            newFitness[i] = fitness[indexes[i]];
            newBackpack[i] = backpack[indexes[i]];
        }

        // 将排好序的种群数组和适应度数组写回输入的数组
        System.arraycopy(newPopulation, 0, population, 0, population.length);
        System.arraycopy(newFitness, 0, fitness, 0, fitness.length);
        System.arraycopy(newBackpack, 0, backpack, 0, backpack.length);
    }

    /* 初始化KP_GA
     * Initial the Algorithm parameters.
     * Calculate the efficient and generate the HD & HV.
     */
    private void init() {
        bestLength = 0;
        bestTour = new int[LL];
        bestT = 0;
        bestBackpack = new int[dimension];
        t = 0;
        totalTime_ms = 0;
        averageFitnessList = new ArrayList<Double>();
        maxFitnessList = new ArrayList<Integer>();

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

        HV = descendingSortedIndexes(v);
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

    /* 贪婪修复算子
     * input:  不可行解个体chromosome
     * output: None
     * Repair chromosome by HD.
     * The method will modify the input chromosome.
     */
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

    /* 贪婪优化算子
     * input:  可行解个体chromosome
     * output: None
     * Optimize chromosome by HD in greedy.
     * The method will modify the input chromosome.
     */
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

    void op_hybrid_optimize(int[] chromosome, int[] bb) {
        // 优化可行解个体chromosome，使其充分利用背包资源
        float r = random.nextFloat();
        int[] HL;
        if (r < Ph) {
            HL = HD.clone();
        } else {
            HL = HV.clone();
        }
        for (int i = 0; i < LL; i++) {
            // 按HD顺序遍历物品
            int index = HL[i];
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

    int op_local_search(int[] chromosome, int fitness, int[] backpack, int local_times) {
        int tmp_fitness = fitness;
        for (int i = 0; i < local_times; i++) {
            int[] new_chromosome = chromosome.clone();
            int[] new_backpack = new int[dimension];
            int j = random.nextInt(LL);
            if (new_chromosome[j] == 1) {
                new_chromosome[j] = 0;
            }
            else if (new_chromosome[j] == 0) {
                new_chromosome[j] = 1;
            }

            int new_fitness = evaluate(new_chromosome, new_backpack);
            if (new_fitness == 0) {
                // 修复不可行解个体
                op_repair(new_chromosome);
                new_fitness = evaluate(new_chromosome, new_backpack);
            }
            // 优化背包资源利用不充分的可行解个体
            if (Ph > 0.95) {
                op_optimize(new_chromosome, new_backpack);
            } else {
                op_hybrid_optimize(new_chromosome, new_backpack);
            }
            new_fitness = evaluate(new_chromosome, new_backpack);

            if (new_fitness > tmp_fitness) {
                tmp_fitness = new_fitness;
                System.arraycopy(new_chromosome, 0, chromosome, 0, chromosome.length);
                //System.arraycopy(new_backpack, 0, backpack, 0, backpack.length);
                for (int k = 0; k < dimension; k++) {
                    backpack[k] = new_backpack[k];
                }
            }
        }
        return tmp_fitness;
    }

    int op_sa_local_search(int[] chromosome, int fitness, int[] backpack, int local_times,
                           double T, double Tf, double k) {
        int tmp_fitness = fitness;
        while (T > Tf) {
            for (int i = 0; i < local_times; i++) {
                int[] new_chromosome = chromosome.clone();
                int[] new_backpack = new int[dimension];
                int j = random.nextInt(LL);
                if (new_chromosome[j] == 1) {
                    new_chromosome[j] = 0;
                } else if (new_chromosome[j] == 0) {
                    new_chromosome[j] = 1;
                }

                int new_fitness = evaluate(new_chromosome, new_backpack);
                if (new_fitness == 0) {
                    // 修复不可行解个体
                    op_repair(new_chromosome);
                    new_fitness = evaluate(new_chromosome, new_backpack);
                }
                // 优化背包资源利用不充分的可行解个体
                if (Ph > 0.95) {
                    op_optimize(new_chromosome, new_backpack);
                } else {
                    op_hybrid_optimize(new_chromosome, new_backpack);
                }
                new_fitness = evaluate(new_chromosome, new_backpack);

                double p0 = Math.exp((new_fitness - tmp_fitness) / T);
                if (new_fitness > tmp_fitness || random.nextDouble() < Math.exp((new_fitness - tmp_fitness) / T)) {
                    //chromosome = new_chromosome;
                    tmp_fitness = new_fitness;
                    //backpack = new_backpack;
                    System.arraycopy(new_chromosome, 0, chromosome, 0, chromosome.length);
                    //System.arraycopy(new_fitness, 0, fitness, 0, fitness.length);
                    System.arraycopy(new_backpack, 0, backpack, 0, backpack.length);
                }
            }
            T = T * k;
        }
        return tmp_fitness;
    }

    /* 染色体评估
     * input:  待评估chromosome, 待更新backpack
     * output: total vale of the chromosome
     * Evaluate the chromosome. Calculate the total value and backpack.
     * If the backpack is less than capacity, update the backpack and return the total value.
     * Set the backpack to 0 and return 0, if over.
     */
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
    float[] countRate(int[] fitness, int scale) {
        int k;
        double sumFitness = 0;// 适应度总和

        int[] temp_f = new int[scale];

        float[] pi = new float[scale];

        for (k = 0; k < scale; k++) {
            temp_f[k] = fitness[k];
            sumFitness += temp_f[k];
        }

        if (sumFitness != 0) {
            pi[0] = (float) (temp_f[0] / sumFitness);
            for (k = 1; k < scale; k++) {
                pi[k] = (float) (temp_f[k] / sumFitness + pi[k - 1]);
            }
        }
        else {
            pi[0] = (float) (1. / scale);
            for (k = 1; k < scale; k++) {
                pi[k] = (float) (1. / scale + pi[k - 1]);
            }
        }

        return pi;
    }

    // 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
    public void copyGh(int kk, int[][] srcPopulation, int k, int[][] destPopulation) {
        int i;
        for (i = 0; i < LL; i++) {
            destPopulation[k][i] = srcPopulation[kk][i];
        }
    }

    public int findBestGh() {
        int k, i, maxid;
        int maxevaluation;
        int sumevaluation;
        double average;

        maxid = 0;
        maxevaluation = fitness[0];
        sumevaluation = fitness[0];
        for (k = 1; k < scale; k++) {
            sumevaluation += fitness[k];
            if (maxevaluation < fitness[k]) {
                maxevaluation = fitness[k];
                maxid = k;
            }
        }
        average = (double) sumevaluation / fitness.length;
        averageFitnessList.add(average);
        maxFitnessList.add(maxevaluation);

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

        return maxid;
    }

    // 挑选某代种群中适应度最高的个体，直接复制到子代中
    // 前提是已经计算出各个个体的适应度Fitness[max]
    public void selectBestGh() {
        int max_id = findBestGh();

        // 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
        copyGh(max_id, oldPopulation, 0, newPopulation); // 将当代种群中适应度最高的染色体k复制到新种群中，排在第一位0
    }

    // 赌轮选择策略挑选
    public int[] roulette_wheel_select(int[][] srcPopulation, int srcNum, int[][] destPopulation, int destNum,
                                       float[] pi, int start) {
        if (start < 0 || start >= destNum) {
            throw new IllegalArgumentException("Invalid start");
        }
        int k, i, selectId;
        float ran1;
        int[] selectIds = new int[destNum];
        for (k = start; k < destNum; k++) {
            ran1 = (float) (random.nextInt(65535) % 1000 / 1000.0);
            // System.out.println("概率"+ran1);
            // 产生方式
            for (i = 0; i < srcNum; i++) {
                if (ran1 <= pi[i]) {
                    break;
                }
            }
            selectId = i;
            selectIds[k] = selectId;
            copyGh(selectId, srcPopulation, k, destPopulation);
        }
        return selectIds;
    }

    public void copy_pop_select(){
        int k, i;
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
            if (Ph > 0.95) {
                op_optimize(oldPopulation[k], backpack[k]);
            } else {
                op_hybrid_optimize(oldPopulation[k], backpack[k]);
            }
            fitness[k] = evaluate(oldPopulation[k], backpack[k]);
        }
        sortPopulationByFitness(oldPopulation, fitness, backpack);
    }

    public void roulette_wheel_pop_select() {
        // 找到并选择某代种群中适应度最高的个体
        int[] new_fitness = new int[scale];
        int[][] new_backpack = new int[scale][dimension];
        // 计算种群适应度
        for (int k = 0; k < scale; k++) {
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            if (new_fitness[k] == 0) {
                // 修复不可行解个体
                op_repair(newPopulation[k]);
                new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            }
        }
        for (int k = 0; k < scale; k++) {
            // 优化背包资源利用不充分的可行解个体
            if (Ph > 0.95) {
                op_optimize(newPopulation[k], new_backpack[k]);
            } else {
                op_hybrid_optimize(newPopulation[k], new_backpack[k]);
            }
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
        }
        sortPopulationByFitness(newPopulation, new_fitness, new_backpack);
        copyGh(0, newPopulation, 0, oldPopulation);
        fitness[0] = new_fitness[0];
        System.arraycopy(new_backpack[0], 0, backpack[0], 0, dimension);
        // 赌轮选择策略挑选scale-1个下一代个体
        int[] selectIds = roulette_wheel_select(newPopulation, scale, oldPopulation, scale, countRate(new_fitness, scale), 1);
        for(int k = 1; k < scale; k++) {
            fitness[k] = new_fitness[selectIds[k]];
            System.arraycopy(new_backpack[selectIds[k]], 0, backpack[k], 0, dimension);
        }
    }

    public void best_pop_select() {
        int[] new_fitness = new int[scale];
        int[][] new_backpack = new int[scale][dimension];
        // 计算种群适应度
        for (int k = 0; k < scale; k++) {
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            if (new_fitness[k] == 0) {
                // 修复不可行解个体
                op_repair(newPopulation[k]);
                new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            }
        }
        for (int k = 0; k < scale; k++) {
            // 优化背包资源利用不充分的可行解个体
            if (Ph > 0.95) {
                op_optimize(newPopulation[k], new_backpack[k]);
            } else {
                op_hybrid_optimize(newPopulation[k], new_backpack[k]);
            }
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
        }
        sortPopulationByFitness(newPopulation, new_fitness, new_backpack);

        // 合并排序
        int totalLength = scale + scale;
        int[][] mergedPopulation = new int[totalLength][LL];
        int[] mergedFitness = new int[totalLength];
        int[][] mergedBackpack = new int[totalLength][dimension];

        int i = 0, j = 0, k = 0;
        while (i < scale && j < scale) {
            if (fitness[i] >= new_fitness[j]) {
                System.arraycopy(oldPopulation[i], 0, mergedPopulation[k], 0, LL);
                mergedFitness[k] = fitness[i];
                System.arraycopy(backpack[i], 0, mergedBackpack[k], 0, dimension);
                i++;
            } else {
                System.arraycopy(newPopulation[j], 0, mergedPopulation[k], 0, LL);
                mergedFitness[k] = new_fitness[j];
                System.arraycopy(new_backpack[j], 0, mergedBackpack[k], 0, dimension);
                j++;
            }
            k++;
        }

        while (i < scale) {
            System.arraycopy(oldPopulation[i], 0, mergedPopulation[k], 0, LL);
            mergedFitness[k] = fitness[i];
            System.arraycopy(backpack[i], 0, mergedBackpack[k], 0, dimension);
            i++;
            k++;
        }

        while (j < scale) {
            System.arraycopy(newPopulation[j], 0, mergedPopulation[k], 0, LL);
            mergedFitness[k] = new_fitness[j];
            System.arraycopy(new_backpack[j], 0, mergedBackpack[k], 0, dimension);
            j++;
            k++;
        }

        // 选择最优的scale个染色体个体作为下一代种群
        System.arraycopy(mergedPopulation, 0, oldPopulation, 0, scale);
        System.arraycopy(mergedFitness, 0, fitness, 0, scale);
        System.arraycopy(mergedBackpack, 0, backpack, 0, scale);
    }

    public void best_roulette_wheel_pop_select() {
        int[] new_fitness = new int[scale];
        int[][] new_backpack = new int[scale][dimension];
        // 计算种群适应度
        for (int k = 0; k < scale; k++) {
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            if (new_fitness[k] == 0) {
                // 修复不可行解个体
                op_repair(newPopulation[k]);
                new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            }
        }
        for (int k = 0; k < scale; k++) {
            // 优化背包资源利用不充分的可行解个体
            if (Ph > 0.95) {
                op_optimize(newPopulation[k], new_backpack[k]);
            } else {
                op_hybrid_optimize(newPopulation[k], new_backpack[k]);
            }
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
        }
        sortPopulationByFitness(newPopulation, new_fitness, new_backpack);

        // 合并排序
        int totalLength = scale + scale;
        int[][] mergedPopulation = new int[totalLength][LL];
        int[] mergedFitness = new int[totalLength];
        int[][] mergedBackpack = new int[totalLength][dimension];

        int i = 0, j = 0, k = 0;
        while (i < scale && j < scale) {
            if (fitness[i] >= new_fitness[j]) {
                System.arraycopy(oldPopulation[i], 0, mergedPopulation[k], 0, LL);
                mergedFitness[k] = fitness[i];
                System.arraycopy(backpack[i], 0, mergedBackpack[k], 0, dimension);
                i++;
            } else {
                System.arraycopy(newPopulation[j], 0, mergedPopulation[k], 0, LL);
                mergedFitness[k] = new_fitness[j];
                System.arraycopy(new_backpack[j], 0, mergedBackpack[k], 0, dimension);
                j++;
            }
            k++;
        }

        while (i < scale) {
            System.arraycopy(oldPopulation[i], 0, mergedPopulation[k], 0, LL);
            mergedFitness[k] = fitness[i];
            System.arraycopy(backpack[i], 0, mergedBackpack[k], 0, dimension);
            i++;
            k++;
        }

        while (j < scale) {
            System.arraycopy(newPopulation[j], 0, mergedPopulation[k], 0, LL);
            mergedFitness[k] = new_fitness[j];
            System.arraycopy(new_backpack[j], 0, mergedBackpack[k], 0, dimension);
            j++;
            k++;
        }

        // 选择最优的scale个染色体个体作为下一代种群
        float[] merged_pi = countRate(mergedFitness, totalLength);
        int[][] tmpPopulation = new int[scale][LL];
        int[] tmpFitness = new int[scale];
        int[][] tmpBackpack = new int[scale][dimension];
        copyGh(0, mergedPopulation, 0, tmpPopulation);
        tmpFitness[0] = mergedFitness[0];
        System.arraycopy(mergedBackpack[0], 0, tmpBackpack[0], 0, dimension);
        int[] selectIds = roulette_wheel_select(mergedPopulation, totalLength, tmpPopulation, scale, merged_pi, 1);//TODO 先copy最优的个体，再赌轮选择
        for(k = 1; k < scale; k++) {
            tmpFitness[k] = mergedFitness[selectIds[k]];
            System.arraycopy(mergedBackpack[selectIds[k]], 0, tmpBackpack[k], 0, dimension);
        }
        System.arraycopy(tmpPopulation, 0, oldPopulation, 0, scale);
        System.arraycopy(tmpFitness, 0, fitness, 0, scale);
        System.arraycopy(tmpBackpack, 0, backpack, 0, scale);
    }

    // 两点交叉算子
    void OXCross() {
        for (int k1 = 0; k1 < scale; k1 = k1 + 1) {
            float r = random.nextFloat();// 产生概率
            // System.out.println("交叉率..." + r);
            if (r < Pc) {
                int k2 = random.nextInt(65535) % scale;
                while (k2 == k1) {
                    k2 = random.nextInt(65535) % scale;
                }
                // System.out.println(k + "与" + k_cross + "进行交叉...");

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
        }
    }

    void OXCross_single_point() {
        for (int i = 0; i < scale; i += 2) {
            // 随机选择两个父母个体
            int parent1Index = random.nextInt(scale);
            int parent2Index = random.nextInt(scale);

            // 交叉操作
            if (random.nextDouble() < Pc) {
                int ran = 0;
                ran = random.nextInt(65535) % LL;
                for (int j = 0; j < LL; j++) {
                    // 单点交叉
                    if (j <= ran) {
                        newPopulation[i][j] = oldPopulation[parent2Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent1Index][j];
                    } else {
                        newPopulation[i][j] = oldPopulation[parent1Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent2Index][j];
                    }
                }
            } else {
                // 如果不进行交叉，则子代个体与父代个体相同
                newPopulation[i] = oldPopulation[parent1Index].clone();
                newPopulation[i + 1] = oldPopulation[parent2Index].clone();
            }
        }
    }

    void OXCross_two_point() {
        for (int i = 0; i < scale; i += 2) {
            // 随机选择两个父母个体
            int parent1Index = random.nextInt(scale);
            int parent2Index = random.nextInt(scale);

            // 交叉操作
            if (random.nextDouble() < Pc) {
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
                for (int j = 0; j < LL; j++) {
                    // 两点交叉
                    if (j >= ran1 && j <= ran2) {
                        newPopulation[i][j] = oldPopulation[parent2Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent1Index][j];
                    } else {
                        newPopulation[i][j] = oldPopulation[parent1Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent2Index][j];
                    }
                }
            } else {
                // 如果不进行交叉，则子代个体与父代个体相同
                newPopulation[i] = oldPopulation[parent1Index].clone();
                newPopulation[i + 1] = oldPopulation[parent2Index].clone();
            }
        }
    }

    // 按照均匀交叉
    void OXCross_Uniform() {
        for (int i = 0; i < scale; i += 2) {
            // 随机选择两个父母个体
            int parent1Index = random.nextInt(scale);
            int parent2Index = random.nextInt(scale);

            // 交叉操作
            if (random.nextDouble() < Pc) {
                for (int j = 0; j < LL; j++) {
                    // 均匀交叉
                    if (random.nextBoolean()) {
                        newPopulation[i][j] = oldPopulation[parent1Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent2Index][j];
                    } else {
                        newPopulation[i][j] = oldPopulation[parent2Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent1Index][j];
                    }
                }
            } else {
                // 如果不进行交叉，则子代个体与父代个体相同
                newPopulation[i] = oldPopulation[parent1Index].clone();
                newPopulation[i + 1] = oldPopulation[parent2Index].clone();
            }
        }
    }

    // 多次对换变异算子
    public void OnCVariation() {
        for (int k = 0; k < scale; k = k + 1) {
            float r = random.nextFloat();// 产生概率
            // System.out.println("变异率..." + r);
            if (r < Pm) {
                // System.out.println(k + "变异...");

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
        }
    }

    // 各位异向变异算子，单独记录新的子代
    public void OnCVariation_Bitwise() {
        for (int i = 0; i < scale; i += 1) {
            for (int j = 0 ; j < LL; j += 1) {
                if (random.nextDouble() < Pm) {
                    // 如果随机数小于变异概率，则进行基位变异
                    newPopulation[i][j] = (newPopulation[i][j] == 0) ? 1 : 0; // 0变异为1，1变异为0
                }
            }
        }
    }

    public void OnCVariation_SM() {
        for (int i = 0; i < scale; i += 1) {
            MutateSM(newPopulation[i]);
        }
    }

    public void OnCVariation_DM() {
        for (int i = 0; i < scale; i += 1) {
            MutateDM(newPopulation[i]);
        }
    }

    public void OnCVariation_IM() {
        for (int i = 0; i < scale; i += 1) {
            MutateIM(newPopulation[i]);
        }
    }

    public void MutateSM(int[] chromosome) {
        if (random.nextDouble() < Pm)
            return;

        final int m_nMinSpanSize = 3;

        int beg, end;
        int[] section = ChooseSection(chromosome.length - 1, m_nMinSpanSize);
        beg = section[0];
        end = section[1];
        int span = end - beg + 1;
        int numberOfSwapsReqd = span;

        while (numberOfSwapsReqd-- > 0) {
            int gene1 = random.nextInt(beg, end + 1);
            int gene2 = random.nextInt(beg, end + 1);

            int tmp = chromosome[gene1];
            chromosome[gene1] = chromosome[gene2];
            chromosome[gene2] = tmp;
        }
    }

    public void MutateDM(int[] chromosome) {
        if (random.nextDouble() < Pm)
            return;

        final int m_nMinSpanSize = 3;

        int beg, end;
        int i;

        int[] section = ChooseSection(chromosome.length - 1, m_nMinSpanSize);
        beg = section[0];
        end = section[1];
        int span = end - beg + 1;
        while (chromosome.length <= span || span <= 0) {
            section = ChooseSection(chromosome.length - 1, m_nMinSpanSize);
            beg = section[0];
            end = section[1];
            span = end - beg + 1;
        }

        int[] tmp_section = new int[span];
        int[] tmp_anti_section = new int[chromosome.length - span];
        int p = 0;
        int q = 0;
        for (i = 0; i < chromosome.length; i++) {
            if (i >= beg && i <= end) {
                tmp_section[p] = chromosome[i];
                p++;
            }
            else {
                tmp_anti_section[q] = chromosome[i];
                q++;
            }
        }

        int insert_position = random.nextInt(0, tmp_anti_section.length);
        insertArray(chromosome, tmp_anti_section, tmp_section, insert_position);
    }

    public void MutateIM(int[] chromosome) {
        if (random.nextDouble() < Pm)
            return;

        int beg, end;
        int i;

        beg = random.nextInt(0, chromosome.length);
        end = beg;

        int[] tmp_section = new int[1];
        int[] tmp_anti_section = new int[chromosome.length - 1];
        int p = 0;
        int q = 0;
        for (i = 0; i < chromosome.length; i++) {
            if (i >= beg && i <= end) {
                tmp_section[p] = chromosome[i];
                p++;
            }
            else {
                tmp_anti_section[q] = chromosome[i];
                q++;
            }
        }

        int insert_position = random.nextInt(0, tmp_anti_section.length);
        insertArray(chromosome, tmp_anti_section, tmp_section, insert_position);
    }

    public static void insertArray(int[] resultArray, int[] originalArray, int[] insertionArray, int position) {
        if (position < 0 || position >= originalArray.length) {
            throw new IllegalArgumentException("Invalid position");
        }
        if (resultArray.length != originalArray.length + insertionArray.length) {
            throw new IllegalArgumentException("Invalid array");
        }

        System.arraycopy(originalArray, 0, resultArray, 0, originalArray.length);

        // Shift elements after insertion position to make room for insertionArray
        System.arraycopy(resultArray, position, resultArray, position + insertionArray.length, originalArray.length - position);

        // Insert insertionArray into result array
        System.arraycopy(insertionArray, 0, resultArray, position, insertionArray.length);
    }

    private int[] ChooseSection(int maxIndex, int minSpanSize) {
        int[] section = new int[2];
        section[0] = random.nextInt(0, maxIndex - minSpanSize + 1); //TODO bound is wrong
        section[1] = section[0] + minSpanSize + random.nextInt(0, maxIndex - section[0] - minSpanSize + 1);
        return section;
    }

    public boolean solve(int local_times) throws IOException {
        int i;
        int k;

        // 开始时间
        long start_time = System.currentTimeMillis();

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
            if (Ph > 0.95) {
                op_optimize(oldPopulation[k], backpack[k]);
            } else {
                op_hybrid_optimize(oldPopulation[k], backpack[k]);
            }
            fitness[k] = evaluate(oldPopulation[k], backpack[k]);
        }
        // 根据适应度降序排列
        sortPopulationByFitness(oldPopulation, fitness, backpack);

        // 计算初始化种群中各个个体的累积概率，Pi[max]
        Pi = countRate(fitness, scale);

        // 迭代进化
        int equal_count = 0;
        //for (t = 0; t < MAX_GEN; t++) {
        while (t < MAX_GEN && equal_count < 5) {
            // 找到某代种群中适应度最高的个体
            findBestGh();

            // 交叉方法
            //OXCross();
            if (cross_id == 1) {
                OXCross_single_point();
            } else if (cross_id == 2) {
                OXCross_two_point();
            } else if (cross_id == 0) {
                OXCross_Uniform();
            } else {
                System.out.println("Illegal cross_id!");
                return false;
            }

            // 变异方法
            //OnCVariation();
            if (mutate_id == 1) {
                OnCVariation_Bitwise();
            } else if (mutate_id == 2) {
                OnCVariation_SM();
            } else if (mutate_id == 3) {
                OnCVariation_DM();
            } else if (mutate_id == 0) {
                OnCVariation_IM();
            } else {
                System.out.println("Illegal mutate_id!");
                return false;
            }

            // 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
            //copy_pop_select();
            // 在newPopulation（和oldPopulation）中选择个体进入父代种群oldPopulation，准备下一代进化
            if (select_id == 1) {
                roulette_wheel_pop_select();
            } else if (select_id == 2) {
                best_pop_select();
            } else if (select_id == 0) {
                best_roulette_wheel_pop_select();
            } else {
                System.out.println("Illegal select_id!");
                return false;
            }

            // 随机贪婪局部搜索模块
            //for (i = 0; i < scale; i++) {
            //    fitness[i] = op_local_search(oldPopulation[i], fitness[i], backpack[i], local_times);
            //}
            //sortPopulationByFitness(oldPopulation, fitness, backpack);

            // SA局部搜索模块
            //for (i = 0; i < scale; i++) {
            //    fitness[i] = op_sa_local_search(oldPopulation[i], fitness[i], backpack[i], local_times,
            //            200, 0.1, 0.9); //200, 0.001, 0.99);
            //}
            //sortPopulationByFitness(oldPopulation, fitness, backpack);

            // 计算种群中各个个体的累积概率
            Pi = countRate(fitness, scale);

            t++;
            if (Math.abs(fitness[0] - fitness[scale - 1]) == 0) {
                equal_count++;
            } else {
                equal_count = 0;
            }

            // 结束时间
            long end_time = System.currentTimeMillis();
            totalTime_ms = end_time - start_time;
            if (totalTime_ms > 600000) {
                System.out.println("Timeout!");
                return false;
            }
        }

        //System.out.println("最后种群...");
        //for (k = 0; k < scale; k++) {
        //    for (i = 0; i < LL; i++) {
        //        System.out.print(oldPopulation[k][i] + ",");
        //    }
        //    System.out.println();
        //    System.out.println("---" + fitness[k] + " " + Pi[k]);
        //}

        String dir = "logs/" + test_case.split(".dat")[0] + "/";
        File log_dir = new File(dir);
        if (!(log_dir.exists())) {
            log_dir.mkdir();
        }
        String[] file_list = log_dir.list();
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
        writer.write("最佳编码：\n");
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
            System.out.print(bestBackpack[i] + " ");
            //向文件中写入内容
            writer.write(bestBackpack[i] + " ");
        }

        System.out.println("\n执行代数：");
        System.out.println(t);
        //向文件中写入内容
        writer.write("\n执行代数：\n" + t + "\n");

        System.out.println("执行时间：");
        System.out.println(totalTime_ms + " ms");
        //向文件中写入内容
        writer.write("执行时间：\n" + totalTime_ms + "\n");

        writer.flush();
        writer.close();

        // 写入种群平均价值变化曲线
        String avg_filename = "log" + count + "_" + test_case.split(".dat")[0] + ".avg.txt";
        File avg_file = new File(dir + avg_filename);
        //如果文件不存在，创建文件
        if (!avg_file.exists())
            avg_file.createNewFile();
        //创建FileWriter对象
        writer = new FileWriter(avg_file);
        for (double f : averageFitnessList) {
            writer.write(f + " ");
        }
        writer.flush();
        writer.close();

        // 写入种群最佳价值变化曲线
        String max_filename = "log" + count + "_" + test_case.split(".dat")[0] + ".max.txt";
        File max_file = new File(dir + max_filename);
        //如果文件不存在，创建文件
        if (!max_file.exists())
            max_file.createNewFile();
        //创建FileWriter对象
        writer = new FileWriter(max_file);
        for (int f : maxFitnessList) {
            writer.write(f + " ");
        }
        writer.flush();
        writer.close();

        return true;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String[] d_list = {"5", "10", "30"};
        String[] n_list = {"100", "250", "500"};
        String[] r_list = {"0.25", "0.50", "0.75"};
        String[] i_list = {"1", "10"};
        int file_num = d_list.length * n_list.length * r_list.length * i_list.length;
        String[] file_path_list = new String[file_num];
        int k = 0;
        for (String d : d_list) {
            for (String n : n_list) {
                for (String r : r_list) {
                    for (String i : i_list) {
                        file_path_list[k] = "testcases/chubeas/OR"+d+"x"+n+"/OR"+d+"x"+n+"-"+r+"_"+i+".dat";
                        k++;
                    }
                }
            }
        }

        float[] Ph_list = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};

        int f_id;
        int exp_count;
        for (f_id = 0; f_id < file_path_list.length; f_id++) {
            String file_path_arg = file_path_list[f_id];
            int scale_arg = 100;
            float Pc_arg = 0.99f;
            float Pm_arg = 0.1f;
            int cross_arg = 3;
            int mutate_arg = 1;
            int select_arg = 2;
            //float Ph_arg = 0.6f;
            int local_times_arg = 20;
            for (exp_count = 0; exp_count < Ph_list.length; exp_count++) {
                float Ph_arg = Ph_list[exp_count];

                String[] tmp_str = file_path_arg.split("/");
                String test_case_name = tmp_str[tmp_str.length - 1];
                System.out.println("Start.... " + test_case_name);

                IntegerTestCase tc = new IntegerTestCase();
                tc.readTestCase(file_path_arg);

                boolean success;
                for (int i = 0; i < 1; i++) {
                    KP_IGA_RO ga = new KP_IGA_RO(scale_arg, tc, 5000, Pc_arg, Pm_arg, Ph_arg,
                            cross_arg, mutate_arg, select_arg, test_case_name);
                    ga.init();
                    success = ga.solve(local_times_arg);
                    if (!success) {
                        break;
                    }
                }
            }
        }
    }
}