package serialAbelianSandpile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelAutoSimulation extends RecursiveAction {

    static final boolean DEBUG = false; // for debiging output

    private static int threshold = 80;

    static long startTime = 0;
    static long endtime = 0;

    static Grid grid;
    int lo, hi, width;

    public ParallelAutoSimulation(Grid g, int l, int h) {
        grid = g;
        lo = l;
        hi = h;
        width = g.getRows();
    }

    private static void tick() {
        startTime = System.currentTimeMillis();
    }

    private static void tock() {
        endtime = System.currentTimeMillis();
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
                System.out.printf("Rows: %d, Columns: %d\n", width, height); // Do NOT CHANGE - you must output this

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

    public static void main(String[] args) {
        Grid simGrid; // the cellular automaton grid

        //String iFileName = "input/65_by_65_all_4.csv"; // input file name
        //String oFileName = "outputtest/65-parallel.png"; // output file name

        if (args.length!=2) {   //input is the name of the input and output files
    		System.out.println("Incorrect number of command line arguments provided.");   	
    		System.exit(0);
    	}

        String iFileName = args[0];
        String oFileName = args[1];

        // Read from input .csv file
        simGrid = new Grid(readArrayFromCSV(iFileName));

        ParallelAutoSimulation task = new ParallelAutoSimulation(simGrid, 0, simGrid.getColumns());
        ForkJoinPool p = new ForkJoinPool();
        p.invoke(task);

        int counter = 0;
        tick(); // start timer
        if (DEBUG) {
            System.out.printf("starting config: %d \n", counter);
            grid.printGrid();
        }
        while (grid.update()) {// run until no change
            if (DEBUG)
                grid.printGrid();
            counter++;
        }
        tock();

        int noThreads = Runtime.getRuntime().availableProcessors();

        System.out.println("Simulation complete, writing image...");
        try {
            grid.gridToImage(oFileName);
        } catch (IOException e) {
            System.out.println("Couldn't write image");
        } 
        
        // write grid as an image
        System.out.printf("Number of steps to stable state: %d \n", counter);
        System.out.printf("Number of threads: %d \n", noThreads);
        System.out.printf("Time: %d ms\n", endtime - startTime);
    }

    public static boolean update() {
        boolean change = false;
        // do not update border
        for (int i = 1; i < grid.getRows() - 1; i++) {
            for (int j = 1; j < grid.getColumns() - 1; j++) {
                // updateGrid[i][j]
                int val = (grid.getGrid()[i][j] % 4) +
                        (grid.getGrid()[i - 1][j] / 4) +
                        grid.getGrid()[i + 1][j] / 4 +
                        grid.getGrid()[i][j - 1] / 4 +
                        grid.getGrid()[i][j + 1] / 4;
                grid.setUpdateGrid(i, j, val);

                if (grid.getGrid()[i][j] != grid.getUpdateGrid(i, j)) {
                    change = true;
                }
            }
        } // end nested for
        if (change) {
            grid.nextTimeStep();
        }
        return change;
    }

    @Override
    protected void compute() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'compute'");

        if ((hi - lo) * width < threshold) {
            update();
        } else {
            int split = (hi - lo) / 2 + lo;
            ParallelAutoSimulation lTask = new ParallelAutoSimulation(grid, lo, split);
            ParallelAutoSimulation rTask = new ParallelAutoSimulation(grid, split, hi);
            //System.out.println("before fork");
            lTask.fork();
            //System.out.println("After fork-before compute");
            rTask.compute();
            //System.out.println("After compute");
            lTask.join();
        }
    }
}
