# Genetic Algorithm for Solving the Multidimensional Knapsack Problem

## Overview

This project explores solving the **Multidimensional Knapsack Problem (MKP)** using an **improved genetic algorithm (IGA)**. The MKP is a combinatorial optimization problem that extends the classic knapsack problem by adding multiple resource constraints, making it more challenging and applicable to real-world scenarios such as logistics, finance, and resource allocation. The improved genetic algorithm enhances traditional methods with specialized operators, hybrid optimization and local search strategies to achieve better solutions.

## Method

### Problem Definition
The **MKP** is defined by selecting items with specific values and multiple resource constraints to maximize the total value while adhering to capacity limits across all dimensions.

### Genetic Algorithm Design
The improved genetic algorithm includes the following key components:

- **Encoding**: Uses binary encoding to represent item selection, where each bit indicates whether an item is included in the knapsack.
- **Genetic Operators**:
  - Employs **uniform crossover** to ensure diverse offspring.
  - Utilizes **bitwise mutation** to introduce variability.
  - Introduces **best selection** to maintain population diversity.
- **Repair and Optimization Operators**: Ensure feasibility of solutions by adjusting the selection to fit within capacity constraints while maximizing value.
    - **Repair Operators**: The purpose of repair operators is to adjust these solutions back into the feasible space. This project employs a **greedy repair operator**:
        - It generates a list of items sorted in descending order based on their value density.
        - For a solution that does not meet the capacity constraints, the repair operator iteratively removes items with the lowest value density until the solution satisfies all dimension constraints.
        - This approach ensures that high value-density items are retained as much as possible during the repair process, preserving the quality of the solution.

    - **Optimization Operators**: Optimization operators are used to improve the quality of feasible solutions by increasing their total value while keeping them within capacity constraints. This project implements **hybrid greedy optimization operator**:
        - To avoid local optima traps caused by focusing solely on value density, the hybrid greedy optimization operator introduces a probabilistic approach that balances absolute item value and value density. 
        - It randomly selects whether to add items based on their value density or absolute value, allowing for more flexible adjustments. 
        - This approach enhances global search capabilities by balancing the preference for high-value and high-value-density items.

- **Local Search Mechanism**: Implements simulated annealing and random local search to refine solutions, balancing global and local exploration.
    - **Random Local Search Operator**: During each generation of the genetic algorithm, a random local search is applied to each individual in the population. This process involves randomly selecting and flipping a gene (item selection state):
        - If the modified solution has a higher fitness (total value) than the original, the new solution is accepted; otherwise, the original solution is retained.
        - This method allows for minor adjustments in the vicinity of a solution, helping the algorithm escape local optima and potentially discover better solutions through multiple iterations.

    - **Simulated Annealing Local Search Operator**: The simulated annealing approach introduces a probabilistic acceptance mechanism to avoid being trapped in local optima:
        - During each local search, a random perturbation is applied to the current solution, and the decision to accept the new solution is based on the change in fitness and a temperature parameter.
        - Initially, the algorithm is more likely to accept inferior solutions, which helps explore a wider range of the solution space; as the temperature decreases, the algorithm gradually favors better solutions, leading to convergence.
        - This strategy mimics the physical annealing process, allowing the algorithm to explore broadly in early stages and refine solutions in later stages, achieving a balance between global exploration and local optimization.
    - This experiment analysis indicates that both methods have their advantages:
        - The **random local search operator** can achieve superior results but tends to be less stable, making it suitable when the goal is to find the best possible solution across multiple runs.
        - The **SA local search operator** is more stable and consistently achieves good solutions, making it ideal when fewer runs are available or when a reliable solution is preferred.

    
    | Benchmark         | Random Local Search Operator |                | SA Local Search Operator |                |
    |-------------------|-----------------------------|----------------|--------------------------|----------------|
    |                   | Average Error      | Best Error | Average Error   | Best Error |
    | OR5×250-0.25      | 0.18                        | **0.12**           | **0.13**                     | 0.13           |
    | OR10×250-0.25     | 0.32                        | 0.11           | **0.13**                     | **0.08**           |
    | OR30×250-0.25     | 0.59                        | **0.26**           | **0.50**                     | 0.50           |



