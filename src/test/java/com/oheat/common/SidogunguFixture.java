package com.oheat.common;

import com.oheat.common.sido.Sido;
import com.oheat.common.sigungu.Sigungu;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

public class SidogunguFixture {

    public static Sido seoul() {
        return Sido.builder()
            .ogrFid(1)
            .geometry(null)
            .ctprvnCd("11")
            .ctpKorNm("서울특별시")
            .ctpEngNm("Seoul")
            .build();
    }

    public static Sigungu jongno_gu() {
        GeometryFactory geometryFactory = new GeometryFactory();

        LinearRing shell = geometryFactory.createLinearRing(new Coordinate[]{});
        return Sigungu.builder()
            .ogrFid(1)
            .geometry(new Polygon(shell, null, geometryFactory))
            .sigCd("11110")
            .sigKorNm("종로구")
            .sigEngNm("Jongno-gu")
            .adj1hop(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff})
            .build();
    }
}
