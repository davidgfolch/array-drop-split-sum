package com.dgf.arrayDropSplitSum;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Main {

    private static final Solution s = new Solution();

    public static void main(String[] args) {
        //Unit tests
        assertSolution(true,  s.run(new int[]{0,0,0,0,0,0,0,0,0}));
        assertSolution(true,  s.run(new int[]{0,0,0,0,0}));
        assertSolution(true,  s.run(new int[]{1, 3, 4, 2, 2, 2, 1, 1, 2}));
        assertSolution(false, s.run(new int[]{1}));
        assertSolution(false, s.run(new int[]{1, 1}));
        assertSolution(false, s.run(new int[]{1, 1, 1}));
        assertSolution(false, s.run(new int[]{1, 1, 1, 1}));
        assertSolution(true,  s.run(new int[]{1, 1, 1, 1, 1}));
        assertSolution(false, s.run(new int[]{1, 1, 1, 1, 1, 1}));
        assertSolution(false, s.run(new int[]{1, 1, 1, 1, 1, 1, 1}));
        assertSolution(true,  s.run(new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
        assertSolution(false, s.run(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1}));
        IntStream.range(10, 99).forEachOrdered(i-> {
            performanceTest(true, generateArray(i,true));
            performanceTest(false, generateArray(i,false));
        });

        //Performance tests
//        IntStream.range(110, 1000).forEachOrdered(i-> {
//            performanceTest(true, generateArray(i,true));
//            performanceTest(false, generateArray(i,false));
//        });
//        IntStream.of(1000,2000).forEachOrdered(i-> {
////        IntStream.of(1000,2000,3000,4000,5000,6000,7000,8000,9000).forEachOrdered(i-> {
//            performanceTest(true,  generateArray(i,true));
//            performanceTest(false,  generateArray(i,false));
//        });
//        IntStream.of(10000, 20000,30000,40000,50000,60000,70000,80000,90000,100000).forEachOrdered(i-> {
        IntStream.of(30000,40000,50000,60000,70000,80000,90000,100000).forEachOrdered(i-> {
            performanceTest(true, generateArray(i,true));
            performanceTest(false, generateArray(i,false));
        });
        TaskManager.shutdownExecutor();
    }

    private static int[] generateArray(int size, boolean divisible) {
        int[] arr = new int[size];
        int drop1= size/3;
        int drop2= size-drop1;
        if (divisible) {
            Random random = new Random();
            Arrays.fill(arr,0,drop1, random.nextInt(999)+1);
            Arrays.fill(arr,drop1,drop2, random.nextInt(999)+1);
            Arrays.fill(arr,drop2,arr.length, random.nextInt(999)+1);
            arr[drop1]=3;
            arr[drop2]=3;
            int[] arr1=Arrays.copyOfRange(arr,0,drop1);
            int[] arr2=Arrays.copyOfRange(arr,drop1+1,drop2);
            int[] arr3=Arrays.copyOfRange(arr,drop2+1,arr.length);
            int sum1=IntStream.of(arr1).sum();
            int sum2=IntStream.of(arr2).sum();
            int sum3=IntStream.of(arr3).sum();
            int maxValue=Integer.max(Integer.max(sum1,sum2),sum3);
            if (sum1!=maxValue) arr[drop1-1]+=maxValue-sum1;
            if (sum2!=maxValue) arr[drop2-1]+=maxValue-sum2;
            if (sum3!=maxValue) arr[arr.length-1]+=maxValue-sum3;
        } else {
            for (int i=0; i<4; i++)
                arr[i]=arr.length*4;
            for (int i=4; i<arr.length; i++)
                arr[i]=1;
        }
        return arr;
    }

    private static void performanceTest(boolean success, int[] arr) {
        long start=System.currentTimeMillis();
        System.out.println("Performance test "+arr.length+" fixed items start "+toTime(start));
        boolean result = s.run(arr);
        assertSolution(success, result);
        long end=System.currentTimeMillis();
        System.out.println("Performance test "+arr.length+" random items: finished in    "+toTime(end-start)+", result="+result);
        System.out.flush();
    }

	private static String toTime(long ms) {
		long seconds = ms / 1000;
		long s = seconds % 60;
	    long m = (seconds / 60) % 60;
	    long h = (seconds / (60 * 60)) % 24;
	    return String.format("%d:%02d:%02d", h,m,s);
	}

	private static void assertSolution(boolean expectedResult, boolean solutionFound) {
		if (expectedResult==solutionFound) {
            System.out.println("Expected result OK!!! Result ="+solutionFound);
			return;
		}
		System.out.flush();
        throw new RuntimeException("Expected result FAILED!!! Result ="+solutionFound);
	}
	
}
