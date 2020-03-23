package geo.craw.crawler.Controllers;

import com.sun.org.apache.xpath.internal.operations.Bool;
import geo.craw.crawler.CrawlerConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@SpringBootApplication
@RequestMapping(value = "/Crawller")
public class crawController {

    @Autowired
    private CrawlerConfiguration address;

    @Autowired
    private CrawlerConfiguration crawConfig;

    // 连接相关
    private Proxy proxy;
    private HttpURLConnection conn;
    private InputStream inStream;

    // 数据相关
    private List<String> geoList = new ArrayList<String>();

    @RequestMapping(value = "/geoCraw")
    public String geoCraw() {
        List<String> indexes = crawConfig.getIndexes();
        for (int i = 0; i < indexes.size(); i++) {
            String code = indexes.get(i).toString();
            if (isPrimary(code)) {
                System.out.print(code);
            }
            craw(code, false);
        }
        hotFix();
        for (int j = 0; j < geoList.size(); j++) {
            System.out.println(geoList.get(j));
        }
        return geoList.toString();
    }

    private void craw(String code, Boolean isFull) {
        String extention = isFull ? "_full.json" : ".json";
        String jsonPath = address.getAddress()
                + code
                + extention;
        String json = ReadJson(jsonPath);
        if (json.length() > 0 && extention.length() > 0) {
            JSONObject jsonObject = new JSONObject(json);
            printSQL(jsonObject);
        }
    }

    private void printSQL(JSONObject jsonObject) {
        JSONArray list = new JSONArray();
        list = (JSONArray) jsonObject.get("features");
        for (int i = 0; i < list.length(); i++) {
            JSONObject feature = (JSONObject) list.get(i);
            JSONObject props = (JSONObject) feature.get("properties");
            JSONObject parent = (JSONObject) props.get("parent");
            Integer parent_id = Integer.parseInt(parent.get("adcode").toString());
            String code = props.get("adcode").toString();
            Integer id = Integer.parseInt(code);
            String name = props.get("name").toString();
            String sql = "insert into region values(" + id + ",'" + name + "'," + parent_id + ");";
            geoList.add(sql);
            if (code.endsWith("00")) {
                craw(code, true);
            }
        }
    }

    private void hotFix() {
        geoList.add("insert into region values(110100,'北京市',110000);");
        geoList.add("insert into region values(120100,'天津市',120000);");
        geoList.add("insert into region values(310100,'上海市',310000);");
        geoList.add("insert into region values(500100,'重庆市',500000);");
        geoList.add("update region set parent_id=110100 where parent_id= 110000;");
        geoList.add("update region set parent_id=120100 where parent_id= 120000;");
        geoList.add("update region set parent_id=310100 where parent_id= 310000;");
        geoList.add("update region set parent_id=500100 where parent_id= 500000;");
        geoList.add("update region set parent_id=110000 where id= 110100;");
        geoList.add("update region set parent_id=120000 where id= 120100;");
        geoList.add("update region set parent_id=310000 where id= 310100;");
        geoList.add("update region set parent_id=500000 where id= 500100;");
    }

    private Boolean isPrimary(String code) {
        Boolean result = false;
        List<String> primaries = crawConfig.getPrimaries();
        for (int i = 0; i < primaries.size(); i++) {
            if (primaries.get(i) == code) {
                result = true;
            }
        }
        return result;
    }

    private String ReadJson(String jsonPath) {
        StringBuilder json = new StringBuilder();
        try {
            URL url = new URL(jsonPath);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.17.171.129", 8080));
            conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko");
            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return json.toString();
    }


}
