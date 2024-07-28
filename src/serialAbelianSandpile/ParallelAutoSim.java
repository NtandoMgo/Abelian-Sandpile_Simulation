package serialAbelianSandpile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.RecursiveAction;

import serialAbelianSandpile.Grid;

public class ParallelAutoSim extends RecursiveAction {
    static final boolean DEBUG = false; // for debugging output, off

    private int threshold = 10;
    private int lo, hi;
    private int[][] array;
    static Grid simulationGrid;
    static int height;
    static int width;
    private static long time;
    static int counter = 0;

    public ParallelAutoSim(int l, int h, int[][] array) {
        this.lo = l;
        this.hi = h;
        this.array = array;
    }

    public static void main(String[] args) {
        String inputfilename = "input/8_by_8_all_4 copy.csv";
        simulationGrid = new Grid(readArrayFromCSV(inputfilename));
        
        int lo = 0;
        int hi = height; // Set the range for the task
        ParallelAutoSim task = new ParallelAutoSim(lo, hi, simulationGrid.getGrid());
        task.fork(); // Fork the task to execute in parallel
        task.join(); // Wait for the task to complete

        if (DEBUG) {
            simulationGrid.printGrid();
        }

        // System.out.println("Parallel computation completed.");
        System.out.println("Simulation complete, writing image...");
        try {
            if (inputfilename.equals("input/8_by_8_all_4 copy.csv")) {
                simulationGrid.gridToImage("output/outputFileparallel-8_by_8.png");
            } else if (inputfilename.equals("input/16_by_16_all_4.csv")) {
                simulationGrid.gridToImage("output/outputFileparallel-16_by_16_all_4.png");
            } else if (inputfilename.equals("input/16_by_16_one_100.csv")) {
                simulationGrid.gridToImage("output/outputFileparallel-16_by_16_one_100.png");
            } else if (inputfilename.equals("input/65_by_65_all_4.csv")) {
                simulationGrid.gridToImage("output/outputFileparallel-65_by_65_all_4.png");
            } else if (inputfilename.equals("input/517_by_517_centre_534578.csv")) {
                simulationGrid.gridToImage("output/outputFileparallel-517_by_517_centre_534578.png");
            } else if (inputfilename.equals("input/1001_by_1001_all_8.csv")) {
                simulationGrid.gridToImage("output/outputFileparallel-1001_by_1001_all_8.png");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            System.out.println("could not print file!");
        }
        System.out.printf("Number of steps to stable state: %d \n", counter);
        System.out.printf("Time: %d ms\n", time);
    }

    @Override
    protected void compute() {
        long startTime = System.currentTimeMillis();
        if ((hi - lo) * width < threshold) {
            if (DEBUG) {
                System.out.printf("starting config: %d \n", counter);
                simulationGrid.printGrid();
            }
            while (simulationGrid.update()) { // run until no change
                if (DEBUG)
                    simulationGrid.printGrid();
                counter++;
            }
        } else { // if the work is large enough or still large, break it down
            int splitting = (hi - lo) / 2 + lo;
            ParallelAutoSim left = new ParallelAutoSim(lo, splitting, array);
            ParallelAutoSim right = new ParallelAutoSim(splitting, hi, array);
            left.fork();
            right.compute();
            left.join();
        }
        long endTime = System.currentTimeMillis();

        time = endTime - startTime;
    }

    public static int[][] readArrayFromCSV(String filePath) {
        int[][] array = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                String[] dimensions = line.split(",");
                width = Integer.parseInt(dimensions[0]);
                height = Integer.parseInt(dimensions[1]);
                System.out.printf("Rows: %d, Columns: %d\n", height, width);

                array = new int[height][width];
                int rowIndex = 0;

                while ((line = br.readLine()) != null && rowIndex < height) {
                    String[] values = line.split(",");
                    for (int colIndex = 0; colIndex < width; colIndex++) {
                        array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
                    }
                    rowIndex++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }
}
