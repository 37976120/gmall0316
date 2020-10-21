package com.atguigu.gmall.all.juc;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Test01 {
    public static void main(String[] args) {
        int i = 1;
        StringBuilder s = new StringBuilder("a");
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(i);
            }
        }, "a").start();
        CompletableFuture<Object> b = CompletableFuture.supplyAsync(new Supplier<Object>() {
            void t1() {
                System.out.println(s.append("b"));
            }

            @Override
            public Object get() {
                System.out.println(i);
                this.t1();
                return null;
            }
        });
        CompletableFuture.allOf(b).join();
    }

    void innerClass() {

    }

    private static void exp() {
        CompletableFuture<Double> bsync = CompletableFuture.supplyAsync(new Supplier<Double>() {
            @Override
            public Double get() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("async");
                int i = 100 / 0;
                return 100d;
            }
        });
        bsync.exceptionally(new Function<Throwable, Double>() {
            @Override
            public Double apply(Throwable throwable) {
                System.out.println("exception");
                return 0.1d;
            }
        }).whenComplete(new BiConsumer<Double, Throwable>() {
            @Override
            public void accept(Double aDouble, Throwable throwable) {

                System.out.println("complete" + aDouble);

            }
        });

        CompletableFuture.allOf(bsync).join();
    }
}
