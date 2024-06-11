package com.geosys.modules.sys.service.impl;

import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geosys.modules.sys.dao.SysPanoDao;
import com.geosys.modules.sys.entity.SysPanoEntity;
import com.geosys.modules.sys.service.SysPanoService;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBWriter;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.io.Proj4FileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysPanoServiceImpl extends ServiceImpl<SysPanoDao, SysPanoEntity> implements SysPanoService {
    private static final String WGS84 = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";

    @Autowired
    private SysPanoDao sysPanoDao;

    private static final String FILE_PATH = "C:\\Users\\rayjx\\OneDrive\\Desktop\\repo\\EO(2).CSV";

    private static final String MAPPING_CSV_PATH = "C:\\Users\\rayjx\\OneDrive\\Desktop\\repo\\panolist.csv";

    public void insertSysPano(SysPanoEntity sysPanoEntity) {
        save(sysPanoEntity);
    }

    public List<Map<String, String>> readCsv() {
        List<Map<String, String>> rows = new ArrayList<>();
        CsvReader reader = CsvUtil.getReader();
        File csvFile = new File("C:\\Users\\rayjx\\OneDrive\\Desktop\\repo\\EO(2).CSV");
        List<CsvRow> csvRows = reader.read(csvFile).getRows();
        if (!csvRows.isEmpty()) {
            List<String> headers = ((CsvRow)csvRows.get(0)).getRawList();
            for (int i = 1; i < csvRows.size(); i++) {
                CsvRow csvRow = csvRows.get(i);
                Map<String, String> rowMap = new HashMap<>();
                List<String> values = csvRow.getRawList();
                for (int j = 0; j < headers.size(); j++)
                    rowMap.put(headers.get(j), values.get(j));
                try {
                    double easting = Double.parseDouble(rowMap.get("Easting"));
                    double northing = Double.parseDouble(rowMap.get("Northing"));
                    double height = Double.parseDouble(rowMap.get("Height"));
                    ProjCoordinate wgs84Coordinate = coordinateToWGS84("2326", easting, northing, height);
                    rowMap.put("Longitude", String.valueOf(wgs84Coordinate.x));
                    rowMap.put("Latitude", String.valueOf(wgs84Coordinate.y));
                    rowMap.put("Height", String.valueOf(wgs84Coordinate.z));
                    String wkbGeom = convertToWKB(wgs84Coordinate.x, wgs84Coordinate.y);
                    rowMap.put("Geom", wkbGeom);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rows.add(rowMap);
            }
        }
        return rows;
    }

    private List<Map<String, String>> readPanoListCsv() {
        List<Map<String, String>> rows = new ArrayList<>();
        CsvReader reader = CsvUtil.getReader();
        File csvFile = new File("C:\\Users\\rayjx\\OneDrive\\Desktop\\repo\\panolist.csv");
        List<CsvRow> csvRows = reader.read(csvFile).getRows();
        if (!csvRows.isEmpty()) {
            List<String> headers = ((CsvRow)csvRows.get(0)).getRawList();
            for (int i = 1; i < csvRows.size(); i++) {
                CsvRow csvRow = csvRows.get(i);
                Map<String, String> rowMap = new HashMap<>();
                List<String> values = csvRow.getRawList();
                for (int j = 0; j < headers.size(); j++)
                    rowMap.put(headers.get(j), values.get(j));
                rows.add(rowMap);
            }
        }
        return rows;
    }

    public void insertPanoFromCsv() {
        List<Map<String, String>> rows = readCsv();
        List<Map<String, String>> panoList = readPanoListCsv();
        List<SysPanoEntity> entities = new ArrayList<>();
        for (Map<String, String> row : rows) {
            SysPanoEntity entity = new SysPanoEntity();

            entity.setPanoName(getPanoNameFromPanoList(panoList, row.get("Photo Name")));
            entity.setRotationX(Float.valueOf(Float.parseFloat(row.get("Omega"))));
            entity.setRotationY(Float.valueOf(Float.parseFloat(row.get("Phi"))));
            entity.setRotationZ(Float.valueOf(Float.parseFloat(row.get("Kappa"))));
            entity.setZ(Float.valueOf(Float.parseFloat(row.get("Height"))));
            entity.setGeom(row.get("Geom"));
            entities.add(entity);
        }
        if (CollectionUtils.isNotEmpty(entities)) {
            List<List<SysPanoEntity>> splits = Lists.partition(entities, 500);
            for (List<SysPanoEntity> split : splits)
                this.sysPanoDao.insertBatch(split);
        }
    }

    private String getPanoNameFromPanoList(List<Map<String, String>> panoList, String photoName) {
        for (Map<String, String> row : panoList) {
            if (row.containsKey("file_name") && ((String)row.get("file_name")).equals(photoName))
                return row.get("pano_id");
        }
        return null;
    }

    public void updateAllColumnsBatch(List<SysPanoEntity> entities) {
        if (CollectionUtils.isNotEmpty(entities))
            this.sysPanoDao.updateAllColumnsBatch(entities);
    }

    public void updatePanoById(Long panoId, SysPanoEntity updatedPano) {
        updatedPano.setPanoId(panoId);
        String postGISGeom = convertToPostGISGeom(updatedPano.getGeom());
        updatedPano.setGeom(postGISGeom);
        ((SysPanoDao)this.baseMapper).updateById(updatedPano);
    }

    private String convertToWKB(double longitude, double latitude) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        WKBWriter writer = new WKBWriter();
        byte[] wkb = writer.write((Geometry)point);
        return WKBWriter.toHex(wkb);
    }

    private String convertToPostGISGeom(String geomString) {
        try {
            int startIndex = geomString.indexOf('[');
            int endIndex = geomString.indexOf(']');
            String coordinatesString = geomString.substring(startIndex + 1, endIndex);
            String[] coordinates = coordinatesString.split(",");
            double x = Double.parseDouble(coordinates[0]);
            double y = Double.parseDouble(coordinates[1]);
            return "POINT(" + x + " " + y + ")";
        } catch (Exception e) {
            if (geomString.startsWith("(")) {
                String[] coords = geomString.replaceAll("[()]", "").split(",");
                double x = Double.parseDouble(coords[0].trim());
                double y = Double.parseDouble(coords[1].trim());
                return "POINT(" + x + " " + y + ")";
            }
            return geomString;
        }
    }

    @Transactional
    public void deletePanoById(Long panoId) {
        ((SysPanoDao)this.baseMapper).deleteById(panoId);
    }

    public SysPanoEntity getPanoById(Long panoId) {
        try {
            return this.sysPanoDao.getPanoById(panoId);
        } catch (Exception e) {
            return null;
        }
    }

    private ProjCoordinate coordinateToWGS84(String epsg, double longitude, double latitude, double height) throws IOException {
        Proj4FileReader proj4FileReader = new Proj4FileReader();
        CRSFactory crsFactory = new CRSFactory();
        String[] coordinateParam = proj4FileReader.readParametersFromFile("epsg", epsg);
        CoordinateReferenceSystem sourceCoordinate = crsFactory.createFromParameters(epsg, coordinateParam);
        CoordinateReferenceSystem targetCoordinate = crsFactory.createFromParameters("4326", "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");
        CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = transformFactory.createTransform(sourceCoordinate, targetCoordinate);
        ProjCoordinate projCoordinate = new ProjCoordinate(longitude, latitude, height);
        transform.transform(projCoordinate, projCoordinate);
        return projCoordinate;
    }
}