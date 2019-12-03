package com.dgf.travelperk;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Solution {

    private static final int _100000 = 100000;
//    private static final String FIRST_DROP_D_MILLIS_D = "\r1rst Drop %d, millis: %d";
    private static final String CAN_T_EXECUTE_WORKER = "Can't execute worker";

    private ForkJoinPool executor;

    private long end;
    private long start;

    private int drop1;
    private int drop2;

    public Solution(ForkJoinPool executor) {
        this.executor=executor;
    }

    public boolean run(int[] a) {
        //a size 5-100.000
        //drop 2 items
        //split in 3 contiguous arrays
        //if sums return same values return true
        //else return false
        if (a.length<5 || a.length>_100000){
            System.out.println("Array not in range 5-100000 items: "+ Arrays.toString(a));
            return false;
        }
        if (a.length>100) {
            return processBigArr(a);
        } else {
            return processSmallArr(a);
        }
    }

    private boolean processBigArr(int[] a) {
        final AtomicBoolean solutionFound= new AtomicBoolean(false);
        for (drop1 = 0; drop1 < a.length - 1; drop1++) {
            start=System.currentTimeMillis();
            for (drop2 = drop1 +1; drop2 <a.length; drop2++) {
                final int finalDrop1 = drop1;
                final int finalDrop2 = drop2;
                Runnable worker = () -> {
                    final int[] b = removeItems(a,finalDrop1,finalDrop2);
                    if (checkArrSplit(b)) {
                        solutionFound.set(true);
                    }
                };
                if (solutionFound.get()) {
                    return true;
                } else {
                    try {
//                        System.out.println("executor.getQueuedSubmissionCount(),executor.getParallelism()"+executor.getQueuedSubmissionCount()+","+executor.getParallelism());
                        executor.execute(worker);
                        if (executor.getQueuedSubmissionCount()>executor.getParallelism()*100) {
                            try {
//                                System.out.print("\rMain thread sleep for "+a.length+" -> executor "+executor);
                                Thread.sleep(a.length/10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (RejectedExecutionException e) {
                        if (!solutionFound.get())
                            throw new RuntimeException(CAN_T_EXECUTE_WORKER, e);
                    }
                }
            }
            end=System.currentTimeMillis();
//            System.out.print(String.format(FIRST_DROP_D_MILLIS_D, drop1, end - start) + executor);
//            System.out.flush();
        }
        return solutionFound.get();
    }

    private boolean processSmallArr(int[] a) {
        for (drop1 = 0; drop1 < a.length - 1; drop1++) {
            start=System.currentTimeMillis();
            for (drop2 = drop1 +1; drop2 <a.length; drop2++) {
                final int[] b = removeItems(a, drop1, drop2);
                if (checkArrSplit(b)) {
                    return true;
                }
            }
            end=System.currentTimeMillis();
            //System.out.print(String.format(FIRST_DROP_D_MILLIS_D, drop1,end-start));
        }
        return false;
    }

    private int[] removeItems(int[] a, int drop1, int drop2) {
        int[] b=new int[a.length-2];
        System.arraycopy(a,0,b,0,drop1);
        System.arraycopy(a,drop1+1,b,drop1,drop2-drop1-1);
        System.arraycopy(a,drop2+1,b,drop2-1,b.length-drop2+1);
        return b;
    }

    static boolean checkArrSplit(int[] arr) {
        int preSum=0;
        int split1=-1;
        int totalSum=0;
        int count;
        for (count=0; count<arr.length; count++)
            totalSum+=arr[count];
        if (totalSum%3!=0)
            return false;
        if (totalSum==0)
            return true;
        int sum1=totalSum/3;
        int sum2=2*sum1;
        for (count=0; count<arr.length; count++) {
            preSum+=arr[count];
            if (preSum>0) {
                if (split1 == -1 && preSum % sum1 == 0)
                    split1 = count;
                else if (preSum % sum2 == 0) {
//                    System.out.println("Found solution splits: " + split1 + "/" + count + " arr=" +
//                            Arrays.toString(Arrays.copyOfRange(arr, 0, split1 + 1)) +
//                            Arrays.toString(Arrays.copyOfRange(arr, split1 + 1, count + 1)) +
//                            Arrays.toString(Arrays.copyOfRange(arr, count + 1, arr.length))
//                    );
                    return true;
                }
            }
        }
        return false;
    }

}
