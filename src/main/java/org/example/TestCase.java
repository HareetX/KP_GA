package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class TestCase {
    int[] v;

    int[][] b;

    int[] pb;

    int LL;

    int dimension;

    boolean readTestCase(String filepath) throws IOException {
        FileReader reader = new FileReader(filepath);
        BufferedReader br = new BufferedReader(reader);
        String line;
        int line_num = 0;
        int item_count = 0;
        int dimension_count = 0;
        int array_count = 0;
        int v_array = 0;
        while ((line=br.readLine()) != null) {
            line_num += 1;

            String[] st_list_temp = line.split(" ");
            String[] st_list = Arrays.stream(st_list_temp).filter(s -> !"".equals(s)).toArray(String[]::new);
            int len = st_list.length;
            if (line_num == 1) {
                for (int i = 0; i < len; i++) {
                    if (st_list[i] != null) {
                        int n = Integer.valueOf(st_list[i]);
                        if (i == 0) {
                            LL = n;
                            v = new int[n];
                        } else if (i == 1) {
                            dimension = n;
                            b = new int[n][LL];
                            pb = new int[n];
                        } else if (i == 2) {
                            v_array = n;
                        }
                        else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            else {
                if (v_array == array_count) {
                    for (String s : st_list) {
                        if (s != null) {
                            int n = Integer.valueOf(s);
                            v[item_count] = n;
                            item_count += 1;
                            if (item_count >= LL) {
                                array_count += 1;
                                item_count = 0;
                            }
                        }
                    }
                }
                else if (array_count <= dimension) {
                    for (String s : st_list) {
                        if (s != null) {
                            int n = Integer.valueOf(s);
                            b[array_count - 1][item_count] = n;
                            item_count += 1;
                            if (item_count >= LL) {
                                array_count += 1;
                                item_count = 0;
                            }
                        }
                    }
                }
                else {
                    for (int i = 0; i < len; i++) {
                        if (st_list[i] != null) {
                            if (dimension_count < dimension) {
                                int n = Integer.valueOf(st_list[i]);
                                pb[dimension_count] = n;
                                dimension_count += 1;
                            }
                            else {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
