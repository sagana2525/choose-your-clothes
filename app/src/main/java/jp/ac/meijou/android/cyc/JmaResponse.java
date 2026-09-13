package jp.ac.meijou.android.cyc;

import java.util.List;

public class JmaResponse {
    public List<TimeSeries> timeSeries;

    public static class TimeSeries {
        public List<Area> areas;
    }

    public static class Area {
        public AreaInfo area;
        public List<String> weathers;
        public List<String> weatherCodes;
        public List<String> pops;   // 降水確率
        public List<String> temps;  // 気温
    }

    public static class AreaInfo {
        public String name;
        public String code;
    }
}
