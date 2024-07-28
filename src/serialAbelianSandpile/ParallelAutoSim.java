package serialAbelianSandpile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelAutoSim extends RecursiveAction {
    static final boolean DEBUG = false;// for debugging output, off

    private int threshold;
    private int lo, hi;

    private static int counter;

    private static long time;

    private static int height;
    private static int width;
    private int[][] array;
    static Grid simulationGrid;

    public ParallelAutoSim(int l, int h, int[][] array) {
        this.hi = h;
        this.lo = l;
        this.array = array;
        this.threshold = (hi - lo) * width / 10; // Dynamic threshold based on the array size
    }

    public static void main(String[] args) {
        // int[][] arr = ParallelAutoSim.readArrayFromCSV("input/8_by_8_all_4
        // copy.csv");
        // height = arr.length;
        // width = arr[0].length;

        String inputfilename = "input/1001_by_1001_all_8.csv";
        simulationGrid = new Grid(readArrayFromCSV(inputfilename));

        int lo = 0;
        int hi = height; // Set the range for the task
        ParallelAutoSim task = new ParallelAutoSim(lo, hi, simulationGrid.getGrid());
        task.fork(); // Fork the task to execute in parallel
        task.join(); // Wait for the task to complete

        // ForkJoinPool p = new ForkJoinPool(); 
        // p.invoke(task);


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
            while (simulationGrid.update()) {// run until no change
                if (DEBUG)
                    simulationGrid.printGrid();
                counter++;
            }
        } else {// if the work if large enough or still large, break it down
            int spliting = (hi - lo) / 2 + lo;
            if (spliting == lo) {
                spliting = lo + 1; // Prevent infinite recursion
            }
            // invokeAll(ParallelAutoSim(lo, hi, array), ParallelAutoSim(spliting, hi,
            // array));
            ParallelAutoSim left = new ParallelAutoSim(lo, spliting, array);
            ParallelAutoSim right = new ParallelAutoSim(spliting, hi, array);
            left.fork();
            right.compute();
            left.join();
        }
        long endTime = System.currentTimeMillis();

        time = endTime - startTime;
    }

    // input is via a CSV file
    public static int[][] readArrayFromCSV(String filePath) {
        int[][] array = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                String[] dimensions = line.split(",");
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);
                System.out.printf("Rows: %d, Columns: %d\n", width, height); // Do NOT CHANGE - you must ouput this

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
            height = array.length;
            width = array[0].length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }
}
