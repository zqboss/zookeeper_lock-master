package com.yy.lock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by luyuanyuan on 2017/9/22.
 */
public class OrderCodeGenerator {

    // 自增长序列
    private static int i = 0;

   public String getOrderCode(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-");
        return simpleDateFormat.format(date) + ++i;
   }

    public static void main(String[] args) {

        OrderCodeGenerator orderCodeGenerator = new OrderCodeGenerator();
        int a = 1000000;
        int i = 0;
        Set<String> sets = new HashSet<String>();
        while (a-->0){
            if(sets.contains(orderCodeGenerator.getOrderCode())) {
                 i++;
            }else {
                sets.add(orderCodeGenerator.getOrderCode());
            }

            System.out.println(orderCodeGenerator.getOrderCode());
        }
        System.out.println(i);

    }

}
