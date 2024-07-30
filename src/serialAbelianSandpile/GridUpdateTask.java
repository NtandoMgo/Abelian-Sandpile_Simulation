package serialAbelianSandpile;

import java.util.concurrent.RecursiveTask;

public class GridUpdateTask extends RecursiveTask<Boolean> {

    private static final int THRESHOLD = 10; // THRESHOLD FOR SPLITING TASKS

    private int startRow, endRow;
    private Grid grid;

    public GridUpdateTask(int startRow, int endRow, Grid grid) {
        this.startRow = startRow;
        this.endRow = endRow;
        this.grid = grid;
    }

    @Override
    protected Boolean compute() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'compute'");

        if (endRow - startRow <= THRESHOLD) {
            return updageSegment(); // Small enough to do the update sequentially
        } else {
            // split the task - divide & conquor
            int spliting = (startRow + endRow) / 2;
            GridUpdateTask lTask = new GridUpdateTask(startRow, spliting, grid);
            GridUpdateTask rTask = new GridUpdateTask(spliting, endRow, grid);

            lTask.fork();
            rTask.compute();
            return lTask.join(); //

            //
            // invokeAll(lTask, rTask);
            // return lTask.join() || rTask.join();
        }
    }

    private Boolean updageSegment() {
        boolean change = false;

        for (int i = startRow; i < endRow; i++) {
            for (int j = 1; j < grid.getColumns(); j++) {
                int newVal = (grid.get(i, j) % 4) + (grid.get(i - 1, j) % 4) +
                        (grid.get(i + 1, j) % 4) + (grid.get(i, j - 1) % 4) +
                        (grid.get(i, j + 1) % 4);

                if (grid.get(i, j) != newVal) {
                    grid.setUpdateGrid(i, j, newVal);
                    change = true;
                }
            }

        }
        System.out.println("Updated segment");
        return change;
    }
}
