package com.ajiu.test.bloom;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.DecimalFormat;
import java.util.*;

/**
 * description: BloomFilter布隆过滤器的使用，可以指定误判率，进行误判率测试
 * @author: JiuDongDong
 * date: 2018/9/16.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath*:spring-test.xml"})
public class BloomFilterDemo2 {
    private static final int pieces = 10000000;//2千万个订单
    // 指定布隆过滤器的误判率---默认误判率为0.03，根据业务需要自己调整
    double wrongFate = 0.0001;

    /**
     * Description: 误判率测试
     * @author: JiuDongDong
     * date: 2018/9/16 11:06
     */
    @Test
    public void erroneousTest() {
        //初始化一个存储String类型的过滤器，初始化大小为100W
//        // 第一种方式，使用默认的误判率0.03
//        BloomFilter<String> bloomFilter =
//                BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), pieces);
        // 第二种方式，使用自定义的误判率0.001
        BloomFilter<String> bloomFilter =
                BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), pieces, wrongFate);

        //初始化一个存储String的set，初始化大小为100W，用于后面测试误判率时的判断是否包含正确值
        Set<String> set = new HashSet<>(pieces);
        //初始化一个存储String的list，初始化大小为100W，用于后面测试误判率时的获取正确值
        List<String> list = new ArrayList<>(pieces);

        //向3个容器分别初始化100万个随机并且唯一的字符串
        for (int i=0; i < pieces; i ++) {
            String random = UUID.randomUUID().toString();
            // 布隆过滤器加入
            bloomFilter.put(random);
            // list set 加入
            list.add(random);
            set.add(random);
        }

        /* 以下验证布隆过滤器的误判率 */
        //判断布隆过滤器判断正确的次数
        int right = 0;
        //判断布隆过滤器判断错误的次数
        int wrong = 0;

        for (int i = 0; i < 10000000; i ++) {
            // 按照一定比例（100倍数）选择布隆过滤器中一定存在的值，其它选择一定不存在的随机数，做判断
            String test = i % 100 == 0 ? list.get(i/100) : UUID.randomUUID().toString();
            if (bloomFilter.mightContain(test)) {//bloomFilter认为bloomFilter里有这个值
                if (set.contains(test)) {//set再次去做校验，set里有才是真的有
                    // bloomFilter认为bloomFilter里有这个值，set也真正有，说明没误判
                    right++;
                } else {
                    // bloomFilter认为bloomFilter里有这个值，而set里却不存在，说明误判了
                    wrong ++;
                }
            }
        }
        System.out.println("--------------right--------------: " + right);
        System.out.println("--------------wrong--------------: " + wrong);
        // 实际误判率
        DecimalFormat decimalFormat = new DecimalFormat("0.00000");
        System.out.println("设定误判率= " + String.valueOf(wrongFate) +
                "，实际误判率 = " + decimalFormat.format((double)wrong / 10000000));
    }

}
