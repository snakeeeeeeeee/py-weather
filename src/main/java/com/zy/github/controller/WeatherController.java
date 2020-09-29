package com.zy.github.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.zy.github.domain.RequestParam;
import com.zy.github.domain.WeatherInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version 1.0 created by zhangyu_fh on 2020/9/28 17:46
 */
@Controller
public class WeatherController {


    @RequestMapping(value = "/weather/index")
    public String index() {
        return "index";
    }

    /**
     * 下载Excel
     *
     * @param response
     */
    @RequestMapping(value = "/weather/export-excel")
    public void uploadExcel(HttpServletResponse response, RequestParam param) {
        ExcelWriter writer = null;
        OutputStream out = null;
        try {
            List<WeatherInfo> userList = getDatas(param.getTargetUrl());
            out = response.getOutputStream();
            writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
            Sheet sheet = new Sheet(0, 1, WeatherInfo.class);
            sheet.setSheetName("天气信息");
            writer.write(userList, sheet);
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(("天气信息.xlsx").getBytes(), "ISO8859-1"));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.finish();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @RequestMapping(value = "/weather/get-data")
    @ResponseBody
    public List<WeatherInfo> getDatas(String dataUrl) {
        List<WeatherInfo> infos = new ArrayList<>();
        try {
            Map<String, String> header = new HashMap<>();
            header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            header.put("Accept-Encoding", "gzip, deflate");
            header.put("Accept-Language", "zh-CN,zh;q=0.9");
            header.put("Cache-Control", "max-age=0");
            header.put("Connection", "keep-alive");
            header.put("Cookie", "UM_distinctid=174d4afc60d126-0b42f13b5274dc-e343166-384000-174d4afc60eb7b; Hm_lvt_ab6a683aa97a52202eab5b3a9042a8d2=1601296124; cityPy=chengdu; cityPy_expire=1601908484; CNZZDATA1275796416=565365793-1601292994-%7C1601302164; Hm_lpvt_ab6a683aa97a52202eab5b3a9042a8d2=1601305082");
            header.put("Host", "Mon, 28 Sep 2020 08:41:06 GMT");
            header.put("Referer", "http://lishi.tianqi.com/nanchong/202007.html");
            header.put("Upgrade-Insecure-Requests", "1");
            header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");

            Document document = Jsoup.connect(dataUrl).timeout(60000).headers(header).get();
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
                    if (CollectionUtils.isNotEmpty(filterNodes) && filterNodes.size() == 5) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return infos;
    }
}
