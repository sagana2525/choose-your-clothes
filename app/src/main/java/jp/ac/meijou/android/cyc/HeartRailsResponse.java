package jp.ac.meijou.android.cyc;

import java.util.List;

public class HeartRailsResponse {
    public ResponseData response;

    public static class ResponseData {
        public List<Location> location;
    }

    public static class Location {
        public String city; // 市区町村名
    }
}
