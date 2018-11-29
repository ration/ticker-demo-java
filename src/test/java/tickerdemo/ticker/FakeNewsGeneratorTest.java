package tickerdemo.ticker;

import org.junit.Assert;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class FakeNewsGeneratorTest {

    @Test
    public void news() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FakeNewsGenerator fakeNewsGenerator = new FakeNewsGenerator();
        Disposable subscribe = fakeNewsGenerator.news().subscribeOn(Schedulers.single()).subscribe(val -> {
            System.out.println("val = " + val);
            latch.countDown();
        });
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}