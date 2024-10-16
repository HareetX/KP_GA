package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class KP_IGA {

    private int[] v; // Item values
    private int[][] b; // Item volumes
    private int[] pb; // Backpack capacities

    private double[] efficiency;

    private int[] HD;

    private int[] HV;

    private int LL; // Chromosome length

    private int dimension; // Dimension
    private int scale; // Population scale
    private int MAX_GEN; // Number of generations

    private int bestT; // Best generation number
    private int bestLength; // Best encoded value
    private int[] bestTour; // Best encoding
    private int[] bestBackpack; // Best encoding corresponding backpack weight
    private long totalTime_ms;
    private ArrayList<Double> averageFitnessList;
    private ArrayList<Integer> maxFitnessList;

    // Initial population, parent population, rows represent population scale, one row represents one individual (chromosome), columns represent chromosome gene segments
    private int[][] oldPopulation;
    private int[][] newPopulation; // New population, offspring population
    private int[] fitness; // Population fitness, representing the fitness of each individual in the population
    private int[][] backpack; // Population backpack weights

    private float[] Pi; // Cumulative probability of each individual in the population
    private float Pc; // Crossover probability
    private float Pm; // Mutation probability
    private float Ph; // Hybrid optimization probability
    private int t; // Current generation
    private Random random;

    private String test_case = null;

    // Population scale, chromosome length, maximum generations, crossover rate, mutation rate
    public KP_IGA(int s, IntegerTestCase tc, int g, float c, float m, float h, String test_case_name) {
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

    /* Get descending sorted indexes
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
                // Sort in descending order based on array values
                return Double.compare(array[index2], array[index1]);
            }
        });

        // Convert Integer array to int array
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
                // Sort in descending order based on array values
                return Integer.compare(array[index2], array[index1]);
            }
        });

        // Convert Integer array to int array
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

        // Sort the index array in descending order based on fitness array
        Arrays.sort(indexes, Comparator.comparingInt(index -> -fitness[index]));

        // Rearrange the population array and fitness array based on the sorted index array
        int[][] newPopulation = new int[population.length][];
        int[] newFitness = new int[fitness.length];
        int[][] newBackpack = new int[backpack.length][];
        for (int i = 0; i < indexes.length; i++) {
            newPopulation[i] = population[indexes[i]];
            newFitness[i] = fitness[indexes[i]];
            newBackpack[i] = backpack[indexes[i]];
        }

        // Write the sorted population array and fitness array back to the input arrays
        System.arraycopy(newPopulation, 0, population, 0, population.length);
        System.arraycopy(newFitness, 0, fitness, 0, fitness.length);
        System.arraycopy(newBackpack, 0, backpack, 0, backpack.length);
    }

    /* Initialize KP_GA
     * Initialize the Algorithm parameters.
     * Calculate the efficiency and generate the HD & HV.
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

    // Initialize population
    void initGroup() {
        int k, i;
        for (k = 0; k < scale; k++) // Population size
        {
            // 01 encoding
            for (i = 0; i < LL; i++) {
                oldPopulation[k][i] = random.nextInt(65535) % 2;
            }
        }
    }

    /* Greedy repair operator
     * input:  Infeasible individual chromosome
     * output: None
     * Repair chromosome by HD.
     * The method will modify the input chromosome.
     */
    void op_repair(int[] chromosome) {
        // Repair the infeasible individual chromosome to a feasible solution
        int[] tmp_chromosome = new int[LL];
        for (int i = 0; i < LL; i++) {
            tmp_chromosome[i] = 0;
        }

        int[] bb = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            bb[i] = 0;
        }
        boolean st = true;  // Flag indicating whether the backpack capacity is exceeded
        for (int i = 0; i < LL; i++) {
            // Traverse items in HD order
            int index = HD[i];
            if (chromosome[index] == 1) {
                // chromosome has selected the index-th item
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

        // Update chromosome
        for (int i = 0; i < LL; i++) {
            chromosome[i] = tmp_chromosome[i];
        }
    }

    /* Hybrid optimization operator
     * input:  Feasible individual chromosome, backpack capacity bb
     * output: None
     * Optimize chromosome by HD in hybrid strategy.
     * The method will modify the input chromosome.
     */
    void op_hybrid_optimize(int[] chromosome, int[] bb) {
        // Optimize the feasible individual chromosome to make full use of backpack resources
        float r = random.nextFloat();
        int[] HL;
        if (r < Ph) {
            HL = HD.clone();
        } else {
            HL = HV.clone();
        }
        for (int i = 0; i < LL; i++) {
            // Traverse items in HD order
            int index = HL[i];
            if (chromosome[index] == 0) {
                // chromosome has not selected the index-th item
                boolean st = true;  // Flag indicating whether the backpack capacity is exceeded after selecting the index-th item
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
            } else if (new_chromosome[j] == 0) {
                new_chromosome[j] = 1;
            }

            int new_fitness = evaluate(new_chromosome, new_backpack);
            if (new_fitness == 0) {
                // Repair infeasible individual
                op_repair(new_chromosome);
                new_fitness = evaluate(new_chromosome, new_backpack);
            }
            // Optimize feasible individual with insufficient backpack resource utilization
            op_hybrid_optimize(new_chromosome, new_backpack);
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
                    // Repair infeasible individual
                    op_repair(new_chromosome);
                    new_fitness = evaluate(new_chromosome, new_backpack);
                }
                // Optimize feasible individual with insufficient backpack resource utilization
                op_hybrid_optimize(new_chromosome, new_backpack);
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

    /* Chromosome evaluation
     * input:  Chromosome to be evaluated, backpack to be updated
     * output: total value of the chromosome
     * Evaluate the chromosome. Calculate the total value and backpack.
     * If the backpack is less than capacity, update the backpack and return the total value.
     * Set the backpack to 0 and return 0, if over.
     */
    public int evaluate(int[] chromosome, int[] backpack) {
        // Total value vv and total volume bb corresponding to chromosome
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
            // Exceeds backpack volume
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

    // Calculate the cumulative probability of each individual in the population, provided that the fitness[max] of each individual has been calculated, as part of the roulette selection strategy, Pi[max]
    float[] countRate(int[] fitness, int scale) {
        int k;
        double sumFitness = 0; // Total fitness

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
        } else {
            pi[0] = (float) (1. / scale);
            for (k = 1; k < scale; k++) {
                pi[k] = (float) (1. / scale + pi[k - 1]);
            }
        }

        return pi;
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
            bestT = t; // The generation when the best chromosome appeared;
            for (i = 0; i < LL; i++) {
                bestTour[i] = oldPopulation[maxid][i];
            }
            for (i = 0; i < dimension; i++) {
                bestBackpack[i] = backpack[maxid][i];
            }
        }

        return maxid;
    }

    public void best_pop_select() {
        int[] new_fitness = new int[scale];
        int[][] new_backpack = new int[scale][dimension];
        // Calculate population fitness
        for (int k = 0; k < scale; k++) {
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            if (new_fitness[k] == 0) {
                // Repair infeasible individual
                op_repair(newPopulation[k]);
                new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
            }
        }
        for (int k = 0; k < scale; k++) {
            // Optimize feasible individual with insufficient backpack resource utilization
            op_hybrid_optimize(newPopulation[k], new_backpack[k]);
            new_fitness[k] = evaluate(newPopulation[k], new_backpack[k]);
        }
        sortPopulationByFitness(newPopulation, new_fitness, new_backpack);

        // Merge sorting
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

        // Select the best scale individuals as the next generation population
        System.arraycopy(mergedPopulation, 0, oldPopulation, 0, scale);
        System.arraycopy(mergedFitness, 0, fitness, 0, scale);
        System.arraycopy(mergedBackpack, 0, backpack, 0, scale);
    }

    // Uniform crossover
    void OXCross_Uniform() {
        for (int i = 0; i < scale; i += 2) {
            // Randomly select two parent individuals
            int parent1Index = random.nextInt(scale);
            int parent2Index = random.nextInt(scale);

            // Crossover operation
            if (random.nextDouble() < Pc) {
                for (int j = 0; j < LL; j++) {
                    // Uniform crossover
                    if (random.nextBoolean()) {
                        newPopulation[i][j] = oldPopulation[parent1Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent2Index][j];
                    } else {
                        newPopulation[i][j] = oldPopulation[parent2Index][j];
                        newPopulation[i + 1][j] = oldPopulation[parent1Index][j];
                    }
                }
            } else {
                // If no crossover occurs, offspring individuals are the same as parent individuals
                newPopulation[i] = oldPopulation[parent1Index].clone();
                newPopulation[i + 1] = oldPopulation[parent2Index].clone();
            }
        }
    }

    // Bitwise mutation operator, record new offspring separately
    public void OnCVariation_Bitwise() {
        for (int i = 0; i < scale; i += 1) {
            for (int j = 0; j < LL; j += 1) {
                if (random.nextDouble() < Pm) {
                    // If the random number is less than the mutation probability, perform bit mutation
                    newPopulation[i][j] = (newPopulation[i][j] == 0) ? 1 : 0; // 0 mutated to 1, 1 mutated to 0
                }
            }
        }
    }

    public boolean solve(String local_search, int local_times, int T_arg, float Tf_arg, float k_arg) throws IOException {
        int i;
        int k;

        // Start time
        long start_time = System.currentTimeMillis();

        // Initialize population
        initGroup();
        // Calculate initial population fitness, Fitness[max]
        for (k = 0; k < scale; k++) {
            fitness[k] = evaluate(oldPopulation[k], backpack[k]);
            if (fitness[k] == 0) {
                // Repair infeasible individual
                op_repair(oldPopulation[k]);
                fitness[k] = evaluate(oldPopulation[k], backpack[k]);
            }
            // System.out.println(fitness[k]);
        }
        for (k = 0; k < scale; k++) {
            // Optimize feasible individual with insufficient backpack resource utilization
            op_hybrid_optimize(oldPopulation[k], backpack[k]);
            fitness[k] = evaluate(oldPopulation[k], backpack[k]);
        }
        // Sort in descending order based on fitness
        sortPopulationByFitness(oldPopulation, fitness, backpack);

        // Calculate cumulative probability of each individual in the initial population, Pi[max]
        Pi = countRate(fitness, scale);

        // Iterative evolution
        int equal_count = 0;
        //for (t = 0; t < MAX_GEN; t++) {
        while (t < MAX_GEN && equal_count < 5) {
            // Find the individual with the highest fitness in a certain generation of the population
            findBestGh();

            // Crossover method
            OXCross_Uniform();

            // Mutation method
            OnCVariation_Bitwise();

            // Copy the new population newGroup to the old population oldGroup, preparing for the next generation evolution
            // Select individuals from newPopulation (and oldPopulation) to enter the parent population oldPopulation, preparing for the next generation evolution
            best_pop_select();

            if (Objects.equals(local_search, "random-local-search")) {
                // Random greedy local search module
                for (i = 0; i < scale; i++) {
                    fitness[i] = op_local_search(oldPopulation[i], fitness[i], backpack[i], local_times);
                }
                sortPopulationByFitness(oldPopulation, fitness, backpack);
            } else {
                // SA local search module
                for (i = 0; i < scale; i++) {
                    fitness[i] = op_sa_local_search(oldPopulation[i], fitness[i], backpack[i], local_times,
                            T_arg, Tf_arg, k_arg); //200, 0.1, 0.9);//200, 0.001, 0.99);
                }
                sortPopulationByFitness(oldPopulation, fitness, backpack);
            }

            // Calculate cumulative probability of each individual in the population
            Pi = countRate(fitness, scale);

            t++;
            if (Math.abs(fitness[0] - fitness[scale - 1]) == 0) {
                equal_count++;
            } else {
                equal_count = 0;
            }

            // End time
            long end_time = System.currentTimeMillis();
            totalTime_ms = end_time - start_time;
            if (totalTime_ms > 3600000) {
                System.out.println("Timeout!");
                return false;
            }
        }

        String dir = "output/";
        File log_dir = new File(dir);
        if (!(log_dir.exists())) {
            log_dir.mkdir();
        }
        String filename = "result" + "_"+ test_case + ".txt";
        File file = new File(dir + filename);
        // If the file does not exist, create the file
        if (!file.exists())
            file.createNewFile();
        // Create FileWriter object
        FileWriter writer = new FileWriter(file);

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        writer.write(formatter.format(date) + "\n");

        System.out.println("Best encoding generation:");
        System.out.println(bestT);
        // Write to the file
        writer.write("Best encoding generation:\n" + bestT + "\n");

        System.out.println("Best encoding value");
        System.out.println(bestLength);
        // Write to the file
        writer.write("Best encoding value:\n" + bestLength + "\n");

        System.out.println("Best encoding:");
        // Write to the file
        writer.write("Best encoding:\n");
        for (i = 0; i < LL; i++) {
            System.out.print(bestTour[i] + ",");
            // Write to the file
            writer.write(bestTour[i] + ",");
        }
        System.out.println();
        // Write to the file
        writer.write("\n");

        System.out.println("Best encoding weight:");
        // Write to the file
        writer.write("Best encoding weight:\n");
        for (i = 0; i < dimension; i++) {
            System.out.print(bestBackpack[i] + " ");
            // Write to the file
            writer.write(bestBackpack[i] + " ");
        }

        System.out.println("\nExecution generations:");
        System.out.println(t);
        // Write to the file
        writer.write("\nExecution generations:\n" + t + "\n");

        System.out.println("Execution time:");
        System.out.println(totalTime_ms + " ms");
        // Write to the file
        writer.write("Execution time:\n" + totalTime_ms + "\n");

        writer.flush();
        writer.close();

        // Write population average value change curve
        String avg_filename = "result" + "_" + test_case.split(".dat")[0] + ".avg.txt";
        File avg_file = new File(dir + avg_filename);
        // If the file does not exist, create the file
        if (!avg_file.exists())
            avg_file.createNewFile();
        // Create FileWriter object
        writer = new FileWriter(avg_file);
        for (double f : averageFitnessList) {
            writer.write(f + " ");
        }
        writer.flush();
        writer.close();

        // Write population maximum value change curve
        String max_filename = "result" + "_" + test_case.split(".dat")[0] + ".max.txt";
        File max_file = new File(dir + max_filename);
        // If the file does not exist, create the file
        if (!max_file.exists())
            max_file.createNewFile();
        // Create FileWriter object
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
        String file_path_arg = args[0];//"testcases/chubeas/OR5x250/OR5x250-0.25_1.dat";
        int scale_arg = 100;
        float Pc_arg = 0.9f;
        float Pm_arg = 0.05f;
        float Ph_arg = 0.6f;
        int local_times_arg = 0;
        String local_search_arg;
        if (Objects.equals(args[1], "random-local-search")) {
            local_search_arg = "random-local-search";
            local_times_arg = 20;
        } else {
            local_search_arg = "sa-local-search";
            local_times_arg = 10;
        }
        int T_arg = 140;
        float Tf_arg = 0.1f;
        float k_arg = 0.98f;

        String[] tmp_str = file_path_arg.split("/");
        String test_case_name = tmp_str[tmp_str.length - 1];
        System.out.println("Start.... " + test_case_name + " " + local_search_arg);

        IntegerTestCase tc = new IntegerTestCase();
        tc.readTestCase(file_path_arg);

        boolean success;
        KP_IGA ga = new KP_IGA(scale_arg, tc, 5000, Pc_arg, Pm_arg, Ph_arg, test_case_name);
        ga.init();
        success = ga.solve(local_search_arg, local_times_arg, T_arg, Tf_arg, k_arg);
    }
}