package com.zy.github.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.zy.github.domain.WeatherInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0 created by zhangyu_fh on 2020/9/28 16:32
 */
@Controller
public class TestController {

    public static void main(String[] args) {
        List<WeatherInfo> infos = new ArrayList<>();
        try {
            Document document = Jsoup.connect("http://lishi.tianqi.com/anda/index.html").get();
            Elements mainTable = document.select(".tian_three");
            Elements uls = mainTable.select(".thrui");
            for (Element ul : uls) {
                List<Node> nodes = ul.childNodesCopy();
                for (Node node : nodes) {
                    String s = node.toString();
                    if (StringUtils.isBlank(s)) {
                        continue;
                    }
                    List<Node> lisNodes = node.childNodes();
                    //只有通过下标去取

                    List<Node> filterNodes = lisNodes.stream().filter(l -> StringUtils.isNotBlank(l.toString())).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(filterNodes) && filterNodes.size() == 5){
                        WeatherInfo info = new WeatherInfo();
                        info.setTime(((Element) filterNodes.get(0)).text());
                        info.setMaxTemperature(((Element) filterNodes.get(1)).text());
                        info.setMinTemperature(((Element) filterNodes.get(2)).text());
                        info.setWeather(((Element) filterNodes.get(3)).text());
                        info.setWindDirection(((Element) filterNodes.get(4)).text());
                        infos.add(info);
                    }
                }
            }
            exportExcel(infos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportExcel(List<WeatherInfo> datas){
        String fileName = "D:\\天气信息.xlsx";
        ExcelWriterBuilder write = EasyExcel.write(fileName, WeatherInfo.class);
        write.sheet().doWrite(datas);
    }
}
