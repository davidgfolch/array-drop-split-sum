package com.dgf.travelperk;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Solution {

    private static final int MIN_ARR_LEN = 5;
    private static final int MAX_ARR_LEN = 100000;
    private static final String ARRAY_NOT_IN_RANGE = "Array not in range "+MIN_ARR_LEN+"-"+MAX_ARR_LEN+" items: ";
//    private static final String FIRST_DROP_D_MILLIS_D = "\r1rst Drop %d, millis: %d";
//    private static final String CAN_T_EXECUTE_WORKER = "Can't execute worker";

    private int drop1;
    private int drop2;

    public boolean run(int[] a) {
        //a size 5-100.000
        //drop 2 items
        //split in 3 contiguous arrays
        //if sums return same values return true
        //else return false
        if (a.length< MIN_ARR_LEN || a.length> MAX_ARR_LEN){
            System.out.println(ARRAY_NOT_IN_RANGE + Arrays.toString(a));
            return false;
        }
        if (a.length>6000) {
            return processBigArr(a);
        } else {
            return processSmallArr(a);
        }
    }

    private boolean processBigArr(int[] a) {
//        long end;
//        long start;
        final AtomicBoolean solutionFound= new AtomicBoolean(false);
        final long[] preSumSegments=preSumSegments(a);
        for (drop1 = 0; drop1 < a.length - 1; drop1++) {
//            start=System.currentTimeMillis();
            for (drop2 = drop1 +1; drop2 <a.length; drop2++) {
//                drop1= a.length/3;  //todo remove (to debug only)
//                drop2= a.length-drop1;
                final int finalDrop1 = drop1;
                final int finalDrop2 = drop2;
                Runnable worker = () -> {
                    if (solutionFound.get())
                        return;
                    if (checkSumsBySegments(a,preSumSegments, finalDrop1, finalDrop2)) {
                        solutionFound.set(true);
                    }
                };
                if (solutionFound.get()) {
                    TaskManager.waitForPendentTasks();
                    return true;
                } else {
                    //System.out.print("\rexecutor="+executorService);
                    TaskManager.execute(worker);
                    TaskManager.prioritizeTasks();
                }
            }
//            end=System.currentTimeMillis();
//            System.out.print(String.format(FIRST_DROP_D_MILLIS_D, drop1, end - start) + " " +forkJoinPool);
//            System.out.flush();
        }
        TaskManager.waitForPendentTasks();
        return solutionFound.get();
    }

    private long[] preSumSegments(int[] arr) {
        long totalSum=0;
        long partialSum=0;
        long[] preSumSegments=new long[arr.length/10];  //todo fix division factor
        int factor=arr.length/preSumSegments.length;
        int i, j=0;
        for (i=0; i<arr.length; i++) {
            totalSum += arr[i];
            if (i>0 && i%factor==0) {
                preSumSegments[j++] = partialSum;
                partialSum=0;
            }
            partialSum += arr[i];
        }
        preSumSegments[preSumSegments.length-1]=totalSum;
        long res1=LongStream.of(Arrays.copyOfRange(preSumSegments,0,preSumSegments.length-1)).sum();
        long res2=IntStream.of(Arrays.copyOfRange(arr,(preSumSegments.length-1)*factor,arr.length)).sum();
        if (totalSum!=res1+res2) {
            throw new RuntimeException("Invalid partial sums");
        }
        System.out.println("Summing by segments approach segments arr="+Arrays.toString(preSumSegments));
        return preSumSegments;
    }

    private long preSum(int[] arr) {
        long totalSum=0;
        for (int i : arr) totalSum += i;
        return totalSum;
    }

    private boolean processSmallArr(int[] a) {
        for (drop1 = 0; drop1 < a.length - 1; drop1++) {
            for (drop2 = drop1 +1; drop2 <a.length; drop2++) {
                final int[] b = removeItems(a, drop1, drop2);
                if (checkSums(b, preSum(b))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int[] removeItems(int[] a, int drop1, int drop2) {
        int[] b=a.clone();
        b[drop1]=0;
        b[drop2]=0;
//        int[] b=new int[a.length-2];
//        System.arraycopy(a,0,b,0,drop1);
//        System.arraycopy(a,drop1+1,b,drop1,drop2-drop1-1);
//        System.arraycopy(a,drop2+1,b,drop2-1,b.length-drop2+1);
        return b;
    }

    static boolean checkSums(int[] arr, final long totalSum) {
        Boolean preCheck = preCheck(totalSum);
        if (preCheck != null) return preCheck;
        long preSum=0;
        int split1=-1;
        int count;
        long sum1=totalSum/3;
        long sum2=2*sum1;
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

    private static Boolean preCheck(long totalSum) {
        if (totalSum%3!=0)
            return false;
        if (totalSum==0)
            return true;
        return null;
    }

    static boolean checkSumsBySegments(int[] origArr, final long[] preSumSegments, int drop1, int drop2) {
        final long totalSum=preSumSegments[preSumSegments.length-1]-origArr[drop1]-origArr[drop2];
        Boolean preCheck = preCheck(totalSum);
        if (preCheck != null) return preCheck;
        int i, j, arrPos1, arrPos2, split1=-1, factor=origArr.length/preSumSegments.length;
        long sum1=totalSum/3,
                sum2=2*sum1,
                segmentValue,
                segmentSum=0,
                unitSum;
        final int[] arr = removeItems(origArr,drop1,drop2);
        for (i=0; i<preSumSegments.length-1; i++) {
            arrPos1 = i * factor;
            arrPos2 = (i+1)*factor;
            if (drop1>=arrPos1 && drop1<arrPos2)
                segmentValue=preSumSegments[i]-origArr[drop1];
            else if (drop2>=arrPos1 && drop2<arrPos2)
                segmentValue=preSumSegments[i]-origArr[drop1];
            else segmentValue=preSumSegments[i];
            segmentSum+=segmentValue;
            if (segmentSum>0) {
                if (split1 == -1) {
                    if (segmentSum>sum1) {
                        unitSum=segmentSum-segmentValue;
                        for (j=i*factor;j<(i+1)*factor;j++) {
                            unitSum+=arr[j];
                            if (unitSum==sum1) {
                                split1 = j;
                                break;
                            }
                            if (unitSum>sum1)
                                return false;
                        }
                    } else if (segmentSum==sum1)
                        split1 = i*factor;
                } else if (segmentSum>sum2) {
                    unitSum=segmentSum-segmentValue;
                    for (j=i*factor;j<arr.length;j++) {
                        unitSum+=arr[j];
                        if (unitSum==sum2)
                            return true;
                        if (unitSum>sum2)
                            return false;
                    }
                } else if (segmentSum==sum2) {
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
