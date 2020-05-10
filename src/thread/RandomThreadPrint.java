package thread;

/**
 * 题目：x 个线程从 1 打印到 100，随机分配时间片
 * 实现：synchronized + notifyAll 随机唤醒 + wait 阻塞睡眠
 * <p>
 * Created by Nelson on 2020/5/10.
 */
public class RandomThreadPrint {

    public static void main(String[] args) {
        // unit test
        new RandomThreadPrint().print();
    }

    private volatile int i = 0;

    private void print() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        while (i < 100) {
                            i++;
                            System.out.println(i + ", " + Thread.currentThread().getName());
                            notifyAll();
                            wait();
                        }
                    } catch (InterruptedException e) {
                        // no-op
                    }
                }
            }
        };

        Thread t1 = new Thread(runnable, "t-1");
        Thread t2 = new Thread(runnable, "t-2");
        Thread t3 = new Thread(runnable, "t-3");
        t1.start();
        t2.start();
        t3.start();
    }
}