## Performance Evaluation
To further evaluate the effectiveness of the **Improved Genetic Algorithm (IGA)**, we conducted comparative tests against existing advanced genetic algorithm variants for MKP: **Guided Genetic Algorithm (GGA)**, **Improved Hybrid Genetic Algorithm (IHGA)**, and **Hybrid Greedy Genetic Algorithm (HGGA)**. The tests were performed on 18 benchmark instances from the **Chu & Beasley** dataset, covering small, medium, and large-scale problems.
- The IGA consistently achieves the lowest **average relative error**  and **best relative error** across the benchmarks, confirming its robustness and stability in solving MKP.
- Compared to GGA, IHGA, and HGGA across different problem scales, the IGA shows a significant improvement, reducing average relative error by **19.8**, **27.2**, and **22.1** times, respectively.

| Benchmark             | IGA           |                | GGA           |                | IHGA           |                 | HGGA           |                 |
|-----------------------|---------------|----------------|---------------|----------------|----------------|-----------------|----------------|-----------------|
|                       | Avg Error     | Best Error     | Avg Error     | Best Error     | Avg Error      | Best Error      | Avg Error      | Best Error      |
| OR10x100-0.25_1       | **0.06**      | **0.00**       | 0.81          | 0.20           | 2.44           | 0.04            | 1.35           | 0.05            |
| OR10x100-0.25_10      | **0.01**      | **0.00**       | 1.02          | **0.00**       | 1.84           | 0.01            | 1.34           | 0.64            |
| OR10x100-0.50_1       | **0.20**      | **0.02**       | 0.67          | 0.24           | 1.62           | 0.88            | 0.97           | 0.26            |
| OR10x100-0.50_10      | **0.17**      | **0.00**       | 0.47          | 0.27           | 1.32           | 0.47            | 0.85           | 0.33            |
| OR10x100-0.75_1       | **0.00**      | **0.00**       | 0.31          | 0.23           | 0.59           | **0.00**        | 0.27           | 0.01            |
| OR10x100-0.75_10      | **0.00**      | **0.00**       | 0.09          | **0.00**       | 0.38           | 0.05            | 0.16           | 0.01            |
| OR10x250-0.25_1       | **0.28**      | **0.09**       | 0.92          | 0.39           | 1.35           | 0.64            | 1.05           | 0.53            |
| OR10x250-0.25_10      | **0.21**      | **0.00**       | 0.88          | 0.61           | 1.28           | 0.70            | 0.96           | 0.42            |
| OR10x250-0.50_1       | **0.31**      | **0.16**       | 0.56          | 0.26           | 1.05           | 0.60            | 0.61           | 0.34            |
| OR10x250-0.50_10      | **0.29**      | **0.12**       | 0.49          | 0.23           | 1.05           | 0.52            | 0.56           | 0.26            |
| OR10x250-0.75_1       | **0.06**      | **0.00**       | 0.28          | 0.14           | 0.41           | 0.12            | 0.26           | 0.14            |
| OR10x250-0.75_10      | **0.09**      | **0.05**       | 0.29          | 0.08           | 0.48           | 0.27            | 0.28           | 0.13            |
| OR10x500-0.25_1       | **0.32**      | **0.16**       | 1.01          | 0.58           | 1.03           | 0.60            | 0.79           | 0.37            |
| OR10x500-0.25_10      | **0.28**      | **0.05**       | 0.88          | 0.57           | 0.85           | 0.46            | 0.66           | 0.27            |
| OR10x500-0.50_1       | **0.25**      | **0.11**       | 0.46          | 0.28           | 0.64           | 0.30            | 0.41           | 0.24            |
| OR10x500-0.50_10      | **0.21**      | **0.14**       | 0.54          | 0.32           | 0.58           | 0.25            | 0.32           | 0.18            |
| OR10x500-0.75_1       | **0.10**      | **0.05**       | 0.26          | 0.12           | 0.40           | 0.23            | 0.19           | 0.11            |
| OR10x500-0.75_10      | **0.09**      | **0.06**       | 0.26          | 0.13           | 0.38           | 0.14            | 0.16           | 0.08            |



These results highlight the strength of the IGA in addressing the complexities of the **Multidimensional Knapsack Problem**, offering a balance between exploration and exploitation that allows it to outperform other state-of-the-art genetic algorithm variants.

## Installation

To run the project, ensure you have Python 3.8+ and necessary dependencies:

1. Clone the repository:
   ```bash
   git clone https://github.com/HareetX/KP_GA.git
   cd mkp-genetic-algorithm
   ```
2. Install JDK and Gradle:
- Make sure that you have intalled JDK and Gradle on your system.
3. Build the code:
   ```bash
   gradle build
   ```
5. Run the application:
   ```bash
   java -jar ./build/libs/KP_GA-1.0-SNAPSHOT.jar <testcase_path> [random-local-search|sa-local-search]
   ```
