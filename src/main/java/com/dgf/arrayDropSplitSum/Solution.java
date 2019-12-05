package com.dgf.arrayDropSplitSum;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

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
        final AtomicBoolean solutionFound= new AtomicBoolean(false);
        final int[] preSumSegments=preSumSegments(a);
        for (drop1 = 0; drop1 < a.length - 1; drop1++) {
            System.out.print("\rFirst drop="+drop1+" executor="+ TaskManager.printTasks());
            for (drop2 = drop1 +1; drop2 <a.length; drop2++) {
//                drop1= a.length/3;  //to debug only
//                drop2= a.length-drop1;
                final int finalDrop1 = drop1;
                final int finalDrop2 = drop2;
                Runnable worker = () -> {
                    if (!solutionFound.get() && checkSumsBySegments(a,preSumSegments, finalDrop1, finalDrop2))
                        solutionFound.set(true);
                };
                if (solutionFound.get()) {
                    break;
                } else {
                    //System.out.print("\rexecutor="+executorService);
                    TaskManager.execute(worker);
                    TaskManager.prioritizeTasks();
                }
            }
            if (solutionFound.get())
                break;
        }
        TaskManager.waitForPendentTasks();
        return solutionFound.get();
    }

    private int[] preSumSegments(int[] arr) {
        System.out.print("\rPre-sum segments...");
        int totalSum=0;
        int partialSum=0;
        int[] preSumSegments=new int[arr.length/10];
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
        System.out.println("Summing by segments approach. Pre-sum segments arr size/values="+preSumSegments.length+"/"+Arrays.toString(preSumSegments));
        return preSumSegments;
    }

    private boolean checkSumsBySegments(int[] origArr, final int[] preSumSegments, int drop1, int drop2) {
        final int totalSum=preSumSegments[preSumSegments.length-1]-origArr[drop1]-origArr[drop2];
        Boolean preCheck = preCheck(totalSum);
        if (preCheck != null) return preCheck;
        int i, j, arrPosIni, arrPosEnd, split1=-1,
                factor=origArr.length/preSumSegments.length,
                sum1=totalSum/3,
                sum2=2*sum1,
                segmentValue,
                segmentSum=0,
                unitSum;
        final int[] arr = removeItems(origArr,drop1,drop2);
        for (i=0; i<preSumSegments.length-1; i++) {
            arrPosIni = i * factor;
            arrPosEnd = (i+1)*factor;
            if (drop1>=arrPosIni && drop1<arrPosEnd)
                segmentValue=preSumSegments[i]-origArr[drop1];
            else if (drop2>=arrPosIni && drop2<arrPosEnd)
                segmentValue=preSumSegments[i]-origArr[drop1];
            else segmentValue=preSumSegments[i];
            segmentSum+=segmentValue;
            if (segmentSum>0) {
                if (split1 == -1) {
                    if (segmentSum>sum1) {
                        //if (i==0) return false;
                        unitSum=segmentSum-segmentValue;
                        for (j=arrPosIni;j<arrPosEnd;j++) {
                            unitSum+=arr[j];
                            if (unitSum==sum1) {
                                split1 = j;
                                break;
                            }
                            if (unitSum>sum1)
                                return false;
                        }
                    } else if (segmentSum==sum1)
                        split1 = arrPosIni;
                } else if (segmentSum>sum2) {
                    //System.out.print("\rsegmentSum>sum2 "+segmentSum+">"+sum2);
                    unitSum=segmentSum-segmentValue;
                    for (j=arrPosIni;j<arr.length;j++) {
                        unitSum+=arr[j];
                        if (unitSum==sum2)
                            return true;
                        if (unitSum>sum2)
                            return false;
                    }
                } else if (segmentSum==sum2) {
                    return true;
                }
            }
        }
        return false;
    }

    private int preSum(int[] arr) {
        int totalSum=0;
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
        return b;
    }

    static boolean checkSums(int[] arr, final int totalSum) {
        Boolean preCheck = preCheck(totalSum);
        if (preCheck != null) return preCheck;
        int preSum=0;
        int split1=-1;
        int count;
        int sum1=totalSum/3;
        int sum2=2*sum1;
        for (count=0; count<arr.length; count++) {
            preSum+=arr[count];
            if (preSum>0) {
                if (split1 == -1 && preSum % sum1 == 0)
                    split1 = count;
                else if (preSum % sum2 == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Boolean preCheck(int totalSum) {
        if (totalSum%3!=0)
            return false;
        if (totalSum==0)
            return true;
        return null;
    }

}
