package com.geosys.modules.sys.controller;

import com.geosys.common.utils.ExcelUtils;
import com.geosys.common.utils.R;
import com.geosys.modules.sys.entity.BIMEntity;
import com.geosys.modules.sys.entity.SysLayerEntity;
import com.geosys.modules.sys.entity.SysPanoEntity;
import com.geosys.modules.sys.service.SysLayerService;
import com.geosys.modules.sys.service.SysPanoService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirstController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ExcelUtils excelUtils;

    @Autowired
    private SysLayerService sysLayerService;

    @Autowired
    private SysPanoService sysPanoService;

    @RequestMapping({"/index"})
    public String sayHello() {
        return "xdd";
    }

    @GetMapping({"/read_csv"})
    public List<Map<String, String>> readCsv() {
        try {
            List<Map<String, String>> rows = this.sysPanoService.readCsv();
            if (!rows.isEmpty())
                rows.remove(0);
            return rows;
        } catch (Exception e) {
            this.logger.error("Error reading CSV file", e);
            return null;
        }
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<?> getPanoById(@PathVariable("id") Long panoId) {
        SysPanoEntity pano = this.sysPanoService.getPanoById(panoId);
        if (pano != null)
            return ResponseEntity.ok(pano);
        return ResponseEntity.notFound().build();
    }

    @PostMapping({"/insert_pano"})
    public ResponseEntity<String> insertPano(@RequestBody SysPanoEntity sysPanoEntity) {
        try {
            this.sysPanoService.insertSysPano(sysPanoEntity);
            return ResponseEntity.ok("Insert successful");
        } catch (Exception e) {
            this.logger.error("Error inserting pano", e);
            return ResponseEntity.status(500).body("Error inserting pano");
        }
    }

    @PostMapping({"/insert_pano_from_csv"})
    public ResponseEntity<String> insertPanoFromCsv() {
        try {
            this.sysPanoService.insertPanoFromCsv();
            return ResponseEntity.ok("Insert from CSV successful");
        } catch (Exception e) {
            this.logger.error("Error inserting pano from CSV", e);
            return ResponseEntity.status(500).body("Insert from CSV failed");
        }
    }

    @PostMapping({"/update_pano_all_columns"})
    public ResponseEntity<?> updatePanoAllColumns(@RequestBody List<SysPanoEntity> entities) {
        try {
            this.sysPanoService.updateAllColumnsBatch(entities);
            return ResponseEntity.ok("Update successful");
        } catch (Exception e) {
            this.logger.error("Error updating pano", e);
            return ResponseEntity.status(500).body("Error updating pano");
        }
    }

    @PutMapping({"/update_pano/{panoId}"})
    public R updatePano(@PathVariable Long panoId, @RequestBody SysPanoEntity updatedPano) {
        try {
            this.sysPanoService.updatePanoById(panoId, updatedPano);
            return R.ok().put("Pano updated successfully", panoId);
        } catch (Exception e) {
            return R.error("Error updating pano");
        }
    }

    @DeleteMapping({"/delete_pano/{panoId}"})
    public R deletePanoById(@PathVariable Long panoId) {
        try {
            this.sysPanoService.deletePanoById(panoId);
            return R.ok().put("Pano deleted successfully", panoId);
        } catch (Exception e) {
            return R.error("Error deleting pano");
        }
    }

    @RequestMapping({"convert"})
    public R convert(@RequestBody BIMEntity entity) throws IOException {
        Long layerId = entity.getLayerId();
        Long layerOffset = entity.getLayerOffset();
        String layerPath = entity.getLayerPath();
        String bimPath = entity.getBimPath();
        String json = this.excelUtils.excelToJson(layerPath, bimPath, layerOffset);
        this.logger.info(json);
        SysLayerEntity layer = new SysLayerEntity();
        layer.setLayerId(layerId);
        layer.setScriptAnim(json);
        this.sysLayerService.updateById(layer);
        return R.ok().put("layerId", layerId);
    }
}
