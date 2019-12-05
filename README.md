# Array drop, split, sum

## Test to implement
In an array, drop two items & find three contiguous sub-arrays that sum the same result.
The array can be up to 100000 items.

## Tech description 

For each drop tuple, a thread runs to check if the result array has 3 contiguous sub-arrays summing same result. 

- Main.java - unit & performance tests.
- Solution.java - algorithm implementation.
- TaskManager.java - helper for ExecutorService & ForkJoinPool (threads).

## Build & Run

    mvn clean install
    java -cp target/array-drop-split-sum-1.0-SNAPSHOT.jar com.dgf.arrayDropSplitSum.Main
