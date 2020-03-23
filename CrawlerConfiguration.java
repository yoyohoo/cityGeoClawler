package geo.craw.crawler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@PropertySource(value = { "classpath:geoIndex.yml " })
//@Component
@Configuration
@ConfigurationProperties(prefix = "geo")
public class CrawlerConfiguration {

    private String address = new String();

    private List<String> indexes = new ArrayList<>();

    public List<String> getPrimaries() {
        return primaries;
    }

    public void setPrimaries(List<String> primaries) {
        this.primaries = primaries;
    }

    private List<String> primaries = new ArrayList<>();

    public List<String> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
